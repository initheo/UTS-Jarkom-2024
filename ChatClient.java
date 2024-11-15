import java.awt.BorderLayout;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

public class ChatClient extends JFrame {
    private static final int PORT = 8000;
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;

    private JTextArea chatArea;
    private JTextField messageField;
    private JList<String> clientList;
    private DefaultListModel<String> clientListModel;

    public ChatClient() {
        try {
            socket = new Socket("localhost", PORT);
            String clientAddress = socket.getLocalAddress().getHostAddress();
            setTitle("Chat Application - Client IP: " + clientAddress);

            // GUI components
            chatArea = new JTextArea();
            chatArea.setEditable(false);
            JScrollPane chatScrollPane = new JScrollPane(chatArea);

            messageField = new JTextField();
            JButton sendButton = new JButton("Send");

            sendButton.addActionListener(e -> {
                String message = messageField.getText().trim();
                if (!message.isEmpty()) {
                    out.println(message);
                    messageField.setText("");
                }
            });

            clientListModel = new DefaultListModel<>();
            clientList = new JList<>(clientListModel);

            JPanel inputPanel = new JPanel(new BorderLayout(10, 10));
            inputPanel.add(messageField, BorderLayout.CENTER);
            inputPanel.add(sendButton, BorderLayout.EAST);

            JPanel mainPanel = new JPanel(new BorderLayout(100, 100));
            mainPanel.add(chatScrollPane, BorderLayout.CENTER);
            mainPanel.add(inputPanel, BorderLayout.SOUTH);
            mainPanel.add(new JScrollPane(clientList), BorderLayout.EAST);

            setContentPane(mainPanel);

            // Tambahkan pengaturan ukuran window
            setSize(800, 600); // Atur ukuran window menjadi 800x600 pixel
            setLocationRelativeTo(null); // Posisikan window di tengah layar

            // Ganti bagian server dengan koneksi client
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // Start thread untuk membaca pesan
            new Thread(() -> {
                try {
                    String message;
                    while ((message = in.readLine()) != null) {
                        final String finalMessage = message;
                        SwingUtilities.invokeLater(() -> chatArea.append(finalMessage + "\n"));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();

            // Tambahkan WindowListener
            addWindowListener(new java.awt.event.WindowAdapter() {
                @Override
                public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                    // Tutup semua koneksi
                    try {
                        if (out != null)
                            out.close();
                        if (in != null)
                            in.close();
                        if (socket != null)
                            socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    // Tutup aplikasi
                    System.exit(0);
                }
            });

            setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        } catch (IOException e) {
            setTitle("Chat Application - Not Connected");
            e.printStackTrace();
        }

        setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ChatClient::new);
    }
}
