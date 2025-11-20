import java.util.Scanner;

/**
 * Utility class containing the command-line interface (CLI) logic for 
 * player registration. This class is designed to be called by the 
 * MTGDatabaseDriver.
 * NOTE: Does not contain a main method.
 */
public class RegisterPlayer {

    /**
     * Guides the user through the input process to register a new player.
     * @param transactions An initialized PlayerTransactions object.
     * @param scanner The application's main Scanner object for input.
     */
    public static void promptAndRegister(PlayerTransactions transactions, Scanner scanner) {
        System.out.println("\n--- New Player Entry ---");
        
        // 1. Get First Name
        System.out.print("Enter First Name: ");
        String firstName = scanner.nextLine().trim();
        if (firstName.isEmpty()) {
            System.out.println("Registration cancelled: First Name cannot be empty.");
            return;
        }

        // 2. Get Last Name
        System.out.print("Enter Last Name: ");
        String lastName = scanner.nextLine().trim();
        if (lastName.isEmpty()) {
            System.out.println("Registration cancelled: Last Name cannot be empty.");
            return;
        }

        // 3. Get City Address (Optional)
        System.out.print("Enter City Address (Optional): ");
        String cityAddress = scanner.nextLine().trim();
        
        // 4. Get Age
        int age = -1;
        while (age < 0) {
            System.out.print("Enter Age (must be a positive number): ");
            try {
                // Check if the user wants to exit during input
                String ageInput = scanner.nextLine().trim();
                if (ageInput.equalsIgnoreCase("cancel")) {
                    System.out.println("Registration cancelled.");
                    return;
                }
                
                age = Integer.parseInt(ageInput);
                if (age <= 0) {
                    System.out.println("Age must be a positive number.");
                    age = -1; // Reset to loop again
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a valid number for age.");
            }
        }

        // Create the Player object (uses the constructor from Player.java)
        // NOTE: Player.java file is required for this to compile.
        Player newPlayer = new Player(firstName, lastName, cityAddress, age);

        // Add player to the database
        int newId = transactions.addPlayer(newPlayer);
        
        if (newId != -1) {
            System.out.println("\nSUCCESS: Player Registered!");
            System.out.println("Name: " + newPlayer.getFirstName() + " " + newPlayer.getLastName());
            System.out.println("Assigned Player ID: " + newId);
        } else {
            System.out.println("\nFAILURE: Could not register player. Check console for details (e.g., UNIQUE constraint violation).");
        }
    }
}