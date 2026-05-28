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

    /**
     * ✅ UPDATED: Now only finds user by username.
     * PASSWORD VERIFICATION IS HANDLED IN THE SERVICE LAYER (UserBean)
     */
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
     * ❌ REMOVED: findUser(String, String)
     * NEVER query for a user by password in the database again.
     */

    /**
     * ❌ REMOVED: validateUserCredentials(...)
     * This logic is now handled in the UserBean using PasswordHasher.verify().
     */

    public String getEmailByUsername(String username) {
        try {
            return getEm().createQuery(
                            "SELECT u.email FROM User u WHERE u.username = :user", String.class)
                    .setParameter("user", username)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    // =========================================================================
    // SECTION 2: VALIDATION & WHITELISTING
    // =========================================================================

    public boolean checkUserExists(String identifier) {
        if (identifier == null || identifier.trim().isEmpty()) return false;

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
        } catch (Exception e) {
            return null;
        }
    }
}