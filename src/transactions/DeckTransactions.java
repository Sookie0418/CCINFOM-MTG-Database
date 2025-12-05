package transactions;
import connection.DatabaseConnection;
import entity.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DeckTransactions {

    public DeckTransactions() {
        // Constructor is fine as is
    }

    /**
     * Creates a new deck header.
     */
    public int createDeck(String deckName, int playerId, String bracketInfo, String description) throws SQLException {
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) {
            throw new SQLException("No database connection available");
        }

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
                    if (rs.next()) {
                        generatedId = rs.getInt(1);
                        System.out.println("Created deck with ID: " + generatedId);
                    }
                }
            }
        }
        return generatedId;
    }

    /**
     * Adds a card to a specific deck.
     */
    public boolean addCardToDeck(int deckId, int cardId, int quantity, boolean isCommander) throws SQLException {
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) {
            throw new SQLException("No database connection available");
        }

        // Start a transaction to ensure both operations succeed or fail together
        conn.setAutoCommit(false);

        try {
            // First, add the card to deck_cards table
            String sql = "INSERT INTO deck_cards (deck_id, card_id, quantity, is_commander) VALUES (?, ?, ?, ?) " +
                    "ON DUPLICATE KEY UPDATE quantity = quantity + ?, is_commander = ?";

            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, deckId);
                pstmt.setInt(2, cardId);
                pstmt.setInt(3, quantity);
                pstmt.setBoolean(4, isCommander);
                pstmt.setInt(5, quantity);
                pstmt.setBoolean(6, isCommander);
                pstmt.executeUpdate();
            }

            // If this card is set as commander, update the deck table
            if (isCommander) {
                String updateDeckSql = "UPDATE deck SET commander_card_id = ? WHERE deck_id = ?";
                try (PreparedStatement pstmt = conn.prepareStatement(updateDeckSql)) {
                    pstmt.setInt(1, cardId);
                    pstmt.setInt(2, deckId);
                    pstmt.executeUpdate();
                }

                // Also, set all other cards in this deck as non-commander
                String clearOtherCommandersSql = "UPDATE deck_cards SET is_commander = FALSE WHERE deck_id = ? AND card_id != ?";
                try (PreparedStatement pstmt = conn.prepareStatement(clearOtherCommandersSql)) {
                    pstmt.setInt(1, deckId);
                    pstmt.setInt(2, cardId);
                    pstmt.executeUpdate();
                }
            }

            conn.commit();
            return true;

        } catch (SQLException e) {
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(true);
        }
    }

    /**
     * Validates the deck.
     */
    public String validateDeck(int deckId) throws SQLException {
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) {
            throw new SQLException("No database connection available");
        }

        int totalCards = 0;
        int bannedCount = 0;
        boolean hasCommander = false;

        // 1. Count total cards
        String countSql = "SELECT COALESCE(SUM(quantity), 0) as total FROM deck_cards WHERE deck_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(countSql)) {
            pstmt.setInt(1, deckId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) totalCards = rs.getInt("total");
        }

        // 2. Check for banned cards
        String banSql = "SELECT COUNT(*) as banned_count FROM deck_cards dc " +
                "JOIN card c ON dc.card_id = c.card_id " +
                "WHERE dc.deck_id = ? AND c.card_status = 'Banned'";
        try (PreparedStatement pstmt = conn.prepareStatement(banSql)) {
            pstmt.setInt(1, deckId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) bannedCount = rs.getInt("banned_count");
        }

        // 3. Check for commander
        String commanderSql = "SELECT COUNT(*) as commander_count FROM deck_cards WHERE deck_id = ? AND is_commander = TRUE";
        try (PreparedStatement pstmt = conn.prepareStatement(commanderSql)) {
            pstmt.setInt(1, deckId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) hasCommander = rs.getInt("commander_count") > 0;
        }

        // 4. Update Status
        boolean isValid = (totalCards == 10 && bannedCount == 0 && hasCommander);
        String updateSql = "UPDATE deck SET validity = ? WHERE deck_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(updateSql)) {
            pstmt.setString(1, isValid ? "Valid" : "Invalid");
            pstmt.setInt(2, deckId);
            pstmt.executeUpdate();
        }

        // Return detailed validation message
        StringBuilder result = new StringBuilder();
        result.append("Deck Validation Results:\n");
        result.append("- Total Cards: ").append(totalCards).append("/100\n");
        result.append("- Banned Cards: ").append(bannedCount).append("\n");
        result.append("- Commander: ").append(hasCommander ? "Present" : "Missing").append("\n");
        result.append("- Status: ").append(isValid ? "VALID" : "INVALID");

        return result.toString();
    }

    /**
     * Retrieves all decks from the database.
     */
    public List<Deck> getAllDecks() throws SQLException {
        List<Deck> decks = new ArrayList<>();
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) {
            throw new SQLException("No database connection available");
        }

        String sql = "SELECT d.deck_id, d.deck_name, d.player_id, d.commander_card_id, d.bracket_info, d.validity, d.description " +
                "FROM deck d";

        try (PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                int deckId = rs.getInt("deck_id");
                String deckName = rs.getString("deck_name");
                int playerId = rs.getInt("player_id");

                // Use getObject to handle NULL values properly
                Object commanderIdObj = rs.getObject("commander_card_id");
                Integer commanderId = null;
                if (commanderIdObj != null) {
                    commanderId = (Integer) commanderIdObj;
                }

                String bracketInfo = rs.getString("bracket_info");
                String validity = rs.getString("validity");
                String description = rs.getString("description");

                // Create Deck object
                Deck deck = new Deck();
                deck.setDeckId(deckId);
                deck.setDeckName(deckName);
                deck.setPlayerId(playerId);
                deck.setBracketInfo(bracketInfo);
                deck.setValidity(validity);
                deck.setDescription(description);

                // Load the commander card if it exists
                if (commanderId != null && commanderId > 0) {
                    try {
                        // Load the commander card using CardFactory or your card loading logic
                        Card commanderCard = loadCardById(commanderId, conn);
                        if (commanderCard != null) {
                            deck.setCommanderCard(commanderCard);
                        }
                    } catch (Exception e) {
                        System.err.println("Error loading commander card ID " + commanderId + " for deck " + deckId + ": " + e.getMessage());
                    }
                }

                decks.add(deck);
            }
        }
        return decks;
    }

    // Helper method to load a card by ID
    private Card loadCardById(int cardId, Connection conn) throws SQLException {
        String sql = "SELECT * FROM card WHERE card_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, cardId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return CardFactory.createCardFromResultSet(rs);
            }
        }
        return null;
    }
}