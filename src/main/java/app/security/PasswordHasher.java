package app.security;
import org.mindrot.jbcrypt.BCrypt;

public class PasswordHasher {
    public static String hash(String password) {
        // 12 is the work factor (cost). Higher is more secure but slower.
        return BCrypt.hashpw(password, BCrypt.gensalt(12));
    }

    public static boolean verify(String password, String hashed) {
        return BCrypt.checkpw(password, hashed);
    }
}