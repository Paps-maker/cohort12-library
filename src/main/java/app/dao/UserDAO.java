package app.dao;

import app.model.User;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.NoResultException;
import jakarta.transaction.Transactional;

/**
 * DATA ACCESS OBJECT: USERS
 * Optimized for the Assessment-1-Livingstone Project.
 * Standard CRUD is inherited from GenericDao.
 */
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

    // =========================================================================
    // SECTION 2: VALIDATION & WHITELISTING
    // =========================================================================

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