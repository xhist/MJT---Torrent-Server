package client;

import command.Command;
import command.CommandCreator;
import interfaces.UserInterface;
import messages.Response;
import messages.ResponseStatus;
import utils.UserUtils;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

// NIO, blocking
public class Client {

    private static final int SERVER_PORT = 7777;
    private static final String SERVER_HOST = "localhost";
    private static final int BUFFER_SIZE = 512;
    private static ByteBuffer buffer = ByteBuffer.allocateDirect(BUFFER_SIZE);
    private static Set<UserInterface> users;
    private static UserInterface currentUser = null;
    private static final String INVALID_ARGS_COUNT_MESSAGE_FORMAT =
            "Invalid count of arguments: \"%s\" expects exactly %d arguments.";
    private static final String DOWNLOAD = "download";

    private static final ScheduledExecutorService SCHEDULER
            = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r);
        t.setDaemon(true);
        return t;
    });

    private static Set<String> extractFiles(String files) {
        if (files.length() < 2) {
            throw new IllegalArgumentException("The files should be in format [{<path_to_file>},]");
        }
        String filesList = files.substring(1, files.length() - 1);
        filesList = filesList.replaceAll("\\s", "");
        Set<String> extractedFiles = new LinkedHashSet<>(List.of(filesList.split(",")));
        return extractedFiles;
    }

    private static void validateDownload(String... arguments) throws FileNotFoundException {
        if (arguments.length != 3) {
            throw new IllegalArgumentException(String.format(INVALID_ARGS_COUNT_MESSAGE_FORMAT,DOWNLOAD,3,"download <user_to_download_from> [{<path_to_file>},] [{<path_to_file>},]"));
        }
        Set<String> sourceFiles = extractFiles(arguments[1]);
        Set<String> destinationFiles = extractFiles(arguments[2]);
        if (sourceFiles.size() != destinationFiles.size()) {
            throw new IllegalArgumentException("Sizes of arrays differ.");
        }
    }

    private static Response processInput(SocketChannel socketChannel, String input) throws IOException, ClassNotFoundException {
        buffer.clear(); // switch to writing mode
        buffer.put(input.getBytes()); // buffer fill
        buffer.flip(); // switch to reading mode
        socketChannel.write(buffer); // buffer drain

        buffer.clear(); // switch to writing mode
        socketChannel.read(buffer);
        buffer.flip();

        byte[] byteArray = new byte[buffer.remaining()];
        buffer.get(byteArray);
        ObjectInputStream oos = new ObjectInputStream(new ByteArrayInputStream(byteArray));
        Response response = (Response) oos.readObject();
        oos.close();
        return response;
    }

    private static void startScheduledFetchingUsers(SocketChannel socketChannel) {
        SCHEDULER.schedule(new Runnable() {
            @Override
            public void run() {
                try {
                    String listUsers = "list-users";
                    Response response = processInput(socketChannel, listUsers);
                    String[] usersString = ((String)response.message()).split(System.lineSeparator());
                    Set<UserInterface> updatedUsers = new HashSet<>();
                    for (String user : usersString) {
                        updatedUsers.add(UserUtils.processUser(user.split("-")));
                    }
                    users = updatedUsers;
                }catch (IOException | ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }
            }
        }, 30, TimeUnit.SECONDS);
    }


    public static void main(String[] args) {

        try (SocketChannel socketChannel = SocketChannel.open();
             Scanner scanner = new Scanner(System.in)) {

            socketChannel.connect(new InetSocketAddress(SERVER_HOST, SERVER_PORT));

            System.out.println("Connected to the server.");

            while (true) {
                startScheduledFetchingUsers(socketChannel);
                System.out.print("Enter message: " + System.lineSeparator());
                String message = scanner.nextLine(); // read a line from the console
                Response response = processInput(socketChannel, message);
                Command cmd = CommandCreator.newCommand(message);
                if ("register".equals(cmd.command()) && response.status() != ResponseStatus.ERROR) {
                    if (currentUser == null) {
                        currentUser = UserUtils.processUser(cmd.arguments()[0].split("-"));
                        MiniServer clientServer = new MiniServer(currentUser.getHost(), currentUser.getPort());
                        new Thread(clientServer).start();
                    }
                }
                if ("download".equals(cmd.command())) {
                    try {
                        if (currentUser == null) {
                            throw new UnsupportedOperationException("Download is not available for guest users!");
                        }
                        validateDownload(cmd.arguments());
                    } catch (Exception e) {
                        System.err.println("Encountered problem during download process: " + e.getMessage());
                        continue;
                    }
                    UserInterface user = UserUtils.processUser(cmd.arguments()[0].split("-"));
                    try (Socket downloadSocket = new Socket(user.getHost(),user.getPort());
                        ObjectInputStream in = new ObjectInputStream(downloadSocket.getInputStream());
                        PrintWriter writer = new PrintWriter(downloadSocket.getOutputStream())) {
                        Set<String> sourceFiles = extractFiles(cmd.arguments()[1]);
                        Set<String> destinationFiles = extractFiles(cmd.arguments()[2]);
                        Iterator<String> sourceIter = sourceFiles.iterator();
                        Iterator<String> destinationIter = destinationFiles.iterator();
                        while (sourceIter.hasNext() && destinationIter.hasNext()) {
                            String source = sourceIter.next();
                            String destination = destinationIter.next();
                            writer.println("download " + user + " " + source + " " + destination);
                            writer.flush();
                            response = (Response) in.readObject();
                            if (response.status() == ResponseStatus.SENDING_FILE) {
                                Files.copy(new FileInputStream((File) in.readObject()), Paths.get(destination), REPLACE_EXISTING);
                                response = (Response) in.readObject();
                            }
                            System.out.println(response);
                            response = null;
                        }
                        writer.println("quit");
                        writer.flush();
                        response = new Response(ResponseStatus.OK, "All files downloaded successfully.");
                    } catch (IOException e) {
                        throw new RuntimeException("There is a problem with the network communication", e);
                    }
                }
                System.out.println(response);
            }

        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException("There is a problem with the network communication", e);
        }
    }
}
