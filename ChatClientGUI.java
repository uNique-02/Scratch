import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.Socket;

public class ChatClientGUI {
    private JFrame loginFrame, chatFrame;
    private JTextField usernameField, messageField;
    private JPasswordField passwordField;
    private JTextArea chatArea;
    private PrintWriter out;
    private BufferedReader in;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ChatClientGUI::new);
    }

    public ChatClientGUI() {
        setupLoginGUI();
    }

    private void setupLoginGUI() {
        loginFrame = new JFrame("Login");
        loginFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        loginFrame.setSize(300, 200);
        loginFrame.setLayout(new GridLayout(3, 2, 5, 5));

        JLabel usernameLabel = new JLabel("Username:");
        JLabel passwordLabel = new JLabel("Password:");
        usernameField = new JTextField();
        passwordField = new JPasswordField();

        JButton loginButton = new JButton("Login");
        loginButton.addActionListener(e -> handleLogin());

        loginFrame.add(usernameLabel);
        loginFrame.add(usernameField);
        loginFrame.add(passwordLabel);
        loginFrame.add(passwordField);
        loginFrame.add(new JLabel()); // Empty space
        loginFrame.add(loginButton);

        loginFrame.setVisible(true);
    }

    private void handleLogin() {
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());

        try {
            // Connect to server
            Socket socket = new Socket("localhost", 12345);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            // Send login credentials
            out.println(username);
            out.println(password);

            // Reader thread to listen for server responses
            new Thread(() -> {
                try {
                    String serverMessage;
                    while ((serverMessage = in.readLine()) != null) {
                        System.out.println("Server: " + serverMessage); // Debugging log
                        boolean loginValid = serverMessage.equals("Welcome to the chat, " + username + "!");
                        if (loginValid) {
                            SwingUtilities.invokeLater(() -> {
                                JOptionPane.showMessageDialog(loginFrame, "Login successful!");
                                loginFrame.dispose(); // Close login window
                                setupChatGUI(); // Open chat GUI
                                listenForMessages(); // Start listening for messages
                            });
                            break; // Exit thread after successful login
                        } else if (serverMessage.equals("Authentication failed.")) {
                            SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(loginFrame, "Login failed. Try again."));
                            break; // Exit thread after failed login
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(loginFrame, "Disconnected from server."));
                }
            }).start();
        } catch (IOException ex) {
            ex.printStackTrace(); // Debugging log
            JOptionPane.showMessageDialog(loginFrame, "Unable to connect to server.");
        }
    }

    private void setupChatGUI() {
        chatFrame = new JFrame("Chat Room");
        chatFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        chatFrame.setSize(400, 500);
        chatFrame.setLayout(new BorderLayout());

        chatArea = new JTextArea();
        chatArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(chatArea);

        messageField = new JTextField();
        JButton sendButton = new JButton("Send");

        sendButton.addActionListener(e -> sendMessage());
        messageField.addActionListener(e -> sendMessage());

        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.add(messageField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);

        chatFrame.add(scrollPane, BorderLayout.CENTER);
        chatFrame.add(inputPanel, BorderLayout.SOUTH);

        chatFrame.setVisible(true);
    }

    private void sendMessage() {
        String message = messageField.getText().trim();
        if (!message.isEmpty()) {
            // Display the sent message immediately in the chat area
            chatArea.append("You: " + message + "\n");
            out.println(message);
            messageField.setText("");
        }
    }

    private void listenForMessages() {
        new Thread(() -> {
            String message;
            try {
                while ((message = in.readLine()) != null) {
                    // Display the message received from the server in the chat area
                    chatArea.append(message + "\n");
                }
            } catch (IOException e) {
                JOptionPane.showMessageDialog(chatFrame, "Disconnected from server.");
                System.exit(0);
            }
        }).start();
    }

}
