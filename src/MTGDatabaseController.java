import java.sql.SQLException;
import java.util.List;
import java.time.LocalDate;

public class MTGDatabaseController {
    private PlayerTransactions playerTransactions;
    private DeckTransactions deckTransactions;
    private CardTransactions cardTransactions;
    private BorrowTransactions borrowTransactions;

    public MTGDatabaseController() {
        this.playerTransactions = new PlayerTransactions();
        this.deckTransactions = new DeckTransactions();
        this.cardTransactions = new CardTransactions();
        this.borrowTransactions = new BorrowTransactions();
    }

}