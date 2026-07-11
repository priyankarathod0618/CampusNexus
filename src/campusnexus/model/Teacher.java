package campusnexus.model;

public class Teacher extends Person {
    private final String employeeId;
    private final String department;
    private final String subject;
    private final String phone;
    private final int collegeId;
    private final String collegeName;

    public Teacher(int id, String name, String email, String password, boolean mustChangePassword,
                   String employeeId, String department, String subject, String phone,
                   int collegeId, String collegeName) {
        super(id, name, email, password, mustChangePassword);
        this.employeeId = employeeId;
        this.department = department;
        this.subject = subject;
        this.phone = phone;
        this.collegeId = collegeId;
        this.collegeName = collegeName;
    }

    public String getEmployeeId() { return employeeId; }
    public String getDepartment() { return department; }
    public String getSubject() { return subject; }
    public String getPhone() { return phone; }
    public int getCollegeId() { return collegeId; }
    public String getCollegeName() { return collegeName; }

    @Override
    public String getRole() {
        return "TEACHER";
    }

    @Override
    public String getProfileDetails() {
        return "Name: " + name +
                "\nEmail: " + email +
                "\nRole: Teacher" +
                "\nEmployee ID: " + employeeId +
                "\nDepartment: " + department +
                "\nSubject: " + subject +
                "\nPhone: " + phone +
                "\nCollege: " + collegeName;
    }
}
