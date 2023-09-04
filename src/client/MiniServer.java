package client;

import command.Command;
import command.CommandCreator;
import messages.Response;
import messages.ResponseStatus;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;

public class MiniServer implements Runnable {
    private static final int BUFFER_SIZE = 1024;
    private final String host;
    private final int port;
    private ByteBuffer buffer;
    private static final String INVALID_ARGS_COUNT_MESSAGE_FORMAT =
            "Invalid count of arguments: \"%s\" expects exactly %d arguments.";
    private static final String DOWNLOAD = "download";
    public MiniServer(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public static void validateDownload(String... arguments) throws FileNotFoundException {
        if (arguments.length != 3) {
            throw new IllegalArgumentException(String.format(INVALID_ARGS_COUNT_MESSAGE_FORMAT,DOWNLOAD,3,"download <user_to_download_from> <path_to_file> <path_to_file>}"));
        }
        String sourceFile = arguments[1];
        Path path = Paths.get(sourceFile);
        if (!Files.exists(path)) {
            throw new FileNotFoundException("File " + path + " does not exist!");
        }
    }

    public void start() {
        try (ServerSocketChannel serverSocketChannel = ServerSocketChannel.open()) {
            Selector selector = Selector.open();
            configureServerSocketChannel(serverSocketChannel, selector);
            this.buffer = ByteBuffer.allocate(BUFFER_SIZE);
            while (true) {
                try {
                    int readyChannels = selector.select();
                    if (readyChannels == 0) {
                        continue;
                    }

                    Iterator<SelectionKey> keyIterator = selector.selectedKeys().iterator();
                    while (keyIterator.hasNext()) {
                        SelectionKey key = keyIterator.next();
                        if (key.isReadable()) {
                            SocketChannel clientChannel = (SocketChannel) key.channel();
                            String clientInput = getClientInput(clientChannel);
                            if (clientInput == null) {
                                continue;
                            }
                            Command cmd = CommandCreator.newCommand(clientInput);
                            Response response = null;
                            if (DOWNLOAD.equals(cmd.command())) {
                                try {
                                    validateDownload(cmd.arguments());
                                    String source = cmd.arguments()[1];
                                    String destination = cmd.arguments()[2];
                                    System.out.println("File " + source + " was requested...");
                                    writeClientOutput(clientChannel, new Response(ResponseStatus.SENDING_FILE, destination));
                                    sendFile(clientChannel, source);
                                    System.out.println("File sending finished successfully.");
                                } catch (Exception e) {
                                    response = new Response(ResponseStatus.ERROR, e.getMessage());
                                }
                            }
                            writeClientOutput(clientChannel,response);
                        } else if (key.isAcceptable()) {
                            accept(selector, key);
                        }

                        keyIterator.remove();
                    }
                } catch (IOException e) {
                    System.err.println("Error occurred while processing client request: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException("failed to start server", e);
        }
    }

    private void configureServerSocketChannel(ServerSocketChannel channel, Selector selector) throws IOException {
        channel.bind(new InetSocketAddress(this.host, this.port));
        channel.configureBlocking(false);
        channel.register(selector, SelectionKey.OP_ACCEPT);
    }

    private String getClientInput(SocketChannel clientChannel) throws IOException {
        buffer.clear();

        int readBytes = clientChannel.read(buffer);
        if (readBytes < 0) {
            clientChannel.close();
            return null;
        }

        buffer.flip();

        byte[] clientInputBytes = new byte[buffer.remaining()];
        buffer.get(clientInputBytes);

        return new String(clientInputBytes, StandardCharsets.UTF_8);
    }

    private void writeClientOutput(SocketChannel clientChannel, Response output) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(output);
        oos.flush();
        byte[] bytes = bos.toByteArray();
        buffer.clear();
        buffer.put(bytes);
        buffer.flip();

        clientChannel.write(buffer);
    }


    private void sendFile(SocketChannel socketChannel, String source) {
        RandomAccessFile aFile = null;
        try {
            File file = new File(source);
            aFile = new RandomAccessFile(file, "r");
            FileChannel inChannel = aFile.getChannel();
            ByteBuffer buffer = ByteBuffer.allocate(1024);
            while (inChannel.read(buffer) > 0) {
                buffer.flip();
                socketChannel.write(buffer);
                buffer.clear();
            }
            aFile.close();
        } catch (IOException ignored) {
        }
    }

    private void accept(Selector selector, SelectionKey key) throws IOException {
        ServerSocketChannel sockChannel = (ServerSocketChannel) key.channel();
        SocketChannel accept = sockChannel.accept();

        accept.configureBlocking(false);
        accept.register(selector, SelectionKey.OP_READ);
    }

    @Override
    public void run() {
        this.start();
    }
}
