import java.io.*;
import java.net.*;
import java.sql.*;
import java.util.*;

public class ChatServer {
    private static Set<ClientHandler> clientHandlers = new HashSet<>();
    private static final String DB_URL = "jdbc:mysql://localhost:3306/ChatApp";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "12345678";

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(12345)) {
            System.out.println("Chat server started on port 12345...");

            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("New client connected: " + socket);

                ClientHandler clientHandler = new ClientHandler(socket);
                clientHandlers.add(clientHandler);
                new Thread(clientHandler).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static class ClientHandler implements Runnable {
        private Socket socket;
        private PrintWriter out;
        private String username;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                 PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {
                this.out = out;

                // Authentication
                String username = in.readLine();
                String password = in.readLine();

                if (authenticate(username, password)) {
                    this.username = username;
                    out.println("Welcome to the chat, " + username + "!");
                    broadcast(username + " has joined the chat!");

                    String message;
                    while ((message = in.readLine()) != null) {
                        broadcast(username + ": " + message);
                    }
                } else {
                    out.println("Authentication failed. Disconnecting...");
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                disconnect();
            }
        }

        private boolean authenticate(String username, String password) {
            try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
                 PreparedStatement stmt = conn.prepareStatement("SELECT * FROM Users WHERE username = ? AND password = ?")) {
                stmt.setString(1, username);
                stmt.setString(2, password);
                ResultSet rs = stmt.executeQuery();
                return rs.next();
            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
        }

        private void broadcast(String message) {
            System.out.println(message);  // For server-side debugging
            for (ClientHandler client : clientHandlers) {
                if (client != this) {
                    client.out.println(message);  // Send the message to all other clients
                }
            }
        }


        private void disconnect() {
            try {
                if (username != null) {
                    System.out.println(username + " has left the chat.");
                    broadcast(username + " has left the chat.");
                }
                clientHandlers.remove(this);
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
