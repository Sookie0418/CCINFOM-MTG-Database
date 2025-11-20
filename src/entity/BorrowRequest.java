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

	public BorrowRequest(int borrowCode, int playerId, int deckId, String borrowType, LocalDate requestDate, String status, LocalDate returnDate) {
		this.borrowCode = borrowCode;
		this.playerId = playerId;
		this.deckId = deckId;
		this.borrowType = borrowType;
		this.requestDate = requestDate;
		this.status = status;
		this.returnDate = returnDate;
	}

	// Getters
	public int getBorrowCode() { return borrowCode; }
	public int getPlayerId() { return playerId; }
	public int getDeckId() { return deckId; }
	public String getBorrowType() { return borrowType; }
	public LocalDate getRequestDate() { return requestDate; }
	public String getStatus() { return status; }
	public LocalDate getReturnDate() { return returnDate; }
}