package transactions;
import connection.*;
import entity.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PlayerTransactions {

    
    public final DatabaseConnection dbConnection; 

    public PlayerTransactions() {
        // Initialize the connection when the Transactions object is created
        this.dbConnection = new DatabaseConnection();
    }

    /**
     * Inserts a new Player record into the 'player' table.
     * @param player The Player object to insert.
     * @return The generated player_id, or -1 if the insertion failed.
     */
    public int addPlayer(Player player) {
        if (!dbConnection.testConnection()) {
            System.err.println("Transaction failed: Database connection is not open.");
            return -1;
        }
        
        String sql = "INSERT INTO player (first_name, last_name, city_address, age) VALUES (?, ?, ?, ?)";
        int generatedId = -1;
        Connection conn = dbConnection.getConnection();

        try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, player.getFirstName());
            pstmt.setString(2, player.getLastName());
            pstmt.setString(3, player.getCityAddress());
            pstmt.setInt(4, player.getAge());

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        generatedId = rs.getInt(1);
                        player.setPlayerId(generatedId);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error adding player: " + e.getMessage());
        }
        return generatedId;
    }

    /**
     * Retrieves a Player record by their ID.
     * @param playerId The ID of the player to retrieve.
     * @return The Player object, or null if not found.
     */
    public Player getPlayerById(int playerId) {
        Connection conn = dbConnection.getConnection();
        if (!dbConnection.testConnection()) return null;

        String sql = "SELECT player_id, first_name, last_name, city_address, age FROM player WHERE player_id = ?";
        Player player = null;

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, playerId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    player = new Player();
                    player.setPlayerId(rs.getInt("player_id"));
                    player.setFirstName(rs.getString("first_name"));
                    player.setLastName(rs.getString("last_name"));
                    player.setCityAddress(rs.getString("city_address"));
                    player.setAge(rs.getInt("age"));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting player: " + e.getMessage());
        }
        return player;
    }

    /**
     * Retrieves all Player records from the 'player' table.
     * @return A list of Player objects.
     */
    public List<Player> getAllPlayers() {
        Connection conn = dbConnection.getConnection();
        if (!dbConnection.testConnection()) return new ArrayList<>();
        
        String sql = "SELECT player_id, first_name, last_name, city_address, age FROM player";
        List<Player> playerList = new ArrayList<>();

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Player player = new Player();
                player.setPlayerId(rs.getInt("player_id"));
                player.setFirstName(rs.getString("first_name"));
                player.setLastName(rs.getString("last_name"));
                player.setCityAddress(rs.getString("city_address"));
                player.setAge(rs.getInt("age"));
                playerList.add(player);
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving all players: " + e.getMessage());
        }
        return playerList;
    }
    
    /**
     * Updates an existing Player record in the 'player' table.
     * @param player The Player object with the updated details. Must have a valid ID.
     * @return true if the update was successful, false otherwise.
     */
    public boolean updatePlayer(Player player) {
        Connection conn = dbConnection.getConnection();
        if (!dbConnection.testConnection()) return false;
        
        String sql = "UPDATE player SET first_name = ?, last_name = ?, city_address = ?, age = ? WHERE player_id = ?";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, player.getFirstName());
            pstmt.setString(2, player.getLastName());
            pstmt.setString(3, player.getCityAddress());
            pstmt.setInt(4, player.getAge());
            pstmt.setInt(5, player.getPlayerId());

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
            
        } catch (SQLException e) {
            System.err.println("Error updating player: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Deletes a Player record by their ID.
     * @param playerId The ID of the player to delete.
     * @return true if the deletion was successful, false otherwise.
     */
    public boolean deletePlayer(int playerId) {
        Connection conn = dbConnection.getConnection();
        if (!dbConnection.testConnection()) return false;
        
        String sql = "DELETE FROM player WHERE player_id = ?";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, playerId);
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
            
        } catch (SQLException e) {
            System.err.println("Error deleting player: " + e.getMessage());
            return false;
        }
    }
    
    // Method to close the database connection when the transactions object is done
    public void close() {
        dbConnection.closeConnection();
    }
}