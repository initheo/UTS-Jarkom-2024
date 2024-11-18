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

public class ChatClient {
    private BufferedReader in;
    private PrintWriter out;
    private JFrame frame;
    private JTextPane messageArea;
    private JTextField messageInput;
    private String serverIP;
    private int serverPort;
    private String username;
    private StyledDocument doc;
    private SimpleDateFormat timeFormat;

    public ChatClient() {
        timeFormat = new SimpleDateFormat("HH:mm");

        // Show configuration dialog first
        if (!showConfigDialog()) {
            System.exit(0);
        }

        // Set up the main chat window
        frame = new JFrame("Chat Client - " + username);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Message area using JTextPane instead of JTextArea
        messageArea = new JTextPane();
        messageArea.setEditable(false);
        messageArea.setBackground(new Color(240, 240, 240));
        doc = messageArea.getStyledDocument();

        // Create and add styles
        addStylesToDocument();

        // Set minimum size and make it scrollable
        messageArea.setPreferredSize(new Dimension(500, 500));
        JScrollPane scrollPane = new JScrollPane(messageArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        frame.getContentPane().add(scrollPane, BorderLayout.CENTER);

        // Input panel
        JPanel inputPanel = new JPanel(new BorderLayout(5, 5));
        messageInput = new JTextField();
        JButton sendButton = new JButton("Send");
        sendButton.setForeground(Color.BLACK);
        sendButton.setFocusPainted(false);
        inputPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        inputPanel.add(messageInput, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);
        frame.getContentPane().add(inputPanel, BorderLayout.SOUTH);

        // Add action listeners
        ActionListener sendListener = e -> {
            String message = messageInput.getText();
            if (!message.trim().isEmpty()) {
                out.println(username + ": " + message);
                messageInput.setText("");
            }
        };

        messageInput.addActionListener(sendListener);
        sendButton.addActionListener(sendListener);

        // Window listener for handling closure
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

    private void addStylesToDocument() {
        // Style for messages from other users (left-aligned)
        Style leftStyle = messageArea.addStyle("leftStyle", null);
        StyleConstants.setAlignment(leftStyle, StyleConstants.ALIGN_LEFT);
        StyleConstants.setForeground(leftStyle, Color.BLACK);
        StyleConstants.setSpaceAbove(leftStyle, 5);
        StyleConstants.setSpaceBelow(leftStyle, 5);
        StyleConstants.setLeftIndent(leftStyle, 20);

        // Style for messages from self (right-aligned)
        Style rightStyle = messageArea.addStyle("rightStyle", null);
        StyleConstants.setAlignment(rightStyle, StyleConstants.ALIGN_RIGHT);
        StyleConstants.setForeground(rightStyle, Color.BLACK);
        StyleConstants.setSpaceAbove(rightStyle, 5);
        StyleConstants.setSpaceBelow(rightStyle, 5);
        StyleConstants.setRightIndent(rightStyle, 20);

        // Style for server messages (center-aligned)
        Style centerStyle = messageArea.addStyle("centerStyle", null);
        StyleConstants.setAlignment(centerStyle, StyleConstants.ALIGN_CENTER);
        StyleConstants.setForeground(centerStyle, Color.GRAY);
        StyleConstants.setItalic(centerStyle, true);
        StyleConstants.setSpaceAbove(centerStyle, 5);
        StyleConstants.setSpaceBelow(centerStyle, 5);
    }

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

                // Auto-scroll to bottom
                messageArea.setCaretPosition(doc.getLength());
            } catch (BadLocationException e) {
                e.printStackTrace();
            }
        });
    }

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

    private void connectToServer() throws IOException {
        Socket socket = new Socket(serverIP, serverPort);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);

        // Send join message
        out.println("SERVER:" + username + " has joined the chat.");

        // Start message listening thread
        new Thread(() -> {
            try {
                String message;
                while ((message = in.readLine()) != null) {
                    if (message.startsWith("SERVER:")) {
                        // Server messages (centered)
                        appendMessage(message.substring(7), "centerStyle");
                    } else if (message.startsWith(username + ": ")) {
                        // Own messages (right-aligned)
                        appendMessage(message.substring(username.length() + 2), "rightStyle");
                    } else {
                        // Other users' messages (left-aligned)
                        appendMessage(message, "leftStyle");
                    }
                }
            } catch (IOException e) {
                appendMessage("Lost connection to server", "centerStyle");
            }
        }).start();
    }

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

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ChatClient client = new ChatClient();
            client.start();
        });
    }
}