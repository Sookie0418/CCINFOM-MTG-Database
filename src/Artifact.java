public class Artifact implements Card{
    private int cardID;
    private String cardName;
    private String manaCost;
    private String cardType;
    private String cardSubtype;
    private String cardText;
    private String cardEdition;
    private String cardStatus;

    public Artifact(int cardID, String cardName, String manaCost, String cardType, String cardSubtype,
                    String cardText, String cardEdition, String cardStatus) {
        this.cardID = cardID;
        this.cardName = cardName;
        this.manaCost = manaCost;
        this.cardType = cardType;
        this.cardSubtype = cardSubtype;
        this.cardText = cardText;
        this.cardEdition = cardEdition;
        this.cardStatus = cardStatus;
    }

    @Override
    public int getCardId() {
        return cardID;
    }

    @Override
    public String getCardName() {
        return cardName;
    }

    @Override
    public String getManaCost() {
        return manaCost;
    }

    @Override
    public String getCardType() {
        return cardType;
    }

    @Override
    public String getCardSubtype() {
        return cardSubtype;
    }

    @Override
    public Integer getPower() {
        return 0;
    }

    @Override
    public Integer getToughness() {
        return 0;
    }

    @Override
    public String getCardText() {
        return cardText;
    }

    @Override
    public String getCardEdition() {
        return cardEdition;
    }

    @Override
    public String getCardStatus() {
        return cardStatus;
    }
}
