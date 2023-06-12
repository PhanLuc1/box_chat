package main;
import java.io.*;
import java.net.*;
import java.util.*;

public class ChatServer {
    private static List<ClientHandler> clients = new ArrayList<>();
    private static String privateRecipient = null;

    public static void main(String[] args) {
        try {
            ServerSocket serverSocket = new ServerSocket(9999);
            System.out.println("Server is running and listening on port 9999...");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected");

                ClientHandler clientHandler = new ClientHandler(clientSocket);
                clients.add(clientHandler);
                clientHandler.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static class ClientHandler extends Thread {
        private Socket clientSocket;
        private PrintWriter writer;
        private String name;

        public ClientHandler(Socket socket) {
            this.clientSocket = socket;
        }

        public void run() {
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                writer = new PrintWriter(clientSocket.getOutputStream(), true);

                System.out.print("Enter your name: ");
                name = reader.readLine();
                writer.println("Welcome, " + name + "!");

                String message;
                while ((message = reader.readLine()) != null) {
                    if (message.startsWith("/private")) {
                        String[] parts = message.split(" ", 2);
                        String recipient = parts[1];
                        setPrivateRecipient(recipient);
                        writer.println("Chatting privately with " + recipient);
                    } else if (message.equals("/offprivate")) {
                        setPrivateRecipient(null);
                        writer.println("Chatting with everyone");
                    } else {
                        if (isInPrivateMode()) {
                            sendPrivateMessage(name, privateRecipient, message);
                        } else {
                            broadcastMessage(name + ": " + message);
                        }
                    }
                }

                clients.remove(this);
                writer.close();
                reader.close();
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void broadcastMessage(String message) {
            for (ClientHandler client : clients) {
                client.writer.println(message);
            }
        }

        private void sendPrivateMessage(String sender, String recipient, String message) {
            for (ClientHandler client : clients) {
                if (client.name.equals(recipient)) {
                    client.writer.println("[Private from " + sender + "]: " + message);
                }
            }
        }

        private synchronized boolean isInPrivateMode() {
            return privateRecipient != null;
        }

        private synchronized void setPrivateRecipient(String recipient) {
            privateRecipient = recipient;
        }
    }
}

