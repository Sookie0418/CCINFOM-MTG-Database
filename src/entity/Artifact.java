//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package entity;

public class Artifact implements Card {
    private int cardID;
    private String cardName;
    private String manaCost;
    private String cardType;
    private String cardSubtype;
    private String cardText;
    private String cardEdition;
    private String cardStatus;

    public Artifact(int var1, String var2, String var3, String var4, String var5, String var6, String var7, String var8) {
        this.cardID = var1;
        this.cardName = var2;
        this.manaCost = var3;
        this.cardType = var4;
        this.cardSubtype = var5;
        this.cardText = var6;
        this.cardEdition = var7;
        this.cardStatus = var8;
    }

    public int getCardId() {
        return this.cardID;
    }

    public String getCardName() {
        return this.cardName;
    }

    public String getManaCost() {
        return this.manaCost;
    }

    public String getCardType() {
        return this.cardType;
    }

    public String getCardSubtype() {
        return this.cardSubtype;
    }

    public Integer getPower() {
        return 0;
    }

    public Integer getToughness() {
        return 0;
    }

    public String getCardText() {
        return this.cardText;
    }

    public String getCardEdition() {
        return this.cardEdition;
    }

    public String getCardStatus() {
        return this.cardStatus;
    }
}
