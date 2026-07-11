package campusnexus.model;

public class Club {
    private final int id;
    private final String name;
    private final String category;
    private final String description;

    public Club(int id, String name, String category, String description) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.description = description;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getCategory() { return category; }
    public String getDescription() { return description; }
}
