package campusnexus.model;

public abstract class Person {
    protected int id;
    protected String name;
    protected String email;
    protected String password;
    protected boolean mustChangePassword;

    protected Person(int id, String name, String email, String password, boolean mustChangePassword) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
        this.mustChangePassword = mustChangePassword;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public boolean isMustChangePassword() { return mustChangePassword; }
    public void setMustChangePassword(boolean mustChangePassword) { this.mustChangePassword = mustChangePassword; }

    public abstract String getRole();
    public abstract String getProfileDetails();
}
