import controller.*;
import gui.*;
import javax.swing.SwingUtilities;
import java.util.Scanner;

/**
 * Driver class to instantiate the Controller and launch the Swing GUI.
 * This class now launches the Login screen first.
 */
public class MTGDatabaseDriver {
    private static MTGDatabaseController controller;
    private static Scanner scanner;

    public static void main(String[] args) {
        scanner = new Scanner(System.in);
        // 1. Instantiate the Controller (which initializes the database)
        controller = new MTGDatabaseController();

        System.out.println("==========================================");
        System.out.println("    MTG Commander Database System");
        System.out.println("==========================================");

        // 2. Launch the Login GUI on the Event Dispatch Thread (EDT)
        SwingUtilities.invokeLater(() -> {
            new LoginGUI(controller);
        });
    }
}