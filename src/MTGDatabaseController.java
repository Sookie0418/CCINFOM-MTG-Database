import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.sql.Connection;
import java.sql.Statement;
import java.util.List;
import java.time.LocalDate;
import java.util.ArrayList;


public class MTGDatabaseController {
    private PlayerTransactions playerTransactions;
    private DeckTransactions deckTransactions;
    private CardTransactions cardTransactions;
    private BorrowTransactions borrowTransactions;

    /**
     * Initializes the controller, ensures the database schema is ready,
     * and initializes the transaction handlers.
     */
    public MTGDatabaseController() {
        initializeDatabaseSchema();

        this.cardTransactions = new CardTransactions();
        this.playerTransactions = new PlayerTransactions();
        this.deckTransactions = new DeckTransactions();
        this.borrowTransactions = new BorrowTransactions();
    }

    public boolean validateUser(String username, String password) {
        // TODO: Replace with actual database validation using a UserTransactions class.
        // TODO: Replace registerPlayer with registerUser and use this for validation.
        Path filePath = Paths.get("LoginInfo.txt");
        if (!Files.exists(filePath)) {
            System.out.println("Login file not found.");
            return false;
        }
        try {
            boolean isValid = Files.lines(filePath)
                    .map(line -> line.split(" "))
                    .filter(parts -> parts.length >= 2)
                    .anyMatch(parts -> parts[0].equals(username) && parts[1].equals(password));

            if (!isValid) {
                System.out.println("Invalid username or password.");
            }
            return isValid;

        } catch (IOException e) {
            System.out.println("Error reading login file: " + e.getMessage());
            return false;
        }
    }

    /**
     * Creates all tables from the MTG Database.sql file if they do not already exist.
     */
    private void initializeDatabaseSchema() {
        String createTablesSQL =
                "CREATE TABLE IF NOT EXISTS player (" +
                        "    player_id INT AUTO_INCREMENT PRIMARY KEY," +
                        "    first_name VARCHAR(50) NOT NULL," +
                        "    last_name VARCHAR(50) NOT NULL," +
                        "    city_address VARCHAR(100)," +
                        "    age INT," +
                        "    UNIQUE (first_name, last_name)" +
                        ");" +
                        "CREATE TABLE IF NOT EXISTS card (" +
                        "    card_id INT AUTO_INCREMENT PRIMARY KEY," +
                        "    card_name VARCHAR(100) NOT NULL," +
                        "    card_mana_cost VARCHAR(50)," +
                        "    card_type VARCHAR(50)," +
                        "    card_subtype VARCHAR(50)," +
                        "    card_power VARCHAR(10)," +
                        "    card_toughness VARCHAR(10)," +
                        "    card_text TEXT," +
                        "    card_edition VARCHAR(50)," +
                        "    card_status ENUM('Legal', 'Banned', 'Game Changer') DEFAULT 'Legal'" +
                        ");" +
                        "CREATE TABLE IF NOT EXISTS deck (" +
                        "    deck_id INT AUTO_INCREMENT PRIMARY KEY," +
                        "    deck_name VARCHAR(100) NOT NULL," +
                        "    player_id INT NOT NULL," +
                        "    commander_card_id INT," +
                        "    bracket_info VARCHAR(50)," +
                        "    mana_base VARCHAR(100)," +
                        "    salt_score DECIMAL(4,2)," +
                        "    validity ENUM('Valid', 'Invalid') DEFAULT 'Valid'," +
                        "    description TEXT," +
                        "    FOREIGN KEY (player_id) REFERENCES player(player_id)" +
                        "        ON DELETE CASCADE" +
                        "        ON UPDATE CASCADE," +
                        "    FOREIGN KEY (commander_card_id) REFERENCES card(card_id)" +
                        "        ON DELETE SET NULL" +
                        "        ON UPDATE CASCADE" +
                        ");" +
                        "CREATE TABLE IF NOT EXISTS borrow_request (" +
                        "    borrow_code INT AUTO_INCREMENT PRIMARY KEY," +
                        "    player_id INT NOT NULL," +
                        "    deck_id INT NOT NULL," +
                        "    borrow_type ENUM('Wait', 'Immediate') DEFAULT 'Immediate'," +
                        "    request_date DATE NOT NULL," +
                        "    due_date DATE," +
                        "    return_date DATE," +
                        "    status ENUM('Pending', 'Approved', 'Returned', 'Overdue', 'Cancelled') DEFAULT 'Pending'," +
                        "    FOREIGN KEY (player_id) REFERENCES player(player_id)" +
                        "        ON DELETE CASCADE" +
                        "        ON UPDATE CASCADE," +
                        "    FOREIGN KEY (deck_id) REFERENCES deck(deck_id)" +
                        "        ON DELETE CASCADE" +
                        "        ON UPDATE CASCADE" +
                        ");";

        Connection conn = DatabaseConnection.getConnection();

        try (Statement stmt = conn.createStatement()) {

            if (conn != null) {
                String[] statements = createTablesSQL.split(";");
                for (String statement : statements) {
                    if (!statement.trim().isEmpty()) {
                        stmt.addBatch(statement);
                    }
                }
                stmt.executeBatch();
                System.out.println("All primary tables initialized successfully (MySQL).");
            }
        } catch (SQLException e) {
            System.err.println("Database table initialization failed. Check MySQL status and credentials: " + e.getMessage());
        }
    }

    public List<Record> getAllCards() throws SQLException {
        return cardTransactions.getAllCards();
    }

    public void addCard(String name, String manaCost, String type, String subtype,
                        String power, String toughness, String text, String edition, String status) throws SQLException {
        cardTransactions.addCard(name, manaCost, type, subtype, power, toughness, text, edition, status);
    }

    public void updateCard(int id, String name, String manaCost, String type, String subtype,
                           String power, String toughness, String text, String edition, String status) throws SQLException {
        cardTransactions.updateCard(id, name, manaCost, type, subtype, power, toughness, text, edition, status);
    }

    public void deleteCard(int id) throws SQLException {
        cardTransactions.deleteCard(id);
    }

    /**
     * Retrieves all player records from the database via the transaction layer.
     */
    public List<Player> getAllPlayers() throws SQLException {
        // FIX: Method signature added
        return playerTransactions.getAllPlayers();
    }

    /**
     * Adds a new player record.
     */
    public int addPlayer(Player player) throws SQLException {
        // FIX: Method signature added
        return playerTransactions.addPlayer(player);
    }

    /**
     * Updates an existing player record.
     */
    public boolean updatePlayer(Player player) throws SQLException {
        // FIX: Method signature added
        return playerTransactions.updatePlayer(player);
    }

    /**
     * Deletes a player record.
     */
    public boolean deletePlayer(int playerId) throws SQLException {
        // FIX: Method signature added
        return playerTransactions.deletePlayer(playerId);
    }
}