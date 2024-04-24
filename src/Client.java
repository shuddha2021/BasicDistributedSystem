import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.*;
import java.net.Socket;

public class Client {
    private static JFrame frame;
    private static JTextField commandInput;
    private static JTextArea serverResponseArea;
    private static Socket socket;
    private static PrintWriter writer;

    public static void main(String[] args) {
        // Set up the GUI
        setUpGUI();
        // Establish connection to server
        connectToServer("localhost", 12345);
    }

    private static void setUpGUI() {
        frame = new JFrame("Client Application");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 400);

        // Input field for commands
        commandInput = new JTextField();
        commandInput.addActionListener(e -> {
            sendCommand(commandInput.getText());
            commandInput.setText("");
        });

        // Area for server response
        serverResponseArea = new JTextArea();
        serverResponseArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(serverResponseArea);

        // Adding components to frame
        frame.getContentPane().add(BorderLayout.SOUTH, commandInput);
        frame.getContentPane().add(BorderLayout.CENTER, scrollPane);

        // Display the window
        frame.setVisible(true);
    }

    private static void connectToServer(String hostname, int port) {
        try {
            socket = new Socket(hostname, port);
            OutputStream output = socket.getOutputStream();
            writer = new PrintWriter(output, true);
        } catch (IOException ex) {
            SwingUtilities.invokeLater(() -> {
                serverResponseArea.append("Cannot connect to server: " + ex.getMessage() + "\n");
            });
        }
    }

    private static void sendCommand(String command) {
        if (command.equalsIgnoreCase("bye")) {
            try {
                socket.close();
                SwingUtilities.invokeLater(() -> {
                    serverResponseArea.append("Connection closed.\n");
                });
            } catch (IOException ex) {
                SwingUtilities.invokeLater(() -> {
                    serverResponseArea.append("Error closing the connection: " + ex.getMessage() + "\n");
                });
            }
        } else {
            writer.println(command);
            InputStream input;
            try {
                input = socket.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(input));
                String response = reader.readLine();
                System.out.println("Received from server: " + response); // Debug statement
                SwingUtilities.invokeLater(() -> {
                    System.out.println("Updating text area with: " + response); // Debug statement
                    serverResponseArea.append("Server response: " + response + "\n");
                });
            } catch (IOException ex) {
                SwingUtilities.invokeLater(() -> {
                    serverResponseArea.append("Error reading from server: " + ex.getMessage() + "\n");
                });
            }
        }
    }
}