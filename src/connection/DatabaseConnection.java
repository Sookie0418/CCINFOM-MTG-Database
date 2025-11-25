//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package connection;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    private static final String URL = "jdbc:mysql://127.0.0.1:3306/mtg_commander_db?useSSL=false&allowPublicKeyRetrieval=true";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "January122006";
    private static Connection sharedConnection = null;

    public static Connection getConnection() {
        return sharedConnection;
    }

    public static boolean testConnection() {
        try {
            return sharedConnection != null && !sharedConnection.isClosed();
        } catch (SQLException var1) {
            return false;
        }
    }

    public static void closeConnection() {
        try {
            if (sharedConnection != null && !sharedConnection.isClosed()) {
                sharedConnection.close();
                System.out.println("Database connection closed.");
            }
        } catch (SQLException var1) {
            System.err.println("Error closing connection: " + var1.getMessage());
        }

    }

    static {
        initializeConnection();
    }

    private static void initializeConnection() {
        try {
            // Test if driver is available
            Class.forName("com.mysql.cj.jdbc.Driver");
            System.out.println("✓ MySQL JDBC Driver loaded successfully");

            // Test connection
            sharedConnection = DriverManager.getConnection(
                    "jdbc:mysql://127.0.0.1:3306/mtg_commander_db?serverTimezone=UTC&useSSL=false",
                    "root",
                    "January122006"
            );
            System.out.println("✓ Database connection established successfully");

        } catch (ClassNotFoundException e) {
            System.err.println("✗ MySQL JDBC Driver not found in classpath");
            System.err.println("Classpath: " + System.getProperty("java.class.path"));
            sharedConnection = null;
        } catch (SQLException e) {
            System.err.println("✗ Database connection failed: " + e.getMessage());
            sharedConnection = null;
        }
    }


}
