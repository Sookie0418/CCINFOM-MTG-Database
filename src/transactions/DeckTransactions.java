package transactions;
import connection.*;
import entity.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DeckTransactions {

    private final DatabaseConnection dbConnection;

    public DeckTransactions() {
        this.dbConnection = new DatabaseConnection();
    }

    /**
     * Creates a new deck header.
     * Based on "Create and validate deck" transaction.
     */
    public int createDeck(String deckName, int playerId, String bracketInfo, String description) {
        Connection conn = dbConnection.getConnection();
        String sql = "INSERT INTO deck (deck_name, player_id, bracket_info, description, validity) VALUES (?, ?, ?, ?, 'Invalid')";
        int generatedId = -1;

        try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, deckName);
            pstmt.setInt(2, playerId);
            pstmt.setString(3, bracketInfo);
            pstmt.setString(4, description);

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) generatedId = rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error creating deck: " + e.getMessage());
        }
        return generatedId; // Returns the new Deck ID
    }

    /**
     * Adds a card to a specific deck.
     */
    public boolean addCardToDeck(int deckId, int cardId, int quantity, boolean isCommander) {
        Connection conn = dbConnection.getConnection();
        String sql = "INSERT INTO deck_cards (deck_id, card_id, quantity, is_commander) VALUES (?, ?, ?, ?)";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, deckId);
            pstmt.setInt(2, cardId);
            pstmt.setInt(3, quantity);
            pstmt.setBoolean(4, isCommander);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error adding card to deck: " + e.getMessage());
            return false;
        }
    }

    /**
     * CRITICAL FUNCTION: Validates the deck.
     * 1. Checks if total number of cards is 100.
     * 2. Checks for banned cards.
     * 3. Updates the 'validity' column in the deck table.
     */
    public String validateDeck(int deckId) {
        Connection conn = dbConnection.getConnection();
        int totalCards = 0;
        int bannedCount = 0;

        // 1. Count total cards
        String countSql = "SELECT SUM(quantity) FROM deck_cards WHERE deck_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(countSql)) {
            pstmt.setInt(1, deckId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) totalCards = rs.getInt(1);
        } catch (SQLException e) { return "Error during validation: " + e.getMessage(); }

        // 2. Check for banned cards
        String banSql = "SELECT COUNT(*) FROM deck_cards dc JOIN card c ON dc.card_id = c.card_id WHERE dc.deck_id = ? AND c.card_status = 'Banned'";
        try (PreparedStatement pstmt = conn.prepareStatement(banSql)) {
            pstmt.setInt(1, deckId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) bannedCount = rs.getInt(1);
        } catch (SQLException e) { return "Error during validation: " + e.getMessage(); }

        // 3. Update Status
        boolean isValid = (totalCards == 100 && bannedCount == 0);
        String updateSql = "UPDATE deck SET validity = ? WHERE deck_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(updateSql)) {
            pstmt.setString(1, isValid ? "Valid" : "Invalid");
            pstmt.setInt(2, deckId);
            pstmt.executeUpdate();
        } catch (SQLException e) { return "Error updating status"; }

        if (isValid) return "Deck is VALID.";
        return "Deck is INVALID. Card Count: " + totalCards + " (Required: 100). Banned Cards: " + bannedCount;
    }
}
