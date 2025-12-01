import gui.GUI;

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.channels.FileLock;

public class Main {
    public static void main(String[] args) throws Exception {
        RandomAccessFile file = new RandomAccessFile(new File("app.lock"), "rw");
        FileLock lock = file.getChannel().tryLock();
        if (lock == null) {
            System.out.println("Another instance is running!");
            System.exit(0);
        }

        // Run your GUI
        javax.swing.SwingUtilities.invokeLater(() -> {
            new GUI().setVisible(true);
        });
        
    }
}
