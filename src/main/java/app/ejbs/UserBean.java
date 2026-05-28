package app.ejbs;

import app.dao.UserDAO;
import app.model.User;
import app.events.LibraryEvent;
import app.security.JwtUtil;
import app.security.PasswordHasher; // Ensure this utility class exists
import app.websocket.SystemActivityServer;
import jakarta.ejb.Stateless;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;

@Stateless
public class UserBean {

    @Inject
    private UserDAO userDAO;

    @Inject
    private Event<LibraryEvent> eventPublisher;


    //  USER REGISTRATION & SECURITY VALIDATION


    public String authenticate(String username, String password) {
        if (username == null || username.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            return "missing_credentials";
        }

        // Fetch user by username (DAO should NOT verify password in SQL)
        User user = userDAO.findUserByUsername(username.trim());

        // Verify password hash
        if (user == null || !PasswordHasher.verify(password, user.getPassword())) {
            SystemActivityServer.broadcastActivity(" SECURITY ALERT: Failed auth for: [" + username.trim() + "].");
            return "invalid_credentials";
        }

        String generatedToken = JwtUtil.generateToken(user.getUsername(), user.getRole());
        SystemActivityServer.broadcastActivity(" SECURITY ACCESS: Token established for [" + user.getUsername() + "]");

        return "Bearer " + generatedToken;
    }

    public String registerUser(String username, String email, String password) {
        if (username == null || username.trim().isEmpty() ||
                email == null || email.trim().isEmpty() ||
                password == null || password.trim().isEmpty()) {
            return "invalid_input";
        }

        String cleanEmail = email.toLowerCase().trim();
        String whitelistedRole = userDAO.getWhitelistedRole(cleanEmail);

        if (whitelistedRole == null) return "not_authorized";
        if (userDAO.emailExists(cleanEmail)) return "email_taken";

        try {
            // Hash password before saving
            String hashedPassword = PasswordHasher.hash(password);
            User newUser = new User(username.trim(), cleanEmail, hashedPassword, whitelistedRole);
            userDAO.save(newUser);

            eventPublisher.fire(new LibraryEvent("REGISTER", cleanEmail, "Created: " + whitelistedRole, "Active"));
            SystemActivityServer.broadcastActivity("👤 NEW USER: [" + username.trim() + "] registered.");

            return "success";
        } catch (Exception e) {
            return "error";
        }
    }


    //  DATA LOOKUPS & QUERIES


    public List<User> getAllUsers() { return userDAO.findAll(); }
    public User getUserById(int id) { return userDAO.findById(id); }
    public User getUserDetails(String username) { return userDAO.findUserByUsername(username); }

    public List<String> getBroadcastRecipientEmails() {
        return getAllUsers().stream()
                .map(User::getEmail)
                .filter(email -> email != null && !email.trim().isEmpty())
                .collect(Collectors.toList());
    }


    //  PROFILE MUTATIONS


    public boolean updateUser(User user) {
        try { userDAO.save(user); return true; } catch (Exception e) { return false; }
    }

    public boolean updateUser(String username, String email, String password) {
        User existing = userDAO.findUserByUsername(username);
        if (existing == null) return false;

        existing.setEmail(email);

        // Hash password only if it has changed/been provided
        if (password != null && !password.trim().isEmpty()) {
            existing.setPassword(PasswordHasher.hash(password));
        }

        if (updateUser(existing)) {
            SystemActivityServer.broadcastActivity(" PROFILE MODIFIED: [" + username + "].");
            return true;
        }
        return false;
    }

    public boolean deleteUser(int id) {
        try {
            userDAO.delete(id);
            SystemActivityServer.broadcastActivity(" SECURITY PURGE: User ID [" + id + "] removed.");
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}