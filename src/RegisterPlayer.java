import java.util.Scanner;

/**
 * Command-Line Interface (CLI) application for registering new MTG Commander players.
 * This class uses the PlayerTransactions class to interact with the database.
 * * NOTE: For this to work, ensure the following are in your 'src' folder:
 * 1. Player.java
 * 2. PlayerTransactions.java
 * 3. DatabaseConnection.java (correctly configured with MySQL credentials)
 * 4. Your MySQL server is running.
 */
public class RegisterPlayer {

    public static void main(String[] args) {
        // Initialize the transactions handler, which internally initializes the database connection
        PlayerTransactions transactions = new PlayerTransactions();
        // Initialize the scanner for user input
        Scanner scanner = new Scanner(System.in);
        
        // Check if the database connection was successfully established
        if (!transactions.dbConnection.testConnection()) {
            System.err.println("FATAL ERROR: Cannot run application. Check your DatabaseConnection.java configuration and ensure MySQL is running.");
            transactions.close();
            return;
        }
        
        System.out.println("--- MTG Commander Player Registration System ---");
        System.out.println("Connected successfully to the database.");
        System.out.println("Enter 'exit' at any time to quit.");
        
        // Loop to allow continuous player registration
        while (true) {
            System.out.println("\n--- New Player Entry ---");
            
            // 1. Get First Name
            System.out.print("Enter First Name: ");
            String firstName = scanner.nextLine().trim();
            if (firstName.equalsIgnoreCase("exit")) break;
            if (firstName.isEmpty()) {
                System.out.println("First Name cannot be empty. Please try again.");
                continue;
            }

            // 2. Get Last Name
            System.out.print("Enter Last Name: ");
            String lastName = scanner.nextLine().trim();
            if (lastName.equalsIgnoreCase("exit")) break;
            if (lastName.isEmpty()) {
                System.out.println("Last Name cannot be empty. Please try again.");
                continue;
            }

            // 3. Get City Address (Optional)
            System.out.print("Enter City Address (Optional): ");
            String cityAddress = scanner.nextLine().trim();
            if (cityAddress.equalsIgnoreCase("exit")) break;
            
            // 4. Get Age
            int age = -1;
            while (age < 0) {
                System.out.print("Enter Age (must be a positive number): ");
                String ageInput = scanner.nextLine().trim();
                if (ageInput.equalsIgnoreCase("exit")) {
                    age = 0; // Set age to 0 to exit gracefully
                    break;
                }
                try {
                    age = Integer.parseInt(ageInput);
                    if (age <= 0) {
                        System.out.println("Age must be a positive number.");
                        age = -1; // Reset to loop again
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Invalid input. Please enter a valid number for age.");
                }
            }
            if (age == 0) break; // Exit requested

            // Create the Player object (uses the constructor from Player.java)
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
            
            System.out.print("\nPress Enter to register another player, or type 'exit' to quit...");
            String exitCheck = scanner.nextLine().trim();
            if (exitCheck.equalsIgnoreCase("exit")) break;
        }

        scanner.close();
        transactions.close(); // Close the database connection cleanly
        System.out.println("\nRegistration system closed. Database connection released. Goodbye!");
    }
}