package campusnexus.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Salted SHA-256 password hashing.
 * Stored format: "<base64 salt>:<base64 hash>"
 *
 * verify() also accepts old plaintext passwords (no ":" in the stored value)
 * so existing seed-data / pre-upgrade accounts keep working - AuthService
 * re-hashes them automatically the first time they log in successfully.
 */
public class PasswordUtil {
    private static final String ALGORITHM = "SHA-256";
    private static final int SALT_LENGTH = 16;
    private static final SecureRandom RANDOM = new SecureRandom();

    private PasswordUtil() {
    }

    public static String hash(String plainPassword) {
        byte[] salt = new byte[SALT_LENGTH];
        RANDOM.nextBytes(salt);
        byte[] hash = digest(plainPassword, salt);
        return Base64.getEncoder().encodeToString(salt) + ":" + Base64.getEncoder().encodeToString(hash);
    }

    public static boolean verify(String plainPassword, String stored) {
        if (stored == null) {
            return false;
        }
        if (!stored.contains(":")) {
            // Legacy plaintext password (pre-hashing accounts / seed data)
            return stored.equals(plainPassword);
        }
        String[] parts = stored.split(":", 2);
        byte[] salt = Base64.getDecoder().decode(parts[0]);
        byte[] expectedHash = Base64.getDecoder().decode(parts[1]);
        byte[] actualHash = digest(plainPassword, salt);
        return MessageDigest.isEqual(expectedHash, actualHash);
    }

    public static boolean isLegacyPlaintext(String stored) {
        return stored != null && !stored.contains(":");
    }

    private static byte[] digest(String plainPassword, byte[] salt) {
        try {
            MessageDigest md = MessageDigest.getInstance(ALGORITHM);
            md.update(salt);
            return md.digest(plainPassword.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}