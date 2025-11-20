public class Record {
    private final int id;
    private final String name;
    private final String manaCost;
    private final String type;
    private final String subtype;
    private final String power;
    private final String toughness;
    private final String text;
    private final String edition;
    private final String status;

    public Record(int id, String name, String manaCost, String type, String subtype,
                  String power, String toughness, String text, String edition, String status) {
        this.id = id;
        this.name = name;
        this.manaCost = manaCost;
        this.type = type;
        this.subtype = subtype;
        this.power = power;
        this.toughness = toughness;
        this.text = text;
        this.edition = edition;
        this.status = status;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getManaCost() { return manaCost; }
    public String getType() { return type; }
    public String getSubtype() { return subtype; }
    public String getPower() { return power; }
    public String getToughness() { return toughness; }
    public String getText() { return text; }
    public String getEdition() { return edition; }
    public String getStatus() { return status; }
}