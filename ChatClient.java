import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

/**
 * ChatClient - Aplikasi klien chat dengan antarmuka grafis menggunakan Java
 * Swing.
 * 
 * Kelas ini mengimplementasikan aplikasi chat client yang memungkinkan pengguna
 * untuk:
 * - Terhubung ke server chat melalui socket TCP/IP
 * - Mengirim dan menerima pesan real-time
 * - Melihat status koneksi dan aktivitas pengguna lain
 * - Berinteraksi melalui antarmuka grafis yang user-friendly
 *
 * Fitur utama:
 * - Konfigurasi koneksi yang fleksibel (IP, port, username)
 * - Format pesan yang berbeda untuk pengguna sendiri, pengguna lain, dan pesan
 * sistem
 * - Timestamp pada setiap pesan
 * - Penanganan error dan notifikasi status koneksi
 * 
 * Penggunaan:
 * 1. Jalankan aplikasi
 * 2. Masukkan detail koneksi (IP, port, username)
 * 3. Aplikasi akan terhubung ke server dan menampilkan jendela chat
 * 4. Kirim pesan menggunakan input field dan tombol Send
 *
 * @author [Nama Author]
 * @version 1.0
 */
public class ChatClient {
    /** Reader untuk menerima data dari server */
    private BufferedReader in;

    /** Writer untuk mengirim data ke server */
    private PrintWriter out;

    /** Frame utama aplikasi */
    private JFrame frame;

    /** Area untuk menampilkan pesan chat */
    private JTextPane messageArea;

    /** Field untuk input pesan */
    private JTextField messageInput;

    /** Alamat IP server yang akan dihubungi */
    private String serverIP;

    /** Nomor port server */
    private int serverPort;

    /** Nama pengguna dalam chat */
    private String username;

    /** Dokumen untuk mengatur format dan style pesan */
    private StyledDocument doc;

    /** Format waktu untuk timestamp pesan (HH:mm) */
    private SimpleDateFormat timeFormat;

    /**
     * Konstruktor ChatClient.
     * Menginisialisasi komponen GUI dan meminta konfigurasi koneksi dari pengguna.
     * Jika pengguna membatalkan konfigurasi atau input tidak valid, aplikasi akan
     * keluar.
     * 
     * Urutan inisialisasi:
     * 1. Set format waktu
     * 2. Tampilkan dialog konfigurasi
     * 3. Inisialisasi frame utama
     * 4. Setup komponen GUI (messageArea, scrollPane, inputPanel)
     * 5. Setup event listener untuk pengiriman pesan
     * 6. Setup window listener untuk handling ketika aplikasi ditutup
     */
    public ChatClient() {
        timeFormat = new SimpleDateFormat("HH:mm");

        if (!showConfigDialog()) {
            System.exit(0);
        }

        frame = new JFrame("Chat Client - " + username);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        messageArea = new JTextPane();
        messageArea.setEditable(false);
        messageArea.setBackground(new Color(240, 240, 240));
        doc = messageArea.getStyledDocument();

        addStylesToDocument();

        messageArea.setPreferredSize(new Dimension(500, 500));
        JScrollPane scrollPane = new JScrollPane(messageArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        frame.getContentPane().add(scrollPane, BorderLayout.CENTER);

        JPanel inputPanel = new JPanel(new BorderLayout(5, 5));
        messageInput = new JTextField();
        JButton sendButton = new JButton("Send");
        sendButton.setForeground(Color.BLACK);
        sendButton.setFocusPainted(false);
        inputPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        inputPanel.add(messageInput, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);
        frame.getContentPane().add(inputPanel, BorderLayout.SOUTH);

        ActionListener sendListener = e -> {
            String message = messageInput.getText();
            if (!message.trim().isEmpty()) {
                out.println(username + ": " + message);
                messageInput.setText("");
            }
        };

        messageInput.addActionListener(sendListener);
        sendButton.addActionListener(sendListener);

        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (out != null) {
                    out.println("SERVER:" + username + " has left the chat.");
                }
            }
        });

        frame.setSize(600, 600);
        frame.setLocationRelativeTo(null);
    }

    /**
     * Menambahkan style untuk format pesan dalam chat.
     * 
     * Style yang ditambahkan:
     * 1. leftStyle - untuk pesan dari pengguna lain
     * - Rata kiri
     * - Warna hitam
     * - Spasi atas dan bawah 5 pixel
     * - Indent kiri 20 pixel
     * 
     * 2. rightStyle - untuk pesan dari pengguna sendiri
     * - Rata kanan
     * - Warna hitam
     * - Spasi atas dan bawah 5 pixel
     * - Indent kanan 20 pixel
     * 
     * 3. centerStyle - untuk pesan sistem
     * - Rata tengah
     * - Warna abu-abu
     * - Font miring (italic)
     * - Spasi atas dan bawah 5 pixel
     */
    private void addStylesToDocument() {
        Style leftStyle = messageArea.addStyle("leftStyle", null);
        StyleConstants.setAlignment(leftStyle, StyleConstants.ALIGN_LEFT);
        StyleConstants.setForeground(leftStyle, Color.BLACK);
        StyleConstants.setSpaceAbove(leftStyle, 5);
        StyleConstants.setSpaceBelow(leftStyle, 5);
        StyleConstants.setLeftIndent(leftStyle, 20);

        Style rightStyle = messageArea.addStyle("rightStyle", null);
        StyleConstants.setAlignment(rightStyle, StyleConstants.ALIGN_RIGHT);
        StyleConstants.setForeground(rightStyle, Color.BLACK);
        StyleConstants.setSpaceAbove(rightStyle, 5);
        StyleConstants.setSpaceBelow(rightStyle, 5);
        StyleConstants.setRightIndent(rightStyle, 20);

        Style centerStyle = messageArea.addStyle("centerStyle", null);
        StyleConstants.setAlignment(centerStyle, StyleConstants.ALIGN_CENTER);
        StyleConstants.setForeground(centerStyle, Color.GRAY);
        StyleConstants.setItalic(centerStyle, true);
        StyleConstants.setSpaceAbove(centerStyle, 5);
        StyleConstants.setSpaceBelow(centerStyle, 5);
    }

    /**
     * Menambahkan pesan ke area chat dengan format yang sesuai.
     * Method ini dijalankan dalam Event Dispatch Thread untuk thread safety.
     * 
     * Format pesan:
     * - Pesan normal: [waktu] pesan
     * - Pesan sistem: pesan (tanpa timestamp)
     * 
     * @param message Isi pesan yang akan ditampilkan
     * @param style   Jenis style yang akan digunakan ("leftStyle", "rightStyle",
     *                atau "centerStyle")
     */
    private void appendMessage(String message, String style) {
        SwingUtilities.invokeLater(() -> {
            try {
                String time = timeFormat.format(new Date());
                String fullMessage = "";

                if (style.equals("centerStyle")) {
                    fullMessage = message + "\n";
                } else {
                    fullMessage = String.format("[%s] %s\n", time, message);
                }

                doc.insertString(doc.getLength(), fullMessage, messageArea.getStyle(style));
                doc.setParagraphAttributes(doc.getLength() - fullMessage.length(),
                        fullMessage.length(),
                        messageArea.getStyle(style),
                        true);

                messageArea.setCaretPosition(doc.getLength());
            } catch (BadLocationException e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * Menampilkan dialog untuk konfigurasi koneksi.
     * 
     * Dialog meminta input:
     * - Server IP (default: localhost)
     * - Port (default: 12345)
     * - Username
     * 
     * Validasi yang dilakukan:
     * - Semua field harus diisi
     * - Port harus berupa angka antara 1-65535
     * - IP dan username tidak boleh kosong
     * 
     * @return true jika konfigurasi valid dan user menekan OK
     *         false jika user membatalkan atau input tidak valid
     */
    private boolean showConfigDialog() {
        JPanel panel = new JPanel(new GridLayout(0, 1));

        JTextField ipField = new JTextField("localhost", 20);
        JTextField portField = new JTextField("12345", 20);
        JTextField usernameField = new JTextField(20);

        panel.add(new JLabel("Server IP:"));
        panel.add(ipField);
        panel.add(new JLabel("Port:"));
        panel.add(portField);
        panel.add(new JLabel("Username:"));
        panel.add(usernameField);

        int result = JOptionPane.showConfirmDialog(null, panel,
                "Enter Connection Details", JOptionPane.OK_CANCEL_OPTION);

        if (result == JOptionPane.OK_OPTION) {
            try {
                String ip = ipField.getText().trim();
                int port = Integer.parseInt(portField.getText().trim());
                String username = usernameField.getText().trim();

                if (ip.isEmpty() || username.isEmpty()) {
                    JOptionPane.showMessageDialog(null,
                            "All fields must be filled out.",
                            "Input Error",
                            JOptionPane.ERROR_MESSAGE);
                    return false;
                }

                if (port < 1 || port > 65535) {
                    JOptionPane.showMessageDialog(null,
                            "Port must be between 1 and 65535.",
                            "Input Error",
                            JOptionPane.ERROR_MESSAGE);
                    return false;
                }

                this.serverIP = ip;
                this.serverPort = port;
                this.username = username;
                return true;

            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(null,
                        "Invalid port number.",
                        "Input Error",
                        JOptionPane.ERROR_MESSAGE);
                return false;
            }
        }
        return false;
    }

    /**
     * Membuat koneksi socket ke server chat dan memulai thread untuk menerima
     * pesan.
     * 
     * Proses:
     * 1. Membuat socket connection ke server
     * 2. Inisialisasi reader dan writer
     * 3. Mengirim pesan join ke server
     * 4. Memulai thread untuk menerima pesan
     * 
     * Thread penerima pesan akan:
     * - Membaca pesan dari server secara kontinyu
     * - Memformat pesan sesuai jenisnya (sistem/user)
     * - Menampilkan pesan di area chat
     * 
     * @throws IOException jika terjadi error saat membuat koneksi
     */
    private void connectToServer() throws IOException {
        Socket socket = new Socket(serverIP, serverPort);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);

        out.println("SERVER:" + username + " has joined the chat.");

        new Thread(() -> {
            try {
                String message;
                while ((message = in.readLine()) != null) {
                    if (message.startsWith("SERVER:")) {
                        appendMessage(message.substring(7), "centerStyle");
                    } else if (message.startsWith(username + ": ")) {
                        appendMessage(message.substring(username.length() + 2), "rightStyle");
                    } else {
                        appendMessage(message, "leftStyle");
                    }
                }
            } catch (IOException e) {
                appendMessage("Lost connection to server", "centerStyle");
            }
        }).start();
    }

    /**
     * Memulai aplikasi chat client.
     * 
     * Urutan eksekusi:
     * 1. Menampilkan GUI
     * 2. Mencoba koneksi ke server
     * 3. Menampilkan status koneksi
     * 4. Menangani error jika koneksi gagal
     * 
     * Jika koneksi gagal:
     * - Menampilkan pesan error
     * - Menampilkan dialog error
     * - Menutup aplikasi
     */
    public void start() {
        frame.setVisible(true);
        try {
            connectToServer();
            appendMessage("Connected to server at " + serverIP + ":" + serverPort, "centerStyle");
            appendMessage("You are connected as: " + username, "centerStyle");
        } catch (IOException e) {
            appendMessage("Could not connect to server", "centerStyle");
            JOptionPane.showMessageDialog(frame,
                    "Could not connect to server at " + serverIP + ":" + serverPort,
                    "Connection Error",
                    JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }

    /**
     * Method main - Entry point aplikasi.
     * 
     * Membuat dan menjalankan instance ChatClient dalam Event Dispatch Thread (EDT)
     * untuk memastikan thread safety dalam operasi GUI.
     * 
     * @param args command line arguments (tidak digunakan)
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ChatClient client = new ChatClient();
            client.start();
        });
    }
}