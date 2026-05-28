package app.dao;

import app.model.User;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.NoResultException;
import jakarta.transaction.Transactional;

@ApplicationScoped
@Transactional
public class UserDAO extends GenericDao<User, Integer> {

    // =========================================================================
    // SECTION 1: AUTHENTICATION & LOOKUP
    // =========================================================================

    public User findUser(String username, String password) {
        try {
            return getEm().createQuery(
                            "SELECT u FROM User u WHERE u.username = :user AND u.password = :pass", User.class)
                    .setParameter("user", username)
                    .setParameter("pass", password)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public User findUserByUsername(String username) {
        try {
            return getEm().createQuery(
                            "SELECT u FROM User u WHERE u.username = :user", User.class)
                    .setParameter("user", username)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    /**
     * ✅ NEW: Added for Jakarta Security Identity Store bridging.
     * Validates credentials and returns the user's uppercase role string.
     */
    public String validateUserCredentials(String username, String password) {
        User user = findUser(username, password);
        if (user != null && user.getRole() != null) {
            return user.getRole().toUpperCase();
        }
        return null;
    }

    /**
     * ✅ NEW: Added for FineScheduler email lookups.
     * Pulls the corresponding email address directly based on the borrower's username.
     */
    public String getEmailByUsername(String username) {
        try {
            return getEm().createQuery(
                            "SELECT u.email FROM User u WHERE u.username = :user", String.class)
                    .setParameter("user", username)
                    .getSingleResult();
        } catch (NoResultException e) {
            System.err.println("⚠️ UserDAO: No email found for username: " + username);
            return null;
        }
    }

    // =========================================================================
    // SECTION 2: VALIDATION & WHITELISTING
    // =========================================================================

    /**
     * Added to resolve Walk-In Student Verification requirements.
     * Checks if a user profile exists matching either the specified username or email.
     */
    public boolean checkUserExists(String identifier) {
        if (identifier == null || identifier.trim().isEmpty()) {
            return false;
        }

        Long count = getEm().createQuery(
                        "SELECT COUNT(u) FROM User u WHERE u.username = :id OR u.email = :id", Long.class)
                .setParameter("id", identifier.trim())
                .getSingleResult();

        return count > 0;
    }

    public boolean emailExists(String email) {
        Long count = getEm().createQuery(
                        "SELECT COUNT(u) FROM User u WHERE u.email = :email", Long.class)
                .setParameter("email", email)
                .getSingleResult();
        return count > 0;
    }

    public String getWhitelistedRole(String email) {
        try {
            return (String) getEm().createNativeQuery(
                            "SELECT assigned_role FROM authorized_emails WHERE email = ?")
                    .setParameter(1, email)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }
}