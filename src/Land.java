public class Land implements Card{
    private int cardID;
    private String cardName;
    private String cardType;
    private String cardSubtype;
    private String cardText;
    private String cardEdition;
    private String cardStatus;

    public Land(int cardID, String cardName, String cardType, String cardSubtype,
                String cardText, String cardEdition, String cardStatus) {
        this.cardID = cardID;
        this.cardName = cardName;
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
        return "";
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
