package campusnexus.service;

import campusnexus.dao.UserDAO;
import campusnexus.exception.AccountNotFoundException;
import campusnexus.exception.InvalidCredentialsException;
import campusnexus.model.Person;

import java.sql.SQLException;

public class AuthService {
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

    public void changePassword(int userId, String newPassword) throws SQLException {
        userDAO.updatePassword(userId, newPassword);
    }
}
