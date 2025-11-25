package reports;

import connection.DatabaseConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Generates various reports for the MTG Commander Database System
 */
public class ReportGenerator {
    
    private final DatabaseConnection dbConnection;
    
    public ReportGenerator() {
        this.dbConnection = new DatabaseConnection();
    }
    
    /**
     * Player Borrowing Statistics Report
     * Summarizes each player's borrowing activity within a selected period
     */
    public List<PlayerBorrowingStats> generatePlayerBorrowingStats(LocalDate startDate, LocalDate endDate) {
        List<PlayerBorrowingStats> stats = new ArrayList<>();
        Connection conn = dbConnection.getConnection();
        
        String sql = """
            SELECT 
                p.player_id,
                p.first_name,
                p.last_name,
                COUNT(br.borrow_code) as total_borrows,
                AVG(DATEDIFF(COALESCE(br.return_date, CURDATE()), br.request_date)) as avg_duration,
                SUM(CASE WHEN br.return_date IS NULL AND br.due_date < CURDATE() THEN 1 ELSE 0 END) as overdue_count
            FROM player p
            LEFT JOIN borrow_request br ON p.player_id = br.player_id 
                AND br.request_date BETWEEN ? AND ?
            GROUP BY p.player_id, p.first_name, p.last_name
            ORDER BY total_borrows DESC, overdue_count DESC
            """;
            
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDate(1, java.sql.Date.valueOf(startDate));
            pstmt.setDate(2, java.sql.Date.valueOf(endDate));
            
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                PlayerBorrowingStats stat = new PlayerBorrowingStats(
                    rs.getInt("player_id"),
                    rs.getString("first_name"),
                    rs.getString("last_name"),
                    rs.getInt("total_borrows"),
                    rs.getDouble("avg_duration"),
                    rs.getInt("overdue_count")
                );
                stats.add(stat);
            }
        } catch (SQLException e) {
            System.err.println("Error generating player borrowing stats: " + e.getMessage());
        }
        
        return stats;
    }
    
    /**
     * Card Usage Frequency Report
     * Displays frequency of specific cards appearing across player decks
     */
    public List<CardUsageStats> generateCardUsageFrequency() {
        List<CardUsageStats> stats = new ArrayList<>();
        Connection conn = dbConnection.getConnection();
        
        String sql = """
            SELECT 
                c.card_id,
                c.card_name,
                c.card_type,
                c.card_mana_cost,
                COUNT(dc.deck_id) as deck_count,
                SUM(dc.quantity) as total_copies
            FROM card c
            LEFT JOIN deck_cards dc ON c.card_id = dc.card_id
            GROUP BY c.card_id, c.card_name, c.card_type, c.card_mana_cost
            ORDER BY deck_count DESC, total_copies DESC
            LIMIT 100  -- Top 100 most used cards
            """;
            
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                CardUsageStats stat = new CardUsageStats(
                    rs.getInt("card_id"),
                    rs.getString("card_name"),
                    rs.getString("card_type"),
                    rs.getString("card_mana_cost"),
                    rs.getInt("deck_count"),
                    rs.getInt("total_copies")
                );
                stats.add(stat);
            }
        } catch (SQLException e) {
            System.err.println("Error generating card usage stats: " + e.getMessage());
        }
        
        return stats;
    }
    
    /**
     * Borrow Activity Report
     * Tracks borrowing transactions over a specified period
     */
    public List<BorrowActivity> generateBorrowActivity(LocalDate startDate, LocalDate endDate) {
        List<BorrowActivity> activities = new ArrayList<>();
        Connection conn = dbConnection.getConnection();
        
        String sql = """
            SELECT 
                br.borrow_code,
                p.player_id,
                p.first_name,
                p.last_name,
                d.deck_id,
                d.deck_name,
                br.borrow_type,
                br.request_date,
                br.return_date,
                br.due_date,
                br.status,
                DATEDIFF(COALESCE(br.return_date, CURDATE()), br.request_date) as duration_days
            FROM borrow_request br
            JOIN player p ON br.player_id = p.player_id
            JOIN deck d ON br.deck_id = d.deck_id
            WHERE br.request_date BETWEEN ? AND ?
            ORDER BY br.request_date DESC
            """;
            
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDate(1, java.sql.Date.valueOf(startDate));
            pstmt.setDate(2, java.sql.Date.valueOf(endDate));
            
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                BorrowActivity activity = new BorrowActivity(
                    rs.getInt("borrow_code"),
                    rs.getInt("player_id"),
                    rs.getString("first_name"),
                    rs.getString("last_name"),
                    rs.getInt("deck_id"),
                    rs.getString("deck_name"),
                    rs.getString("borrow_type"),
                    rs.getDate("request_date") != null ? rs.getDate("request_date").toLocalDate() : null,
                    rs.getDate("return_date") != null ? rs.getDate("return_date").toLocalDate() : null,
                    rs.getDate("due_date") != null ? rs.getDate("due_date").toLocalDate() : null,
                    rs.getString("status"),
                    rs.getInt("duration_days")
                );
                activities.add(activity);
            }
        } catch (SQLException e) {
            System.err.println("Error generating borrow activity: " + e.getMessage());
        }
        
        return activities;
    }
    
    /**
     * Deck Usage Frequency Report
     * Shows how often each deck is used/borrowed within timeframe
     */
    public List<DeckUsageStats> generateDeckUsageFrequency(LocalDate startDate, LocalDate endDate) {
        List<DeckUsageStats> stats = new ArrayList<>();
        Connection conn = dbConnection.getConnection();
        
        String sql = """
            SELECT 
                d.deck_id,
                d.deck_name,
                p.first_name as owner_first_name,
                p.last_name as owner_last_name,
                d.bracket_info,
                d.validity,
                COUNT(br.borrow_code) as borrow_count,
                AVG(DATEDIFF(COALESCE(br.return_date, CURDATE()), br.request_date)) as avg_borrow_duration,
                MAX(br.request_date) as last_borrowed
            FROM deck d
            JOIN player p ON d.player_id = p.player_id
            LEFT JOIN borrow_request br ON d.deck_id = br.deck_id 
                AND br.request_date BETWEEN ? AND ?
            GROUP BY d.deck_id, d.deck_name, p.first_name, p.last_name, d.bracket_info, d.validity
            ORDER BY borrow_count DESC, avg_borrow_duration DESC
            """;
            
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDate(1, java.sql.Date.valueOf(startDate));
            pstmt.setDate(2, java.sql.Date.valueOf(endDate));
            
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                DeckUsageStats stat = new DeckUsageStats(
                    rs.getInt("deck_id"),
                    rs.getString("deck_name"),
                    rs.getString("owner_first_name"),
                    rs.getString("owner_last_name"),
                    rs.getString("bracket_info"),
                    rs.getString("validity"),
                    rs.getInt("borrow_count"),
                    rs.getDouble("avg_borrow_duration"),
                    rs.getDate("last_borrowed") != null ? rs.getDate("last_borrowed").toLocalDate() : null
                );
                stats.add(stat);
            }
        } catch (SQLException e) {
            System.err.println("Error generating deck usage stats: " + e.getMessage());
        }
        
        return stats;
    }
    
    /**
     * Summary statistics for dashboard
     */
    public Map<String, Object> generateSummaryStats() {
        Map<String, Object> summary = new HashMap<>();
        Connection conn = dbConnection.getConnection();
        
        try {
            // Total players
            String playerSql = "SELECT COUNT(*) FROM player";
            try (PreparedStatement pstmt = conn.prepareStatement(playerSql)) {
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) summary.put("totalPlayers", rs.getInt(1));
            }
            
            // Total decks
            String deckSql = "SELECT COUNT(*) FROM deck";
            try (PreparedStatement pstmt = conn.prepareStatement(deckSql)) {
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) summary.put("totalDecks", rs.getInt(1));
            }
            
            // Total cards
            String cardSql = "SELECT COUNT(*) FROM card";
            try (PreparedStatement pstmt = conn.prepareStatement(cardSql)) {
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) summary.put("totalCards", rs.getInt(1));
            }
            
            // Active borrows this month
            String borrowSql = "SELECT COUNT(*) FROM borrow_request WHERE MONTH(request_date) = MONTH(CURDATE()) AND YEAR(request_date) = YEAR(CURDATE())";
            try (PreparedStatement pstmt = conn.prepareStatement(borrowSql)) {
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) summary.put("monthlyBorrows", rs.getInt(1));
            }
            
            // Overdue borrows
            String overdueSql = "SELECT COUNT(*) FROM borrow_request WHERE return_date IS NULL AND due_date < CURDATE()";
            try (PreparedStatement pstmt = conn.prepareStatement(overdueSql)) {
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) summary.put("overdueBorrows", rs.getInt(1));
            }
            
        } catch (SQLException e) {
            System.err.println("Error generating summary stats: " + e.getMessage());
        }
        
        return summary;
    }
}