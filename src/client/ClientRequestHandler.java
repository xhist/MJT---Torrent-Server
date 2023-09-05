package client;

import command.Command;
import command.CommandCreator;
import messages.Response;
import messages.ResponseStatus;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ClientRequestHandler implements Runnable {
    private Socket socket;
    private static final String INVALID_ARGS_COUNT_MESSAGE_FORMAT =
            "Invalid count of arguments: \"%s\" expects exactly %d arguments.";
    private static final String DOWNLOAD = "download";

    public ClientRequestHandler(Socket socket) {
        this.socket = socket;
    }

    private static void validateDownload(String... arguments) throws FileNotFoundException {
        if (arguments.length != 3) {
            throw new IllegalArgumentException(String.format(INVALID_ARGS_COUNT_MESSAGE_FORMAT,DOWNLOAD,3,"download <user_to_download_from> <path_to_file> <path_to_file>}"));
        }
        String sourceFile = arguments[1];
        Path path = Paths.get(sourceFile);
        if (!Files.exists(path)) {
            throw new FileNotFoundException("File " + path + " does not exist!");
        }
    }

    @Override
    public void run() {
        try (ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream()); // autoflush on
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            while (true) {
                String clientInput = in.readLine();

                Command cmd = CommandCreator.newCommand(clientInput);
                Response response = null;
                if ("quit".equals(cmd.command())) {
                    break;
                }
                if (DOWNLOAD.equals(cmd.command())) {
                    try {
                        validateDownload(cmd.arguments());
                        String source = cmd.arguments()[1];
                        System.out.println("File " + source + " was requested...");
                        out.writeObject(new Response(ResponseStatus.SENDING_FILE, "Sending file " + source + "..."));
                        out.flush();
                        out.writeObject(new File(source));
                        out.flush();
                        System.out.println("File sending finished successfully.");
                        response = new Response(ResponseStatus.OK, "File " + source + " downloaded successfully");
                    } catch (Exception e) {
                        response = new Response(ResponseStatus.ERROR, e.getMessage());
                    }
                    out.writeObject(response);
                    out.flush();
                }
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
}
