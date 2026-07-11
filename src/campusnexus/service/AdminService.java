package campusnexus.service;

import campusnexus.dao.UserDAO;
import campusnexus.exception.DuplicateEmailException;
import campusnexus.exception.DuplicateRollNumberException;

import java.sql.SQLException;

public class AdminService {
    private final UserDAO userDAO = new UserDAO();

    public int addStudent(String name, String email, String phone, int collegeId, String rollNumber,
                          String branch, int year, String hostelBlock)
            throws DuplicateEmailException, DuplicateRollNumberException, SQLException {

        if (userDAO.emailExists(email)) {
            throw new DuplicateEmailException("An account already exists with this email.");
        }
        if (userDAO.rollNumberExists(rollNumber)) {
            throw new DuplicateRollNumberException("This roll number is already registered.");
        }

        // First-time password is the registered phone number, per project decision
        int userId = userDAO.insertUser(name, email, phone, "STUDENT");
        userDAO.insertStudentProfile(userId, collegeId, rollNumber, branch, year, hostelBlock, phone);
        return userId;
    }

    public int addTeacher(String name, String email, String phone, int collegeId, String employeeId,
                          String department, String subject) throws DuplicateEmailException, SQLException {

        if (userDAO.emailExists(email)) {
            throw new DuplicateEmailException("An account already exists with this email.");
        }

        int userId = userDAO.insertUser(name, email, phone, "TEACHER");
        userDAO.insertTeacherProfile(userId, collegeId, employeeId, department, subject, phone);
        return userId;
    }
}
