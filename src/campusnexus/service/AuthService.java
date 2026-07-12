package campusnexus.service;

import campusnexus.dao.UserDAO;
import campusnexus.exception.AccountNotFoundException;
import campusnexus.exception.InvalidCredentialsException;
import campusnexus.exception.WeakPasswordException;
import campusnexus.model.Person;

import java.sql.SQLException;

public class AuthService {
    private static final int MIN_PASSWORD_LENGTH = 4;
    private final UserDAO userDAO = new UserDAO();

    public Person login(String email, String password)
            throws AccountNotFoundException, InvalidCredentialsException, SQLException {

        Person person = userDAO.findByEmail(email);
        if (person == null) {
            throw new AccountNotFoundException(
                    "No account found with this email. Please contact your college admin.");
        }
        if (!person.getPassword().equals(password)) {
            throw new InvalidCredentialsException("Incorrect password. Try again.");
        }
        return person;
    }

    public void changePassword(int userId, String newPassword) throws WeakPasswordException, SQLException {
        if (newPassword == null || newPassword.length() < MIN_PASSWORD_LENGTH) {
            throw new WeakPasswordException("Password must be at least " + MIN_PASSWORD_LENGTH + " characters.");
        }
        userDAO.updatePassword(userId, newPassword);
    }
}
