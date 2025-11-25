package reports;

public class PlayerBorrowingStats {
    private int playerId;
    private String firstName;
    private String lastName;
    private int totalBorrows;
    private double avgDuration;
    private int overdueCount;
    
    public PlayerBorrowingStats(int playerId, String firstName, String lastName, 
                               int totalBorrows, double avgDuration, int overdueCount) {
        this.playerId = playerId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.totalBorrows = totalBorrows;
        this.avgDuration = avgDuration;
        this.overdueCount = overdueCount;
    }
    
    // Getters
    public int getPlayerId() { return playerId; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public int getTotalBorrows() { return totalBorrows; }
    public double getAvgDuration() { return avgDuration; }
    public int getOverdueCount() { return overdueCount; }
}