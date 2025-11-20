import java.util.Scanner;

/**
 * Utility class containing the command-line interface (CLI) logic for 
 * player registration. This class is designed to be called by the 
 * MTGDatabaseDriver.
 * NOTE: Does not contain a main method.
 */
public class RegisterUser {

    /**
     * Guides the user through the input process to register a new player.
     * @param transactions An initialized PlayerTransactions object.
     * @param scanner The application's main Scanner object for input.
     */
    public static boolean promptAndRegister(PlayerTransactions transactions, Scanner scanner) {
        System.out.println("\n--- New Player Entry ---");
        
        // Get First Name
        System.out.print("Enter First Name: ");
        String firstName = scanner.nextLine().trim();
        if (firstName.isEmpty()) {
            System.out.println("Registration cancelled: First Name cannot be empty.");
            return false;
        }

        // Get Last Name
        System.out.print("Enter Last Name: ");
        String lastName = scanner.nextLine().trim();
        if (lastName.isEmpty()) {
            System.out.println("Registration cancelled: Last Name cannot be empty.");
            return false;
        }

        // Get City Address (Optional)
        System.out.print("Enter City Address (Optional): ");
        String cityAddress = scanner.nextLine().trim();
        
        // Get Age
        int age = -1;
        while (age < 0) {
            System.out.print("Enter Age (must be a positive number): ");
            try {
                // Check if the user wants to exit during input
                String ageInput = scanner.nextLine().trim();
                if (ageInput.equalsIgnoreCase("cancel")) {
                    System.out.println("Registration cancelled.");
                    return false;
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

        Player newPlayer = new Player(firstName, lastName, cityAddress, age);

        int newId = transactions.addPlayer(newPlayer);
        
        if (newId != -1) {
            System.out.println("\nSUCCESS: Player Registered!");
            System.out.println("Name: " + newPlayer.getFirstName() + " " + newPlayer.getLastName());
            System.out.println("Assigned Player ID: " + newId);
        } else {
            System.out.println("\nFAILURE: Could not register player. Check console for details (e.g., UNIQUE constraint violation).");
        }
        return true;
    }
}