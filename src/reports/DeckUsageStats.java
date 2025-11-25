package reports;

import java.time.LocalDate;

public class DeckUsageStats {
    private int deckId;
    private String deckName;
    private String ownerFirstName;
    private String ownerLastName;
    private String bracketInfo;
    private String validity;
    private int borrowCount;
    private double avgBorrowDuration;
    private LocalDate lastBorrowed;
    
    public DeckUsageStats(int deckId, String deckName, String ownerFirstName, String ownerLastName,
                         String bracketInfo, String validity, int borrowCount, 
                         double avgBorrowDuration, LocalDate lastBorrowed) {
        this.deckId = deckId;
        this.deckName = deckName;
        this.ownerFirstName = ownerFirstName;
        this.ownerLastName = ownerLastName;
        this.bracketInfo = bracketInfo;
        this.validity = validity;
        this.borrowCount = borrowCount;
        this.avgBorrowDuration = avgBorrowDuration;
        this.lastBorrowed = lastBorrowed;
    }
    
    // Getters
    public int getDeckId() { return deckId; }
    public String getDeckName() { return deckName; }
    public String getOwnerFirstName() { return ownerFirstName; }
    public String getOwnerLastName() { return ownerLastName; }
    public String getBracketInfo() { return bracketInfo; }
    public String getValidity() { return validity; }
    public int getBorrowCount() { return borrowCount; }
    public double getAvgBorrowDuration() { return avgBorrowDuration; }
    public LocalDate getLastBorrowed() { return lastBorrowed; }
}