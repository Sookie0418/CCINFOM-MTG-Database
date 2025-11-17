public class Player {
    private int playerId;
    private String firstName;
    private String lastName;
    private String cityAddress;
    private int age;

    public Player() {}

    public Player(String firstName, String lastName, String cityAddress, int age) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.cityAddress = cityAddress;
        this.age = age;
    }

    // Getters and Setters
    public int getPlayerId() { return playerId; }
    public void setPlayerId(int playerId) { this.playerId = playerId; }
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public String getCityAddress() { return cityAddress; }
    public void setCityAddress(String cityAddress) { this.cityAddress = cityAddress; }
    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }

    @Override
    public String toString() {
        return String.format("Player ID: %d, Name: %s %s, City: %s, Age: %d",
                playerId, firstName, lastName, cityAddress, age);
    }
}