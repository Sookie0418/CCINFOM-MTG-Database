package reports;

public class CardUsageStats {
    private int cardId;
    private String cardName;
    private String cardType;
    private String manaCost;
    private int deckCount;
    private int totalCopies;
    
    public CardUsageStats(int cardId, String cardName, String cardType, String manaCost, 
                         int deckCount, int totalCopies) {
        this.cardId = cardId;
        this.cardName = cardName;
        this.cardType = cardType;
        this.manaCost = manaCost;
        this.deckCount = deckCount;
        this.totalCopies = totalCopies;
    }
    
    // Getters
    public int getCardId() { return cardId; }
    public String getCardName() { return cardName; }
    public String getCardType() { return cardType; }
    public String getManaCost() { return manaCost; }
    public int getDeckCount() { return deckCount; }
    public int getTotalCopies() { return totalCopies; }
}