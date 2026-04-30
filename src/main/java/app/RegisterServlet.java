package app;

import app.dao.UserDAO;
import app.model.User;
import jakarta.inject.Inject;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;

/**
 * SECURE REGISTRATION PROCESS
 * Uses an "Authorized Whitelist" strategy to prevent unauthorized sign-ups.
 */
@WebServlet("/registerProcess")
public class RegisterServlet extends HttpServlet {

    @Inject
    private UserDAO userDAO;

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Sanitize inputs
        String username = request.getParameter("username");
        String email = request.getParameter("email") != null ?
                request.getParameter("email").toLowerCase().trim() : "";
        String password = request.getParameter("password");

        // 1. WHITELIST VERIFICATION
        // Instead of checking the domain string, we check the database for pre-approval.
        String whitelistedRole = userDAO.getWhitelistedRole(email);

        if (whitelistedRole == null) {
            // SECURITY: The email is not in the 'authorized_emails' table.
            request.setAttribute("status", "not_authorized");
            request.getRequestDispatcher("registerdisplay.jsp").forward(request, response);
            return;
        }

        // 2. DUPLICATE CHECK
        // Ensure the authorized email hasn't already been used to create an account.
        if (userDAO.emailExists(email)) {
            request.setAttribute("status", "email_taken");
            request.getRequestDispatcher("registerdisplay.jsp").forward(request, response);
            return;
        }

        // 3. FINAL REGISTRATION
        // We use the 'whitelistedRole' retrieved from the DB, not a user-provided one.
        User newUser = new User(username, email, password, whitelistedRole);
        boolean success = userDAO.createUser(newUser);

        if (success) {
            request.setAttribute("username", username);
            request.setAttribute("email", email);
            request.setAttribute("status", "success");
        } else {
            request.setAttribute("status", "error");
        }

        request.getRequestDispatcher("registerdisplay.jsp").forward(request, response);
    }
}