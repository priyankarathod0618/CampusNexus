package campusnexus.model;

public class Course {

    private int id;
    private int collegeId;
    private String courseName;
    private String duration;
    private double annualFee;

    public Course(int id,
                  int collegeId,
                  String courseName,
                  String duration,
                  double annualFee) {

        this.id = id;
        this.collegeId = collegeId;
        this.courseName = courseName;
        this.duration = duration;
        this.annualFee = annualFee;
    }

    public int getId() {
        return id;
    }

    public int getCollegeId() {
        return collegeId;
    }

    public String getCourseName() {
        return courseName;
    }

    public String getDuration() {
        return duration;
    }

    public double getAnnualFee() {
        return annualFee;
    }
}