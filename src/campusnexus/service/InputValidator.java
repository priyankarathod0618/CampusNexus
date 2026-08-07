package campusnexus.service;

public class InputValidator {

    public static boolean isNotEmpty(String s) {
        return s != null && !s.trim().isEmpty();
    }


    // Letters only, single spaces between words, no leading/trailing/double spaces.
    public static boolean isValidName(String name) {
        return name != null && name.matches("[A-Za-z]+( [A-Za-z]+)*") && name.length() >= 3 && name.length() <= 50;
    }

    // Roll numbers are purely numeric (per college policy), 3-20 digits, no spaces.
    public static boolean isValidRollNumber(String roll) {
        return roll != null && roll.matches("[0-9]{3,20}");
    }

    public static boolean isValidBranch(String branch) {
        return branch.matches("[A-Za-z &]{2,50}");
    }

    public static boolean isValidHostelBlock(String block) {
        return block.matches("[A-Za-z0-9-]{1,10}");
    }

    public static boolean isValidEmployeeId(String id) {
        return id.matches("[A-Za-z0-9-]{3,20}");
    }

    public static boolean isValidDepartment(String dept) {
        return dept.matches("[A-Za-z &]{2,50}");
    }

    public static boolean isValidSubject(String subject) {
        return subject.matches("[A-Za-z &]{2,50}");
    }
    public static boolean isValidEmail(String email) {
        if (email == null || email.isEmpty()) {
            return false;
        }

        String regex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";

        return email.matches(regex);
    }

    // Exactly 10 digits, first digit 6-9 (valid Indian mobile prefixes), no spaces.
    public static boolean isValidPhone(String phone) {
        return phone != null && phone.matches("[6-9][0-9]{9}");
    }

    public static boolean isValidYear(String yearStr) {
        try {
            int year = Integer.parseInt(yearStr.trim());
            return year >= 1 && year <= 4;
        } catch (Exception e) {
            return false;
        }
    }
}
