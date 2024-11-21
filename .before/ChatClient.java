import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import javax.swing.BorderFactory;
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
    private static final int PORT = 9000;
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;

    private JTextArea chatArea;
    private JTextField messageField;
    private JList<String> clientList;
    private DefaultListModel<String> clientListModel;
    private static final String SERVER_PREFIX = "[SERVER]";
    private String myUsername;

    public ChatClient() {
        try {
            socket = new Socket("192.168.1.2", PORT);
            String clientAddress = socket.getInetAddress().getHostAddress();
            setTitle("Chat Application - Client IP: " + clientAddress);

            // GUI components
            chatArea = new JTextArea() {
                @Override
                public void append(String str) {
                    if (str == null)
                        return;

                    String paddedText = "    " + str.trim() + "    \n";

                    if (str.startsWith(SERVER_PREFIX)) {
                        StringBuilder centeredText = new StringBuilder();
                        centeredText.append(String.format("%" +
                                ((getWidth() / getFontMetrics(getFont()).charWidth('m')) / 2 +
                                        str.length() / 2)
                                + "s", str));
                        super.append(centeredText.toString() + "\n");
                    } else if (str.startsWith(myUsername)) {
                        StringBuilder rightAlignedText = new StringBuilder();
                        rightAlignedText.append(String.format("%" +
                                (getWidth() / getFontMetrics(getFont()).charWidth('m') - 5) +
                                "s", paddedText));
                        super.append(rightAlignedText.toString());
                    } else {
                        super.append(paddedText);
                    }
                }
            };
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

            // Atur ukuran panel
            JPanel inputPanel = new JPanel(new BorderLayout(10, 10));
            inputPanel.add(messageField, BorderLayout.CENTER);
            inputPanel.add(sendButton, BorderLayout.EAST);

            // Atur ukuran panel utama dengan padding yang lebih kecil
            JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
            mainPanel.add(chatScrollPane, BorderLayout.CENTER);
            mainPanel.add(inputPanel, BorderLayout.SOUTH);

            // Atur ukuran daftar client
            JScrollPane clientListScrollPane = new JScrollPane(clientList);
            clientListScrollPane.setPreferredSize(new Dimension(150, getHeight())); // Lebar tetap 150px
            mainPanel.add(clientListScrollPane, BorderLayout.EAST);

            // Tambahkan padding ke panel utama
            mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

            // Atur ukuran minimum komponen
            messageField.setPreferredSize(new Dimension(0, 30)); // Tinggi input field 30px
            sendButton.setPreferredSize(new Dimension(80, 30)); // Ukuran button 80x30px

            setContentPane(mainPanel);
            setSize(800, 600);
            setLocationRelativeTo(null);

            // Ganti bagian server dengan koneksi client
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // Modifikasi thread pembaca pesan
            new Thread(() -> {
                try {
                    String message;
                    myUsername = in.readLine();
                    while ((message = in.readLine()) != null) {
                        final String finalMessage = message;
                        SwingUtilities.invokeLater(() -> chatArea.append(finalMessage));
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

            setSize(800, 600);
            setLocationRelativeTo(null);
        }

        setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ChatClient::new);
    }
}
