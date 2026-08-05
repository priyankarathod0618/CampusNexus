package campusnexus.service;

import campusnexus.dao.UserDAO;
import campusnexus.exception.AccountNotFoundException;
import campusnexus.exception.InvalidCredentialsException;
import campusnexus.exception.WeakPasswordException;
import campusnexus.model.Person;
import campusnexus.util.PasswordUtil;

import java.sql.SQLException;

public class AuthService {
    private static final int MIN_PASSWORD_LENGTH = 6;
    private final UserDAO userDAO = new UserDAO();

    public Person login(String email, String password)
            throws AccountNotFoundException, InvalidCredentialsException, SQLException {

        Person person = userDAO.findByEmail(email);

        // ===== DEBUG =====
        System.out.println("Person Found: " + (person != null));

        if (person != null) {
            System.out.println("Stored Password = [" + person.getPassword() + "]");
            System.out.println("Entered Password = [" + password + "]");
            System.out.println("Verify = " + PasswordUtil.verify(password, person.getPassword()));
        }
        // =================

        if (person == null) {
            throw new AccountNotFoundException(
                    "No account found with this email. Please contact your college admin.");
        }

        if (!PasswordUtil.verify(password, person.getPassword())) {
            throw new InvalidCredentialsException("Incorrect password. Try again.");
        }

        if (PasswordUtil.isLegacyPlaintext(person.getPassword())) {
            String upgraded = PasswordUtil.hash(password);
            userDAO.updatePasswordHashOnly(person.getId(), upgraded);
            person.setPassword(upgraded);
        }

        return person;
    }
    public void changePassword(int userId, String newPassword) throws WeakPasswordException, SQLException {
        if (newPassword == null || newPassword.length() < MIN_PASSWORD_LENGTH) {
            throw new WeakPasswordException("Password must be at least " + MIN_PASSWORD_LENGTH + " characters.");
        }
        userDAO.updatePassword(userId, PasswordUtil.hash(newPassword));
    }
}