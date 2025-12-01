import gui.GUI;

import java.io.IOException;
import java.net.ServerSocket;

public class Main {
    private static final int PORT = 65432;

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Application started. No other instance detected.");

            javax.swing.SwingUtilities.invokeLater(() -> {
                new GUI().setVisible(true);
            });
            
            while (true) {
                try {
                    serverSocket.accept();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        } catch (IOException e) {
            System.out.println("Another instance is already running!");
            System.exit(0);
        }
    }
}
