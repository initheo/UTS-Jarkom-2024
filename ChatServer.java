import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

// Server Component
public class ChatServer {
    private static final int PORT = 8000;
    private static final Map<String, PrintWriter> clients = new HashMap<>();

    public static void main(String[] args) {
        try {
            ServerSocket serverSocket = new ServerSocket(PORT);
            System.out.println("Chat server started, listening on port " + PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                String clientId = "Client-" + clients.size();
                PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true);
                clients.put(clientId, writer);
                System.out.println("New client connected: " + clientId);

                new ClientHandler(clientSocket, clientId).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class ClientHandler extends Thread {
        private final Socket clientSocket;
        private final String clientId;
        private final PrintWriter clientWriter;

        public ClientHandler(Socket socket, String id) throws IOException {
            clientSocket = socket;
            clientId = id;
            clientWriter = new PrintWriter(socket.getOutputStream(), true);
            clients.put(clientId, clientWriter);
        }

        public void run() {
            try {
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

                broadcastMessage("SERVER", "Client " + clientId + " has joined the chat");

                String message;
                while ((message = in.readLine()) != null) {
                    System.out.println("Received message from " + clientId + ": " + message);
                    broadcastMessage(clientId, message);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                clients.remove(clientId);
                broadcastMessage("SERVER", "Client " + clientId + " has left the chat");
            }
        }

        private void broadcastMessage(String sender, String message) {
            String fullMessage = sender + ": " + message;
            for (PrintWriter writer : clients.values()) {
                writer.println(fullMessage);
            }
        }
    }
}