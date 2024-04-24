import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    private static ExecutorService executor = Executors.newFixedThreadPool(10);

    public static void main(String[] args) {
        int port = 12345;

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server is listening on port " + port);

            while (true) {
                Socket socket = serverSocket.accept();
                executor.execute(new ServerThread(socket));
            }
        } catch (IOException ex) {
            System.err.println("Server exception: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
}

class ServerThread extends Thread {
    private Socket socket;
    private PrintWriter logWriter;

    public ServerThread(Socket socket) {
        this.socket = socket;
        try {
            // Set up logging to a file
            logWriter = new PrintWriter(new FileWriter("server.log", true), true);
        } catch (IOException ex) {
            System.err.println("Could not set up logger: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    public void run() {
        try {
            InputStream input = socket.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(input));
            OutputStream output = socket.getOutputStream();
            PrintWriter writer = new PrintWriter(output, true);

            String text;

            do {
                text = reader.readLine();
                logWriter.println("Received at " + new Date() + ": " + text);
                String response = executeCommand(text);
                writer.println(response);

            } while (!text.equalsIgnoreCase("bye"));

            socket.close();
        } catch (IOException ex) {
            logWriter.println("Error with client connection: " + ex.getMessage());
        } finally {
            logWriter.close();
        }
    }

    private String executeCommand(String text) {
        // Actual command execution logic
        if (text.startsWith("echo ")) {
            return "Echo: " + text.substring(5);
        } else if (text.startsWith("upper ")) {
            return text.substring(6).toUpperCase();
        } else if (text.startsWith("reverse ")) {
            return new StringBuilder(text.substring(8)).reverse().toString();
        } else if (text.startsWith("count ")) {
            String message = text.substring(6); // Get the message part after "count "
            int count = message.split(" ").length; // Split the message into words and count the number of words
            return "Word count: " + count; // Return the word count
        } else {
            return "Unknown command";
        }
    }
}
