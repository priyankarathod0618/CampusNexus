package campusnexus.service;

import campusnexus.dao.CollegeDAO;
import campusnexus.dao.UserDAO;
import campusnexus.exception.DuplicateEmailException;
import campusnexus.exception.DuplicateRollNumberException;
import campusnexus.exception.InvalidDataException;
import campusnexus.model.College;
import campusnexus.util.PasswordUtil;

import java.sql.SQLException;

public class AdminService {
    private final UserDAO userDAO = new UserDAO();
    private final CollegeDAO collegeDAO = new CollegeDAO();

    public int addStudent(String name, String email, String phone, int collegeId, String rollNumber,
                          String branch, int year, String hostelBlock)
            throws DuplicateEmailException, DuplicateRollNumberException, InvalidDataException, SQLException {

        validateCommonFields(name, email, phone, collegeId);

        if (!InputValidator.isValidRollNumber(rollNumber)) {
            throw new InvalidDataException("Roll number must be 3-20 digits only.");
        }
        if (!InputValidator.isValidBranch(branch)) {
            throw new InvalidDataException("Invalid branch.");
        }
        if (year < 1 || year > 4) {
            throw new InvalidDataException("Year must be between 1 and 4.");
        }
        if (userDAO.emailExists(email)) {
            throw new DuplicateEmailException("An account already exists with this email.");
        }
        if (userDAO.rollNumberExists(rollNumber)) {
            throw new DuplicateRollNumberException("This roll number is already registered.");
        }

        // First-time password is the registered phone number, per project decision -
        // now stored as a salted hash instead of plaintext.
        int userId = userDAO.insertUser(name, email, PasswordUtil.hash(phone), "STUDENT");
        userDAO.insertStudentProfile(userId, collegeId, rollNumber, branch, year, hostelBlock, phone);
        return userId;
    }

    public int addTeacher(String name, String email, String phone, int collegeId, String employeeId,
                          String department, String subject)
            throws DuplicateEmailException, InvalidDataException, SQLException {

        validateCommonFields(name, email, phone, collegeId);

        if (!InputValidator.isValidEmployeeId(employeeId)) {
            throw new InvalidDataException("Invalid employee ID.");
        }
        if (!InputValidator.isValidDepartment(department)) {
            throw new InvalidDataException("Invalid department.");
        }
        if (!InputValidator.isValidSubject(subject)) {
            throw new InvalidDataException("Invalid subject.");
        }
        if (userDAO.emailExists(email)) {
            throw new DuplicateEmailException("An account already exists with this email.");
        }

        int userId = userDAO.insertUser(name, email, PasswordUtil.hash(phone), "TEACHER");
        userDAO.insertTeacherProfile(userId, collegeId, employeeId, department, subject, phone);
        return userId;
    }

    // Shared guard for both addStudent/addTeacher: name, phone, email format,
    // college existence, and (BUGFIX) the email-domain-must-match-college check
    // that previously only existed in the console addStudent() flow and was
    // missing everywhere else (console addTeacher(), and the whole JavaFX
    // AdminSetupView which called this service with no validation at all).
    private void validateCommonFields(String name, String email, String phone, int collegeId)
            throws InvalidDataException {

        if (!InputValidator.isValidName(name)) {
            throw new InvalidDataException("Name must be 3-50 letters (single spaces between words, no digits/symbols).");
        }
        if (!InputValidator.isValidPhone(phone)) {
            throw new InvalidDataException("Phone number must be exactly 10 digits and start with 6, 7, 8 or 9.");
        }
        if (!InputValidator.isValidEmail(email)) {
            throw new InvalidDataException("Invalid email format.");
        }

        College college = collegeDAO.findById(collegeId);
        if (college == null) {
            throw new InvalidDataException("Selected college does not exist.");
        }

        String domain = college.getEmailDomain() == null ? "" : college.getEmailDomain().trim().toLowerCase();
        if (!domain.isEmpty() && !email.toLowerCase().endsWith("@" + domain)) {
            throw new InvalidDataException("Email must end with @" + domain + " for " + college.getName() + ".");
        }
    }
}