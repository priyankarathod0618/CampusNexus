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

        if (email == null || email.isBlank() || password == null || password.isBlank()) {
            throw new InvalidCredentialsException("Email and password are required.");
        }

        Person person = userDAO.findByEmail(email.trim());

        if (person == null) {
            throw new AccountNotFoundException(
                    "No account found with this email. Please contact your college admin.");
        }

        // BUGFIX: accounts created by AdminService store PasswordUtil.hash(phone),
        // so this must verify against the hash, not do a plaintext String.equals().
        if (!PasswordUtil.verify(password, person.getPassword())) {
            throw new InvalidCredentialsException("Incorrect password. Try again.");
        }

        // Transparently upgrade any legacy plaintext password to a salted hash
        // now that we know the plaintext was correct.
        if (PasswordUtil.isLegacyPlaintext(person.getPassword())) {
            String upgradedHash = PasswordUtil.hash(password);
            userDAO.updatePasswordHashOnly(person.getId(), upgradedHash);
            person.setPassword(upgradedHash);
        }

        return person;
    }

    public void changePassword(int userId, String newPassword)
            throws WeakPasswordException, SQLException {

        if (newPassword == null || newPassword.trim().length() < MIN_PASSWORD_LENGTH) {
            throw new WeakPasswordException(
                    "Password must be at least " + MIN_PASSWORD_LENGTH + " characters.");
        }
        if (newPassword.contains(" ")) {
            throw new WeakPasswordException("Password must not contain spaces.");
        }

        // BUGFIX: this used to store newPassword as plaintext, silently
        // downgrading the account back out of the hashed scheme.
        userDAO.updatePassword(userId, PasswordUtil.hash(newPassword));
    }
}