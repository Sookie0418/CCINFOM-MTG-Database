import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles all database transactions (CRUD) for the Card entity using JDBC and MySQL.
 */
public class CardTransactions {

    /**
     * Retrieves all cards from the database.
     */
    public List<Record> getAllCards() throws SQLException {
        String sql = "SELECT card_id, card_name, card_mana_cost, card_type, card_subtype, " +
                "card_power, card_toughness, card_text, card_edition, card_status FROM card ORDER BY card_name;";
        List<Record> cards = new ArrayList<>();

        Connection conn = DatabaseConnection.getConnection(); // Get the shared connection

        // FIX: Removed conn from try block to prevent premature closing
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (conn == null) {
                throw new SQLException("Failed to establish database connection.");
            }

            while (rs.next()) {
                int id = rs.getInt("card_id");
                String name = rs.getString("card_name");
                String manaCost = rs.getString("card_mana_cost");
                String type = rs.getString("card_type");
                String subtype = rs.getString("card_subtype");
                String power = rs.getString("card_power");
                String toughness = rs.getString("card_toughness");
                String text = rs.getString("card_text");
                String edition = rs.getString("card_edition");
                String status = rs.getString("card_status");

                cards.add(new Record(id, name, manaCost, type, subtype, power, toughness, text, edition, status));
            }
        }
        return cards;
    }

    /**
     * Adds a new card record to the database.
     */
    public void addCard(String name, String manaCost, String type, String subtype,
                        String power, String toughness, String text, String edition, String status) throws SQLException {

        String sql = "INSERT INTO card (card_name, card_mana_cost, card_type, card_subtype, " +
                "card_power, card_toughness, card_text, card_edition, card_status) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        Connection conn = DatabaseConnection.getConnection(); // Get the shared connection

        // FIX: Removed conn from try block to prevent premature closing
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {

            if (conn == null) {
                throw new SQLException("Failed to establish database connection.");
            }

            pstmt.setString(1, name);
            pstmt.setString(2, manaCost);
            pstmt.setString(3, type);
            pstmt.setString(4, subtype);
            pstmt.setString(5, power);
            pstmt.setString(6, toughness);
            pstmt.setString(7, text);
            pstmt.setString(8, edition);
            pstmt.setString(9, status); // ENUM field

            pstmt.executeUpdate();
        }
    }

    /**
     * Updates an existing card record in the database.
     */
    public void updateCard(int id, String name, String manaCost, String type, String subtype,
                           String power, String toughness, String text, String edition, String status) throws SQLException {

        String sql = "UPDATE card SET card_name = ?, card_mana_cost = ?, card_type = ?, card_subtype = ?, " +
                "card_power = ?, card_toughness = ?, card_text = ?, card_edition = ?, card_status = ? " +
                "WHERE card_id = ?";

        Connection conn = DatabaseConnection.getConnection(); // Get the shared connection

        // FIX: Removed conn from try block to prevent premature closing
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {

            if (conn == null) {
                throw new SQLException("Failed to establish database connection.");
            }

            pstmt.setString(1, name);
            pstmt.setString(2, manaCost);
            pstmt.setString(3, type);
            pstmt.setString(4, subtype);
            pstmt.setString(5, power);
            pstmt.setString(6, toughness);
            pstmt.setString(7, text);
            pstmt.setString(8, edition);
            pstmt.setString(9, status); // ENUM field
            pstmt.setInt(10, id);

            pstmt.executeUpdate();
        }
    }

    /**
     * Deletes a card record from the database.
     */
    public void deleteCard(int id) throws SQLException {
        String sql = "DELETE FROM card WHERE card_id = ?";

        Connection conn = DatabaseConnection.getConnection(); // Get the shared connection

        // FIX: Removed conn from try block to prevent premature closing
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {

            if (conn == null) {
                throw new SQLException("Failed to establish database connection.");
            }

            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        }
    }
}
