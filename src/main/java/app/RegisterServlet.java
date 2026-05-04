package app;

import app.dao.UserDAO;
import app.model.User;
import app.events.LibraryEvent; // ✅ Added Import
import jakarta.enterprise.event.Event; // ✅ Added Import
import jakarta.inject.Inject;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;

/**
 * SECURE REGISTRATION PROCESS
 * Now includes automated Email Notifications upon successful signup.
 */
@WebServlet("/registerProcess")
public class RegisterServlet extends HttpServlet {

    @Inject
    private UserDAO userDAO;

    @Inject
    private Event<LibraryEvent> eventPublisher; // ✅ Injected for Email Notifications

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Sanitize inputs
        String username = request.getParameter("username");
        String email = request.getParameter("email") != null ?
                request.getParameter("email").toLowerCase().trim() : "";
        String password = request.getParameter("password");

        // 1. WHITELIST VERIFICATION
        String whitelistedRole = userDAO.getWhitelistedRole(email);

        if (whitelistedRole == null) {
            request.setAttribute("status", "not_authorized");
            request.getRequestDispatcher("registerdisplay.jsp").forward(request, response);
            return;
        }

        // 2. DUPLICATE CHECK
        if (userDAO.emailExists(email)) {
            request.setAttribute("status", "email_taken");
            request.getRequestDispatcher("registerdisplay.jsp").forward(request, response);
            return;
        }

        // 3. FINAL REGISTRATION
        User newUser = new User(username, email, password, whitelistedRole);
        boolean success = userDAO.createUser(newUser);

        if (success) {
            // ✅ FIRE WELCOME EMAIL EVENT
            // This triggers your LibraryObserver to send the Gmail notification
            eventPublisher.fire(new LibraryEvent(
                    "REGISTER",
                    email,
                    "Account Created Successfully as: " + whitelistedRole,
                    "Active"
            ));

            request.setAttribute("username", username);
            request.setAttribute("email", email);
            request.setAttribute("status", "success");
        } else {
            request.setAttribute("status", "error");
        }

        request.getRequestDispatcher("registerdisplay.jsp").forward(request, response);
    }
}