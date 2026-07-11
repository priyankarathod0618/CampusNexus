package campusnexus.model;

public class College {
    private final int id;
    private final String name;
    private final String city;
    private final String code;
    private final String emailDomain;
    private final double fees;
    private final boolean hostelAvailable;
    private final String facilities;
    private final double averageRating;

    public College(int id, String name, String city, String code, String emailDomain,
                   double fees, boolean hostelAvailable, String facilities, double averageRating) {
        this.id = id;
        this.name = name;
        this.city = city;
        this.code = code;
        this.emailDomain = emailDomain;
        this.fees = fees;
        this.hostelAvailable = hostelAvailable;
        this.facilities = facilities;
        this.averageRating = averageRating;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getCity() {
        return city;
    }

    public String getCode() {
        return code;
    }

    public String getEmailDomain() {
        return emailDomain;
    }

    public double getFees() {
        return fees;
    }

    public boolean isHostelAvailable() {
        return hostelAvailable;
    }

    public String getFacilities() {
        return facilities;
    }

    public double getAverageRating() {
        return averageRating;
    }
}
