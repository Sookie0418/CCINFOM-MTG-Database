//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package controller;

import connection.DatabaseConnection;
import entity.BorrowRequest;
import entity.Deck;
import entity.Player;
import entity.Record;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import transactions.BorrowTransactions;
import transactions.CardTransactions;
import transactions.DeckTransactions;
import transactions.PlayerTransactions;

public class MTGDatabaseController {
    private PlayerTransactions playerTransactions;
    private DeckTransactions deckTransactions;
    private CardTransactions cardTransactions;
    private BorrowTransactions borrowTransactions;

    public MTGDatabaseController() {
        this.initializeDatabaseSchema();
        this.cardTransactions = new CardTransactions();
        this.playerTransactions = new PlayerTransactions();
        this.deckTransactions = new DeckTransactions();
        this.borrowTransactions = new BorrowTransactions();
    }

    public boolean validateUser(String var1, String var2) {
        Path var3 = Paths.get("LoginInfo.txt");
        if (!Files.exists(var3, new LinkOption[0])) {
            System.out.println("Login file not found.");
            return false;
        } else {
            try {
                boolean var4 = Files.lines(var3).map((var0) -> var0.split(" ")).filter((var0) -> var0.length >= 2).anyMatch((var2x) -> var2x[0].equals(var1) && var2x[1].equals(var2));
                if (!var4) {
                    System.out.println("Invalid username or password.");
                }

                return var4;
            } catch (IOException var5) {
                System.out.println("Error reading login file: " + var5.getMessage());
                return false;
            }
        }
    }

    private void initializeDatabaseSchema() {
        String var1 = "CREATE TABLE IF NOT EXISTS player (    player_id INT AUTO_INCREMENT PRIMARY KEY,    first_name VARCHAR(50) NOT NULL,    last_name VARCHAR(50) NOT NULL,    city_address VARCHAR(100),    age INT,    UNIQUE (first_name, last_name));CREATE TABLE IF NOT EXISTS card (    card_id INT AUTO_INCREMENT PRIMARY KEY,    card_name VARCHAR(100) NOT NULL,    card_mana_cost VARCHAR(50),    card_type VARCHAR(50),    card_subtype VARCHAR(50),    card_power VARCHAR(10),    card_toughness VARCHAR(10),    card_text TEXT,    card_edition VARCHAR(50),    card_status ENUM('Legal', 'Banned', 'Game Changer') DEFAULT 'Legal');CREATE TABLE IF NOT EXISTS deck (    deck_id INT AUTO_INCREMENT PRIMARY KEY,    deck_name VARCHAR(100) NOT NULL,    player_id INT NOT NULL,    commander_card_id INT,    bracket_info VARCHAR(50),    mana_base VARCHAR(100),    salt_score DECIMAL(4,2),    validity ENUM('Valid', 'Invalid') DEFAULT 'Valid',    description TEXT,    FOREIGN KEY (player_id) REFERENCES player(player_id)        ON DELETE CASCADE        ON UPDATE CASCADE,    FOREIGN KEY (commander_card_id) REFERENCES card(card_id)        ON DELETE SET NULL        ON UPDATE CASCADE);CREATE TABLE IF NOT EXISTS borrow_request (    borrow_code INT AUTO_INCREMENT PRIMARY KEY,    player_id INT NOT NULL,    deck_id INT NOT NULL,    borrow_type ENUM('Wait', 'Immediate') DEFAULT 'Immediate',    request_date DATE NOT NULL,    due_date DATE,    return_date DATE,    status ENUM('Pending', 'Approved', 'Returned', 'Overdue', 'Cancelled') DEFAULT 'Pending',    FOREIGN KEY (player_id) REFERENCES player(player_id)        ON DELETE CASCADE        ON UPDATE CASCADE,    FOREIGN KEY (deck_id) REFERENCES deck(deck_id)        ON DELETE CASCADE        ON UPDATE CASCADE);";
        Connection var2 = DatabaseConnection.getConnection();

        try (Statement var3 = var2.createStatement()) {
            if (var2 != null) {
                String[] var4 = var1.split(";");

                for(String var8 : var4) {
                    if (!var8.trim().isEmpty()) {
                        var3.addBatch(var8);
                    }
                }

                var3.executeBatch();
                System.out.println("All primary tables initialized successfully (MySQL).");
            }
        } catch (SQLException var11) {
            System.err.println("Database table initialization failed. Check MySQL status and credentials: " + var11.getMessage());
        }

    }

    public List<Record> getAllCards() throws SQLException {
        return this.cardTransactions.getAllCards();
    }

    public void addCard(String var1, String var2, String var3, String var4, String var5, String var6, String var7, String var8, String var9) throws SQLException {
        this.cardTransactions.addCard(var1, var2, var3, var4, var5, var6, var7, var8, var9);
    }

    public void updateCard(int var1, String var2, String var3, String var4, String var5, String var6, String var7, String var8, String var9, String var10) throws SQLException {
        this.cardTransactions.updateCard(var1, var2, var3, var4, var5, var6, var7, var8, var9, var10);
    }

    public void deleteCard(int var1) throws SQLException {
        this.cardTransactions.deleteCard(var1);
    }

    public List<Player> getAllPlayers() throws SQLException {
        return this.playerTransactions.getAllPlayers();
    }

    public int addPlayer(Player var1) throws SQLException {
        return this.playerTransactions.addPlayer(var1);
    }

    public boolean updatePlayer(Player var1) throws SQLException {
        return this.playerTransactions.updatePlayer(var1);
    }

    public boolean deletePlayer(int var1) throws SQLException {
        return this.playerTransactions.deletePlayer(var1);
    }

    public List<Deck> getAllDecks() throws SQLException {
        return this.deckTransactions.getAllDecks();
    }

    public int createDeck(String var1, int var2, String var3, String var4) throws SQLException {
        return this.deckTransactions.createDeck(var1, var2, var3, var4);
    }

    public boolean addCardToDeck(int deckId, int cardId, int quantity, boolean isCommander) throws SQLException {
        return this.deckTransactions.addCardToDeck(deckId, cardId, quantity, isCommander);
    }

    public String validateDeck(int var1) throws SQLException {
        return this.deckTransactions.validateDeck(var1);
    }

    public List<BorrowRequest> getAllBorrowRequests() throws SQLException {
        return this.borrowTransactions.getAllBorrowRequests();
    }

    public boolean isDeckAvailable(int var1) throws SQLException {
        return this.borrowTransactions.isDeckAvailable(var1);
    }

    public boolean requestBorrow(int var1, int var2) throws SQLException {
        return this.borrowTransactions.requestBorrow(var1, var2);
    }

    public boolean returnDeck(int var1) throws SQLException {
        return this.borrowTransactions.returnDeck(var1);
    }

    public boolean removeCardFromDeck(int deckId, int cardId) throws SQLException {
        Connection connection = DatabaseConnection.getConnection();
        String sql = "DELETE FROM deck_cards WHERE deck_id = ? AND card_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, deckId);
            stmt.setInt(2, cardId);
            return stmt.executeUpdate() > 0;
        }
    }

    // Add this method to your MTGDatabaseController class
    public entity.Card getCardById(int cardId) throws SQLException {
        // This should query your card table and return the appropriate card type
        // You'll need to implement this based on your card transaction logic
        Connection connection = DatabaseConnection.getConnection();
        String sql = "SELECT * FROM card WHERE card_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, cardId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String cardType = rs.getString("card_type").toLowerCase();

                // Create appropriate card type based on card_type
                if (cardType.contains("creature")) {
                    return new entity.Creature(
                            rs.getInt("card_id"),
                            rs.getString("card_name"),
                            rs.getString("card_mana_cost"),
                            rs.getString("card_type"),
                            rs.getString("card_subtype"),
                            rs.getInt("card_power"),
                            rs.getInt("card_toughness"),
                            rs.getString("card_text"),
                            rs.getString("card_edition"),
                            rs.getString("card_status")
                    );
                } else if (cardType.contains("instant")) {
                    return new entity.Instant(
                            rs.getInt("card_id"),
                            rs.getString("card_name"),
                            rs.getString("card_mana_cost"),
                            rs.getString("card_type"),
                            rs.getString("card_subtype"),
                            rs.getString("card_text"),
                            rs.getString("card_edition"),
                            rs.getString("card_status")
                    );
                } else if (cardType.contains("sorcery")) {
                    return new entity.Sorcery(
                            rs.getInt("card_id"),
                            rs.getString("card_name"),
                            rs.getString("card_mana_cost"),
                            rs.getString("card_type"),
                            rs.getString("card_subtype"),
                            rs.getString("card_text"),
                            rs.getString("card_edition"),
                            rs.getString("card_status")
                    );
                } else if (cardType.contains("artifact")) {
                    return new entity.Artifact(
                            rs.getInt("card_id"),
                            rs.getString("card_name"),
                            rs.getString("card_mana_cost"),
                            rs.getString("card_type"),
                            rs.getString("card_subtype"),
                            rs.getString("card_text"),
                            rs.getString("card_edition"),
                            rs.getString("card_status")
                    );
                } else if (cardType.contains("land")) {
                    return new entity.Land(
                            rs.getInt("card_id"),
                            rs.getString("card_name"),
                            rs.getString("card_type"),
                            rs.getString("card_subtype"),
                            rs.getString("card_text"),
                            rs.getString("card_edition"),
                            rs.getString("card_status")
                    );
                }
            }
        }
        return null; // Card not found
    }

    public List<Map<String, Object>> getCardsInDeck(int deckId) throws SQLException {
        List<Map<String, Object>> cards = new ArrayList<>();
        Connection connection = DatabaseConnection.getConnection();

        String sql = "SELECT dc.card_id, c.card_name, c.card_type, c.card_mana_cost, dc.quantity, dc.is_commander " +
                "FROM deck_cards dc " +
                "JOIN card c ON dc.card_id = c.card_id " +
                "WHERE dc.deck_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, deckId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Map<String, Object> card = new HashMap<>();
                card.put("card_id", rs.getInt("card_id"));
                card.put("card_name", rs.getString("card_name"));
                card.put("type", rs.getString("card_type"));
                card.put("mana_cost", rs.getString("card_mana_cost"));
                card.put("quantity", rs.getInt("quantity"));
                card.put("is_commander", rs.getBoolean("is_commander"));
                cards.add(card);
            }
        }
        return cards;
    }

    // Add this method to your MTGDatabaseController class
    public boolean deleteDeck(int deckId) throws SQLException {
        Connection connection = DatabaseConnection.getConnection();

        // First check if there are any active borrow requests for this deck
        String checkBorrowSql = "SELECT COUNT(*) as active_requests FROM borrow_request WHERE deck_id = ? AND status IN ('Pending', 'Approved')";
        try (PreparedStatement checkStmt = connection.prepareStatement(checkBorrowSql)) {
            checkStmt.setInt(1, deckId);
            ResultSet rs = checkStmt.executeQuery();
            if (rs.next() && rs.getInt("active_requests") > 0) {
                return false; // Cannot delete deck with active borrow requests
            }
        }

        // Delete the deck (cascade delete will handle deck_cards due to foreign key constraints)
        String deleteSql = "DELETE FROM deck WHERE deck_id = ?";
        try (PreparedStatement deleteStmt = connection.prepareStatement(deleteSql)) {
            deleteStmt.setInt(1, deckId);
            return deleteStmt.executeUpdate() > 0;
        }
    }
}
