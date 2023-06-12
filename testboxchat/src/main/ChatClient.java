package main;
import java.io.*;
import java.net.*;
import java.util.Scanner;

public class ChatClient {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter your name: ");
        String name = scanner.nextLine();

        try {
            Socket socket = new Socket("localhost", 9999);
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);

            Thread readThread = new Thread(() -> {
                try {
                    String message;
                    while ((message = reader.readLine()) != null) {
                        System.out.println(message);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            readThread.start();

            Thread writeThread = new Thread(() -> {
                try {
                    Scanner inputScanner = new Scanner(System.in);
                    String input;
                    while (true) {
                        input = inputScanner.nextLine();
                        writer.println(input);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            writeThread.start();

            writer.println(name);

            readThread.join();
            writeThread.join();

            reader.close();
            writer.close();
            socket.close();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
