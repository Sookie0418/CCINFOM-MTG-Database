import java.sql.*;

public class MTGDatabaseDriver {
    public static void main(String[] args) {
        Connection conn = null;
        Statement state = null;
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection(
                    "jdbc:mysql://127.0.0.1:3306/mtg_commander_db",
                    "root",
                    "password");

            state = conn.createStatement();
            ResultSet resultSet = state.executeQuery("SELECT * FROM player");

            while (resultSet.next()) {
                System.out.println("Player ID: " + resultSet.getString("player_id"));
                System.out.println("First Name: " + resultSet.getString("first_name"));
                System.out.println("Last Name: " + resultSet.getString("last_name"));
                System.out.println("City: " + resultSet.getString("city_address"));
                System.out.println("Age: " + resultSet.getString("age"));
                System.out.println("-----------------------");
            }

        } catch (SQLException e) {
            System.err.println("Database error: " + e.getMessage());
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}