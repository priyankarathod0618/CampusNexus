package campusnexus.exception;

/**
 * Thrown by the service layer when data fails validation (bad name, phone,
 * email, roll number, email/college-domain mismatch, etc.). Kept separate
 * from the UI-layer InputValidator loops so every entry point into
 * AdminService (console menu today, any other caller tomorrow) is protected
 * even if a caller forgets to pre-validate.
 */
public class InvalidDataException extends Exception {
    public InvalidDataException(String message) {
        super(message);
    }
}
