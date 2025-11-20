import java.sql.*;
import java.time.LocalDate;

public class BorrowTransactions {

    private final DatabaseConnection dbConnection;

    public BorrowTransactions() {
        this.dbConnection = new DatabaseConnection();
    }

    /**
     * Checks if a deck is currently available.
     * Logic: A deck is unavailable if it is in 'Approved' or 'Pending' status
     * and has not been returned yet.
     */
    public boolean isDeckAvailable(int deckId) {
        Connection conn = dbConnection.getConnection();
        String sql = "SELECT COUNT(*) FROM borrow_request WHERE deck_id = ? AND status IN ('Pending', 'Approved')";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, deckId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) == 0;
                }
            }
        } catch (SQLException e) {
            System.err.println("Availability check failed: " + e.getMessage());
        }
        return false;
    }

    /**
     * Processes a Borrow Request.
     * Ref: "Borrow a deck and check its availability".
     */
    public boolean requestBorrow(int playerId, int deckId, String borrowType) {
        if (!isDeckAvailable(deckId)) {
            System.out.println("Transaction Failed: Deck is currently unavailable.");
            return false;
        }

        Connection conn = dbConnection.getConnection();
        String sql = "INSERT INTO borrow_request (player_id, deck_id, borrow_type, request_date, status) VALUES (?, ?, ?, ?, 'Pending')";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, playerId);
            pstmt.setInt(2, deckId);
            pstmt.setString(3, borrowType);
            pstmt.setDate(4, Date.valueOf(LocalDate.now()));

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Error processing borrow request: " + e.getMessage());
            return false;
        }
    }

    /**
     * Updates a borrow request to "Returned".
     */
    public boolean returnDeck(int borrowCode) {
        Connection conn = dbConnection.getConnection();
        String sql = "UPDATE borrow_request SET status = 'Returned', return_date = ? WHERE borrow_code = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDate(1, Date.valueOf(LocalDate.now()));
            pstmt.setInt(2, borrowCode);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error returning deck: " + e.getMessage());
            return false;
        }
    }
}