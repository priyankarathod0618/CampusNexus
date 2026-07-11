package campusnexus.model;

public class Student extends Person {
    private final String rollNumber;
    private final String branch;
    private final int year;
    private final String hostelBlock;
    private final String phone;
    private final int collegeId;
    private final String collegeName;

    public Student(int id, String name, String email, String password, boolean mustChangePassword,
                   String rollNumber, String branch, int year, String hostelBlock, String phone,
                   int collegeId, String collegeName) {
        super(id, name, email, password, mustChangePassword);
        this.rollNumber = rollNumber;
        this.branch = branch;
        this.year = year;
        this.hostelBlock = hostelBlock;
        this.phone = phone;
        this.collegeId = collegeId;
        this.collegeName = collegeName;
    }

    public String getRollNumber() { return rollNumber; }
    public String getBranch() { return branch; }
    public int getYear() { return year; }
    public String getHostelBlock() { return hostelBlock; }
    public String getPhone() { return phone; }
    public int getCollegeId() { return collegeId; }
    public String getCollegeName() { return collegeName; }

    @Override
    public String getRole() {
        return "STUDENT";
    }

    @Override
    public String getProfileDetails() {
        return "Name: " + name +
                "\nEmail: " + email +
                "\nRole: Student" +
                "\nRoll Number: " + rollNumber +
                "\nBranch: " + branch +
                "\nYear: " + year +
                "\nHostel Block: " + hostelBlock +
                "\nPhone: " + phone +
                "\nCollege: " + collegeName;
    }
}
