import java.sql.*;
import java.util.ArrayList;

public class Deck {
    private ArrayList<Card> deckCards;
    private int deckID;
    private String deckName;
    private int ownerID;
    private Card commanderCard;
    private int bracketNum;
    private boolean deckValidity;
    private String manaBase;
    private String description;

    private Connection connection = null;

    // Constructor
    public Deck(int deckID, String deckName, int ownerID, int bracketNum,
                String manaBase, String description) throws SQLException, ClassNotFoundException {
        this.deckID = deckID;
        this.deckName = deckName;
        this.ownerID = ownerID;
        this.bracketNum = bracketNum;
        this.manaBase = manaBase;
        this.description = description;
        this.deckCards = new ArrayList<>();

        // Initialize database connection
        Class.forName("com.mysql.cj.jdbc.Driver");
        connection = DriverManager.getConnection(
                "jdbc:mysql://127.0.0.1:3306/mtg_commander_db",
                "root",
                "password");
    }

    // Load deck cards from database using CardFactory
    public void loadDeckCards() throws SQLException {
        ResultSet rs = null;
        Card card = CardFactory.createCardFromResultSet(rs);
        deckCards.clear();
        String sql = "SELECT c.*, dc.is_commander, dc.is_game_changer, dc.quantity " +
                "FROM deck_cards dc " +
                "JOIN card c ON dc.card_id = c.card_id " +
                "WHERE dc.deck_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, deckID);
            rs = stmt.executeQuery();

            while (rs.next()) {
                card = CardFactory.createCardFromResultSet(rs);

                // Set commander if this card is the commander
                if (rs.getBoolean("is_commander")) {
                    this.commanderCard = card;
                }

                deckCards.add(card);
            }
        }
    }

    // Getters
    public ArrayList<Card> getDeckCards() {
        return new ArrayList<>(deckCards); // Return copy to prevent external modification
    }

    public int getDeckID() {
        return deckID;
    }

    public String getDeckName() {
        return deckName;
    }

    public int getOwnerID() {
        return ownerID;
    }

    public Card getCommanderCard() {
        return commanderCard;
    }

    public int getBracketNum() {
        return bracketNum;
    }

    public boolean getDeckValidity() {
        return deckValidity;
    }

    public String getManaBase() {
        return manaBase;
    }

    public String getDescription() {
        return description;
    }

    // Setters
    public void setBracketNum(int bracketNum) {
        this.bracketNum = bracketNum;
    }

    public void setCommanderCard(Card selectedCard) throws SQLException {
        // Check if the card is a creature and legendary
        if (selectedCard instanceof Creature &&
                selectedCard.getCardType().contains("Legendary")) {

            this.commanderCard = selectedCard;

            // Update database to set this card as commander
            String updateSql = "UPDATE deck_cards SET is_commander = CASE WHEN card_id = ? THEN TRUE ELSE FALSE END WHERE deck_id = ?";
            try (PreparedStatement stmt = connection.prepareStatement(updateSql)) {
                stmt.setInt(1, selectedCard.getCardId());
                stmt.setInt(2, deckID);
                stmt.executeUpdate();
            }

            System.out.println(selectedCard.getCardName() + " is now set as Commander!");
        } else {
            System.out.println("Error: Commander must be a Legendary Creature!");
        }
    }

    // Add card to deck
    public boolean addCardToDeck(Card card, int quantity, boolean isGameChanger) throws SQLException {
        String sql = "INSERT INTO deck_cards (deck_id, card_id, card_name, quantity, is_commander, is_game_changer, card_status) " +
                "VALUES (?, ?, ?, ?, ?, ?, 'In Deck') " +
                "ON DUPLICATE KEY UPDATE quantity = quantity + ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, deckID);
            stmt.setInt(2, card.getCardId());
            stmt.setString(3, card.getCardName());
            stmt.setInt(4, quantity);
            stmt.setBoolean(5, false); // Not commander by default
            stmt.setBoolean(6, isGameChanger);
            stmt.setInt(7, quantity);

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                // Add the card to our local list (avoiding duplicates)
                boolean cardExists = false;
                for (Card existingCard : deckCards) {
                    if (existingCard.getCardId() == card.getCardId()) {
                        cardExists = true;
                        break;
                    }
                }
                if (!cardExists) {
                    deckCards.add(card);
                }
                return true;
            }
        }
        return false;
    }

    // Remove card from deck
    public boolean removeCardFromDeck(int cardId) throws SQLException {
        String sql = "DELETE FROM deck_cards WHERE deck_id = ? AND card_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, deckID);
            stmt.setInt(2, cardId);

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                // Remove from local list
                deckCards.removeIf(card -> card.getCardId() == cardId);
                return true;
            }
        }
        return false;
    }

    // Check deck validity with detailed error reporting
    public boolean checkDeckValidity() {
        ArrayList<String> errors = new ArrayList<>();
        deckValidity = true; // Start assuming valid

        // Check 1: Total card count (must be exactly 100 for Commander)
        int totalCardCount = getTotalCardCount();
        if (totalCardCount != 100) {
            errors.add("Deck must contain exactly 100 cards. Current count: " + totalCardCount);
            deckValidity = false;
        }

        // Check 2: Exactly one commander
        if (commanderCard == null) {
            errors.add("Deck must have exactly one Commander");
            deckValidity = false;
        } else if (!(commanderCard instanceof Creature) || !commanderCard.getCardType().contains("Legendary")) {
            errors.add("Commander must be a Legendary Creature: " + commanderCard.getCardName());
            deckValidity = false;
        }

        // Check 3: Banned cards
        ArrayList<Card> bannedCards = getBannedCards();
        if (!bannedCards.isEmpty()) {
            for (Card bannedCard : bannedCards) {
                errors.add("Deck contains banned card: " + bannedCard.getCardName());
            }
            deckValidity = false;
        }

        // Check 4: Game Changer limits based on bracket
        int gcCount = getGameChangerCount();
        switch (bracketNum) {
            case 1:
            case 2:
                if (gcCount > 0) {
                    errors.add("Bracket " + bracketNum + " allows 0 Game Changers. Current: " + gcCount);
                    deckValidity = false;
                }
                break;
            case 3:
                if (gcCount > 3) {
                    errors.add("Bracket 3 allows maximum 3 Game Changers. Current: " + gcCount);
                    deckValidity = false;
                }
                break;
            case 4:
            case 5:
                // No limit for brackets 4-5
                break;
            default:
                errors.add("Invalid bracket number: " + bracketNum);
                deckValidity = false;
        }

        // Check 5: Singleton rule (except basic lands)
        ArrayList<String> duplicateCards = getDuplicateCards();
        if (!duplicateCards.isEmpty()) {
            for (String duplicate : duplicateCards) {
                errors.add("Multiple copies of non-land card: " + duplicate);
            }
            deckValidity = false;
        }

        // Print all errors
        if (!errors.isEmpty()) {
            System.out.println("\n=== DECK VALIDATION ERRORS ===");
            for (String error : errors) {
                System.out.println("❌ " + error);
            }
        } else {
            System.out.println("✅ Deck is VALID!");
        }

        return deckValidity;
    }

    // Helper methods for validation
    private int getTotalCardCount() {
        String sql = "SELECT SUM(quantity) as total FROM deck_cards WHERE deck_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, deckID);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("total");
            }
        } catch (SQLException e) {
            System.err.println("Error counting cards: " + e.getMessage());
        }
        return 0;
    }

    private ArrayList<Card> getBannedCards() {
        ArrayList<Card> banned = new ArrayList<>();
        for (Card card : deckCards) {
            if ("Banned".equalsIgnoreCase(card.getCardStatus())) {
                banned.add(card);
            }
        }
        return banned;
    }

    private int getGameChangerCount() {
        int count = 0;
        String sql = "SELECT COUNT(*) FROM deck_cards WHERE deck_id = ? AND is_game_changer = TRUE";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, deckID);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                count = rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Error counting game changers: " + e.getMessage());
        }
        return count;
    }

    private ArrayList<String> getDuplicateCards() {
        ArrayList<String> duplicates = new ArrayList<>();
        String sql = "SELECT card_name, SUM(quantity) as total " +
                "FROM deck_cards " +
                "WHERE deck_id = ? AND is_commander = FALSE " +
                "AND card_name NOT IN ('Plains', 'Island', 'Swamp', 'Mountain', 'Forest') " +
                "GROUP BY card_name " +
                "HAVING total > 1";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, deckID);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                duplicates.add(rs.getString("card_name") + " (" + rs.getInt("total") + " copies)");
            }
        } catch (SQLException e) {
            System.err.println("Error checking duplicates: " + e.getMessage());
        }
        return duplicates;
    }

    // Save deck to database
    public boolean saveDeck() throws SQLException {
        String sql = "INSERT INTO deck (deck_id, deck_name, player_id, commander_card_id, bracket_info, mana_base, salt_score, description) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE " +
                "deck_name = VALUES(deck_name), player_id = VALUES(player_id), " +
                "commander_card_id = VALUES(commander_card_id), bracket_info = VALUES(bracket_info), " +
                "mana_base = VALUES(mana_base), salt_score = VALUES(salt_score), description = VALUES(description)";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, deckID);
            stmt.setString(2, deckName);
            stmt.setInt(3, ownerID);
            stmt.setInt(4, commanderCard != null ? commanderCard.getCardId() : 0);
            stmt.setString(5, "Bracket " + bracketNum);
            stmt.setString(6, manaBase);
            stmt.setString(7, description);

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        }
    }

    // Display deck info with card types
    public void displayDeckInfo() {
        System.out.println("\n=== DECK INFORMATION ===");
        System.out.println("Deck ID: " + deckID);
        System.out.println("Deck Name: " + deckName);
        System.out.println("Owner ID: " + ownerID);
        System.out.println("Commander: " + (commanderCard != null ? commanderCard.getCardName() : "None"));
        System.out.println("Bracket: " + bracketNum);
        System.out.println("Mana Base: " + manaBase);
        System.out.println("Total Cards: " + getTotalCardCount());
        System.out.println("Validity: " + (deckValidity ? "Valid" : "Invalid"));

        // Display card breakdown by type
        System.out.println("\n--- Card Breakdown ---");
        int creatures = 0, artifacts = 0, instants = 0, sorceries = 0, lands = 0, others = 0;

        for (Card card : deckCards) {
            if (card instanceof Creature) creatures++;
            else if (card instanceof Artifact) artifacts++;
            else if (card instanceof Instant) instants++;
            else if (card instanceof Sorcery) sorceries++;
            else if (card instanceof Land) lands++;
            else others++;
        }

        System.out.println("Creatures: " + creatures);
        System.out.println("Artifacts: " + artifacts);
        System.out.println("Instants: " + instants);
        System.out.println("Sorceries: " + sorceries);
        System.out.println("Lands: " + lands);
        if (others > 0) System.out.println("Others: " + others);
    }

    // Close database connection
    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            System.err.println("Error closing connection: " + e.getMessage());
        }
    }
}