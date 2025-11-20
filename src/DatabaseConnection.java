import java.sql.*;

public class DatabaseConnection {
    private static final String URL = "jdbc:mysql://127.0.0.1:3306/mtg_commander_db?serverTimezone=UTC&useSSL=false";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "password";

    private static Connection sharedConnection = null;

    /**
     * Initializes the shared database connection upon class loading.
     */
    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            System.out.println("JDBC Driver loaded successfully.");
            sharedConnection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
            System.out.println("Database connection established successfully.");
        } catch (ClassNotFoundException e) {
            System.err.println("FATAL: MySQL JDBC driver not found. Check your classpath (the -cp argument).");
        } catch (SQLException e) {
            System.err.println("FATAL: Database connection failed. Check your MySQL server status, username, and password.");
            System.err.println("SQL Error: " + e.getMessage());
        }
    }

    /**
     * Provides the single shared connection instance.
     * @return The active Connection object, or null if connection failed at startup.
     */
    public static Connection getConnection() {
        return sharedConnection;
    }

    /**
     * Tests if the shared connection is currently valid.
     */
    public static boolean testConnection() {
        try {
            // Check if the connection exists and if open
            return sharedConnection != null && !sharedConnection.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }

    /**
     * Closes the shared database connection (call this before exiting the application).
     */
    public static void closeConnection() {
        try {
            if (sharedConnection != null && !sharedConnection.isClosed()) {
                sharedConnection.close();
                System.out.println("Database connection closed.");
            }
        } catch (SQLException e) {
            System.err.println("Error closing connection: " + e.getMessage());
        }
    }
}