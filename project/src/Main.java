import gui.GUI;
import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        // Launch the amazing GUI!
        SwingUtilities.invokeLater(() -> {
            GUI gui = new GUI();
            gui.setVisible(true);
        });
    }
}
