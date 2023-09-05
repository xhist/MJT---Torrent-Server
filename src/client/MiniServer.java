package client;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MiniServer implements Runnable {
    private final String host;
    private final int port;
    private static final Integer MAX_EXECUTOR_THREADS = 20;
    public MiniServer(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void start() {
        ExecutorService executor = Executors.newFixedThreadPool(MAX_EXECUTOR_THREADS);
        try (ServerSocket serverSocket = new ServerSocket()) {
            serverSocket.bind(new InetSocketAddress(host, port));

            Socket clientSocket;

            while (true) {

                // Calling accept() blocks and waits for connection request by a client
                // When a request comes, accept() returns a socket to communicate with this
                // client
                clientSocket = serverSocket.accept();

                System.out.println("Accepted connection request from client " + clientSocket.getInetAddress());

                // We want each client to be processed in a separate thread
                // to keep the current thread free to accept() requests from new clients
                ClientRequestHandler clientHandler = new ClientRequestHandler(clientSocket);

                // uncomment the line below to launch a thread manually
                // new Thread(clientHandler).start();
                executor.execute(clientHandler); // use a thread pool to launch a thread
            }

        } catch (IOException e) {
            throw new RuntimeException("There is a problem with the server socket", e);
        }
    }

    @Override
    public void run() {
        this.start();
    }
}
