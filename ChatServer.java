    import java.io.*;
    import java.net.*;
    import java.util.*;

    public class ChatServer {
        private static final int PORT = 12345;
        private static HashSet<PrintWriter> clientWriters = new HashSet<>();

        public static void main(String[] args) {
            System.out.println("Chat Server is running...");

            try (ServerSocket serverSocket = new ServerSocket(PORT)) {
                while (true) {
                    Socket clientSocket = serverSocket.accept();
                    System.out.println("New client connected: " + clientSocket.getInetAddress().getHostAddress());

                    // Create a new handler thread for each client
                    ClientHandler handler = new ClientHandler(clientSocket);
                    new Thread(handler).start();
                }
            } catch (IOException e) {
                System.err.println("Error in server: " + e.getMessage());
            }
        }

        // Inner class to handle client connections
        private static class ClientHandler implements Runnable {
            private Socket socket;
            private PrintWriter out;
            private BufferedReader in;

            public ClientHandler(Socket socket) {
                this.socket = socket;
            }

            public void run() {
                try {
                    // Set up input and output streams
                    in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    out = new PrintWriter(socket.getOutputStream(), true);

                    // Add this client's writer to the set
                    synchronized (clientWriters) {
                        clientWriters.add(out);
                    }

                    // Process messages from this client
                    String message;
                    while ((message = in.readLine()) != null) {
                        System.out.println("Received: " + message);
                        // Broadcast message to all connected clients
                        broadcast(message);
                    }
                } catch (IOException e) {
                    System.err.println("Error handling client: " + e.getMessage());
                } finally {
                    // Remove this client's writer from the set
                    synchronized (clientWriters) {
                        clientWriters.remove(out);
                    }
                    try {
                        socket.close();
                    } catch (IOException e) {
                        System.err.println("Error closing socket: " + e.getMessage());
                    }
                }
            }

            // Broadcast message to all connected clients
            private void broadcast(String message) {
                synchronized (clientWriters) {
                    for (PrintWriter writer : clientWriters) {
                        writer.println(message);
                    }
                }
            }
        }
    }