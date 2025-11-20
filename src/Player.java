/**
 * Data Model: Represents a Player record from the database, matching the 'player' table schema.
 */
public class Player {
    private int playerId;
    private String firstName;
    private String lastName;
    private String cityAddress;
    private int age;

    // Default Constructor
    public Player() {}

    // Constructor without ID (for new records)
    public Player(String firstName, String lastName, String cityAddress, int age) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.cityAddress = cityAddress;
        this.age = age;
    }

    // FIX: 5-argument constructor (REQUIRED by PlayerGUI.java for update/new records)
    public Player(int playerId, String firstName, String lastName, String cityAddress, int age) {
        this.playerId = playerId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.cityAddress = cityAddress;
        this.age = age;
    }

    // --- Getters ---
    public int getPlayerId() { return playerId; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getCityAddress() { return cityAddress; }
    public int getAge() { return age; }

    // --- Setters (Needed for database retrieval logic) ---
    public void setPlayerId(int playerId) { this.playerId = playerId; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public void setCityAddress(String cityAddress) { this.cityAddress = cityAddress; }
    public void setAge(int age) { this.age = age; }
}