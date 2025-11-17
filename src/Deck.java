import java.util.ArrayList;

public class Deck {
    private ArrayList<Card> deckCards;
    private String deckID;
    private String deckName;
    private String ownerID;
    private Card commanderCard;
    private int bracketNum;
    private boolean deckValidity;

    public ArrayList<Card> getDeckCards() {
        return deckCards;
    }

    public String getDeckID() {
        return deckID;
    }

    public String getDeckName() {
        return deckName;
    }

    public String getOwnerID() {
        return ownerID;
    }

    public Card getCommanderCard() {
        return commanderCard;
    }

    public void setBracketNum(int bracketNum) {
        this.bracketNum = bracketNum;
    }

    public boolean getDeckValidity() {
        return deckValidity;
    }

    public void checkDeckValidity() {
        // Should print out all errors in validity when checking

        // Checks if deck exceeds card limit
        if(deckCards.size() > 100) {
            // " (!) Deck exceeds card limit (100)!
            deckValidity = false;
        }

        // Checks if banned card exists in the deck

        for(Card card : deckCards) {
            if(card.getCardStatus().equalsIgnoreCase("Banned")) {
                // " (!) Deck contains banned card: {Banned Card Name}
                deckValidity = false;
            }
        }

        // Checks for number of Game Changers in deck and checks if quantity matches maximum in each bracket.
        // Bracket 1 = 0 Game Changers
        // Bracket 2 = 0 Game Changers
        // Bracket 3 = 3 Game Changers
        // Bracket 4 - 5 = No Limit
        int GCQuantity = 0;
        ArrayList<Card> GCList = new ArrayList<Card>();
        for(Card card : deckCards) {
            if(card.getCardStatus().equalsIgnoreCase("Game Changer")) {
                GCList.add(card);
            }
        }
        
        
    }
}
