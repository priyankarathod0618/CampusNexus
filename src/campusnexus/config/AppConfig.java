package campusnexus.config;

/**
 * App-wide constants that used to be duplicated across UI classes.
 */
public class AppConfig {
    // TODO for a real deployment: move this to an env var / properties file,
    // same as DatabaseConfig. A hardcoded code is only OK for a local demo.
    public static final String ADMIN_CODE = "ADMIN@123";

    private AppConfig() {
    }
}