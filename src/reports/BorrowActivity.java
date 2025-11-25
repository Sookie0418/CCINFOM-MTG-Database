package reports;

import java.time.LocalDate;

public class BorrowActivity {
    private int borrowCode;
    private int playerId;
    private String playerFirstName;
    private String playerLastName;
    private int deckId;
    private String deckName;
    private String borrowType;
    private LocalDate requestDate;
    private LocalDate returnDate;
    private LocalDate dueDate;
    private String status;
    private int durationDays;
    
    public BorrowActivity(int borrowCode, int playerId, String playerFirstName, String playerLastName,
                         int deckId, String deckName, String borrowType, LocalDate requestDate,
                         LocalDate returnDate, LocalDate dueDate, String status, int durationDays) {
        this.borrowCode = borrowCode;
        this.playerId = playerId;
        this.playerFirstName = playerFirstName;
        this.playerLastName = playerLastName;
        this.deckId = deckId;
        this.deckName = deckName;
        this.borrowType = borrowType;
        this.requestDate = requestDate;
        this.returnDate = returnDate;
        this.dueDate = dueDate;
        this.status = status;
        this.durationDays = durationDays;
    }
    
    // Getters
    public int getBorrowCode() { return borrowCode; }
    public int getPlayerId() { return playerId; }
    public String getPlayerFirstName() { return playerFirstName; }
    public String getPlayerLastName() { return playerLastName; }
    public int getDeckId() { return deckId; }
    public String getDeckName() { return deckName; }
    public String getBorrowType() { return borrowType; }
    public LocalDate getRequestDate() { return requestDate; }
    public LocalDate getReturnDate() { return returnDate; }
    public LocalDate getDueDate() { return dueDate; }
    public String getStatus() { return status; }
    public int getDurationDays() { return durationDays; }
}