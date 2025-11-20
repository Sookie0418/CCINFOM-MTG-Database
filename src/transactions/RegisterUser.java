package transactions;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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

        // Get First Name
        System.out.print("Enter username: ");
        String username = scanner.nextLine();
        if (username.isEmpty()) {
            System.out.println("Registration cancelled: Username cannot be empty.");
            return false;
        }
        if (username.contains(" ")) {
            System.out.println("Registration cancelled: Username cannot contain a space.");
            return false;
        }
        if (isUsernameTaken(username)) {
            System.out.println("Registration cancelled: Username already taken.");
            return false;
        }


        System.out.print("Enter password: ");
        String passwordTemp = scanner.nextLine().trim();

        System.out.print("Confirm password: ");
        String Password = scanner.nextLine().trim();
        if (Password.isEmpty()) {
            System.out.println("Registration cancelled: Password cannot be empty.");
            return false;
        }
        if (Password.contains(" ")) {
            System.out.println("Registration cancelled: Password cannot contain a space.");
            return false;
        }
        if (Password.length() < 8) {
            System.out.println("Registration cancelled: Password must be at least 8 characters long");
            return false;
        }
        if (!Password.equals(passwordTemp)) {
            System.out.println("Passwords do not match.");
            return false;
        }

        File file = new File("LoginInfo.txt");
        if(file.exists()) {
            try(FileWriter writer = new FileWriter("LoginInfo.txt", true)) {
                writer.write(username + " " + Password + "\n");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        else {
            try {
                if (file.createNewFile()) {
                    try(FileWriter writer = new FileWriter("LoginInfo.txt")) {
                        writer.write("admin password\n");
                        writer.write(username + " " + Password + "\n");
                    }
                }
                else {
                    System.out.println("Error creating file");
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        return true;
    }

    private static boolean isUsernameTaken(String username) {
        Path filePath = Paths.get("LoginInfo.txt");
        if (!Files.exists(filePath)) {
            return false;
        }

        try {
            return Files.lines(filePath)
                    .map(line -> line.split(" ")[0])
                    .anyMatch(existingUsername -> existingUsername.equals(username));
        } catch (IOException e) {
            System.out.println("Error checking username: " + e.getMessage());
            return true;
        }
    }
}