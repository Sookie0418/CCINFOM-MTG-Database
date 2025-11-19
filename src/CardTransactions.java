import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CardTransactions {

    private final DatabaseConnection dbConnection;

    public CardTransactions() {
        this.dbConnection = new DatabaseConnection();
    }

    /**
     * Retrieves a card by its specific ID.
     * Used during Deck Construction to verify card details.
     */
    public Card getCardById(int cardId) {
        Connection conn = dbConnection.getConnection();
        if (!dbConnection.testConnection()) return null;

        String sql = "SELECT * FROM card WHERE card_id = ?";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, cardId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    // Uses your provided Factory to determine if it's a Creature, Land, etc.
                    return CardFactory.createCardFromResultSet(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting card: " + e.getMessage());
        }
        return null;
    }

    /**
     * Searches for cards by name.
     * Essential for the "Card Record Management" view defined in the PDF.
     */
    public List<Card> searchCardsByName(String nameQuery) {
        List<Card> cards = new ArrayList<>();
        Connection conn = dbConnection.getConnection();
        String sql = "SELECT * FROM card WHERE card_name LIKE ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, "%" + nameQuery + "%");
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    cards.add(CardFactory.createCardFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error searching cards: " + e.getMessage());
        }
        return cards;
    }
    
    public void close() {
        dbConnection.closeConnection();
    }
}
