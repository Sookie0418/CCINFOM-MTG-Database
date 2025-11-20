//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package entity;

import java.time.LocalDate;

public class BorrowRequest {
    private int borrowCode;
    private int playerId;
    private int deckId;
    private String borrowType;
    private LocalDate requestDate;
    private String status;
    private LocalDate returnDate;

    public BorrowRequest(int var1, int var2, int var3, String var4, LocalDate var5, String var6, LocalDate var7) {
        this.borrowCode = var1;
        this.playerId = var2;
        this.deckId = var3;
        this.borrowType = var4;
        this.requestDate = var5;
        this.status = var6;
        this.returnDate = var7;
    }

    public int getBorrowCode() {
        return this.borrowCode;
    }

    public int getPlayerId() {
        return this.playerId;
    }

    public int getDeckId() {
        return this.deckId;
    }

    public String getBorrowType() {
        return this.borrowType;
    }

    public LocalDate getRequestDate() {
        return this.requestDate;
    }

    public String getStatus() {
        return this.status;
    }

    public LocalDate getReturnDate() {
        return this.returnDate;
    }
}
