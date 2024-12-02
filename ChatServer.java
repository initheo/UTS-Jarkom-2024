import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;

/**
 * ChatServer - Implementasi server untuk aplikasi chat multi-client.
 * 
 * Server ini mendukung multiple koneksi client secara simultan menggunakan
 * arsitektur multi-threading. Setiap client yang terhubung akan ditangani
 * oleh thread terpisah.
 *
 * Fitur utama:
 * - Mendukung multiple client secara bersamaan
 * - Broadcasting pesan ke semua client yang terhubung
 * - Thread-safe menggunakan synchronized blocks
 * - Penanganan koneksi dan disconnection client
 * 
 * @author [Nama Author]
 * @version 1.0
 */
public class ChatServer {
    /** Port default yang digunakan server untuk listening koneksi client */
    private static final int PORT = 12345;

    /**
     * Set untuk menyimpan PrintWriter dari setiap client yang terhubung.
     * Digunakan untuk broadcasting pesan ke semua client.
     */
    private static HashSet<PrintWriter> clientWriters = new HashSet<>();

    /**
     * Method utama untuk menjalankan server chat.
     * 
     * Server akan:
     * 1. Membuat ServerSocket pada port yang ditentukan
     * 2. Menunggu koneksi client secara terus menerus
     * 3. Membuat thread handler baru untuk setiap client yang terhubung
     * 
     * @param args command line arguments (tidak digunakan)
     */
    public static void main(String[] args) {
        System.out.println("Chat Server is running...");

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected: " + clientSocket.getInetAddress().getHostAddress());

                ClientHandler handler = new ClientHandler(clientSocket);
                new Thread(handler).start();
            }
        } catch (IOException e) {
            System.err.println("Error in server: " + e.getMessage());
        }
    }

    /**
     * Kelas inner untuk menangani koneksi individual client.
     * Mengimplementasikan interface Runnable untuk mendukung multi-threading.
     * 
     * Setiap instance dari kelas ini menangani:
     * - Komunikasi dengan satu client
     * - Membaca pesan dari client
     * - Broadcasting pesan ke semua client yang terhubung
     * - Membersihkan resources saat client disconnect
     */
    private static class ClientHandler implements Runnable {
        /** Socket untuk koneksi dengan client */
        private Socket socket;

        /** Writer untuk mengirim data ke client */
        private PrintWriter out;

        /** Reader untuk menerima data dari client */
        private BufferedReader in;

        /**
         * Konstruktor ClientHandler.
         * 
         * @param socket Socket yang terhubung dengan client
         */
        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        /**
         * Method run yang dijalankan dalam thread terpisah.
         * 
         * Proses yang dilakukan:
         * 1. Inisialisasi streams input/output
         * 2. Menambahkan writer ke collection clientWriters
         * 3. Membaca dan mem-broadcast pesan dari client
         * 4. Membersihkan resources saat client disconnect
         * 
         * Penanganan error:
         * - IOException saat komunikasi dengan client
         * - Pembersihan resources di block finally
         */
        public void run() {
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);

                synchronized (clientWriters) {
                    clientWriters.add(out);
                }

                String message;
                while ((message = in.readLine()) != null) {
                    System.out.println("Received: " + message);
                    broadcast(message);
                }
            } catch (IOException e) {
                System.err.println("Error handling client: " + e.getMessage());
            } finally {
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

        /**
         * Mengirim pesan ke semua client yang terhubung.
         * Method ini thread-safe menggunakan synchronized block.
         * 
         * @param message Pesan yang akan di-broadcast ke semua client
         */
        private void broadcast(String message) {
            synchronized (clientWriters) {
                for (PrintWriter writer : clientWriters) {
                    writer.println(message);
                }
            }
        }
    }
}