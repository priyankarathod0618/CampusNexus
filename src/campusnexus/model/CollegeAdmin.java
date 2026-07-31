package campusnexus.model;

public class CollegeAdmin {

    private final int id;
    private final String name;
    private final String email;
    private final String password;
    private final int collegeId;

    public CollegeAdmin(int id, String name, String email,
                        String password, int collegeId) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
        this.collegeId = collegeId;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public int getCollegeId() {
        return collegeId;
    }
}