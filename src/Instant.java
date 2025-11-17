public class Instant implements Card{
    private int cardID;
    private String cardName;
    private String manaCost;
    private String cardType;
    private String cardSubtype;
    private String cardText;
    private String cardEdition;
    private String cardStatus;

    public Instant(int cardID, String cardName, String manaCost, String cardType, String cardSubtype,
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
        return 0;
    }

    @Override
    public String getCardName() {
        return "";
    }

    @Override
    public String getManaCost() {
        return "";
    }

    @Override
    public String getCardType() {
        return "";
    }

    @Override
    public String getCardSubtype() {
        return "";
    }

    @Override
    public Integer getPower() {
        return 0;
    }

    @Override
    public void setPower(Integer power) {

    }

    @Override
    public Integer getToughness() {
        return 0;
    }

    @Override
    public String getCardText() {
        return "";
    }

    @Override
    public String getCardEdition() {
        return "";
    }

    @Override
    public String getCardStatus() {
        return "";
    }
}
