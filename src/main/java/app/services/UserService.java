package app.services;

import app.dao.UserDAO;
import app.model.User;
import app.events.LibraryEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;

import java.util.List;

@ApplicationScoped
public class UserService {

    @Inject
    private UserDAO userDAO;

    @Inject
    private Event<LibraryEvent> eventPublisher;

    /**
     * Handles user registration with validation + role enforcement.
     * Matches the User(username, email, password, role) constructor.
     */
    public String registerUser(String username, String email, String password) {

        if (username == null || username.trim().isEmpty()) {
            return "invalid_username";
        }

        if (email == null || email.trim().isEmpty()) {
            return "invalid_email";
        }

        if (password == null || password.trim().isEmpty()) {
            return "invalid_password";
        }

        String cleanEmail = email.toLowerCase().trim();
        String whitelistedRole = userDAO.getWhitelistedRole(cleanEmail);

        if (whitelistedRole == null) {
            return "not_authorized";
        }

        if (userDAO.emailExists(cleanEmail)) {
            return "email_taken";
        }

        // ✅ Matches your provided User model (4 arguments)
        User newUser = new User(username.trim(), cleanEmail, password, whitelistedRole);

        try {
            userDAO.save(newUser); // Uses GenericDao merge

            eventPublisher.fire(new LibraryEvent(
                    "REGISTER",
                    cleanEmail,
                    "Account Created Successfully as: " + whitelistedRole,
                    "Active"
            ));
            return "success";
        } catch (Exception e) {
            return "error";
        }
    }

    /**
     * Returns all system users.
     */
    public List<User> getAllUsers() {
        return userDAO.findAll(); // Uses GenericDao findAll
    }

    /**
     * Fetches a specific user by ID.
     */
    public User getUserById(int id) {
        return userDAO.findById(id); // Uses GenericDao findById
    }

    /**
     * Retrieves user details by username via UserDAO.
     */
    public User getUserDetails(String username) {
        return userDAO.findUserByUsername(username);
    }

    /**
     * Processes an update for an existing user record.
     */
    public boolean updateUser(User user) {
        try {
            userDAO.save(user); // Inherited from GenericDao
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Overloaded update for profile-specific changes.
     */
    public boolean updateUser(String username, String email, String password) {
        User existing = userDAO.findUserByUsername(username);
        if (existing == null) return false;

        existing.setEmail(email);

        if (password != null && !password.trim().isEmpty()) {
            existing.setPassword(password);
        }

        return updateUser(existing);
    }

    /**
     * Removes a user from the system.
     */
    public boolean deleteUser(int id) {
        try {
            userDAO.delete(id); // Uses GenericDao delete
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}