package app.controller;

import app.ejbs.UserBean;
import app.framework.ActionGetMethod;
import app.framework.ActionPostMethod;
import app.framework.Controller;
import app.framework.ModelAndView;
import app.model.User;
import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Controller
public class AuthController {

    @Inject
    private UserBean userBean;

    // 1. DISPLAY LOGIN PAGE
    @ActionGetMethod("/login")
    public ModelAndView showLoginPage(HttpServletRequest request) {
        return new ModelAndView("login")
                .addObject("error", request.getParameter("error"));
    }

    // 2. PROCESS LOGIN
    @ActionPostMethod("/authenticate")
    public ModelAndView authenticate(HttpServletRequest request) {
        String username = request.getParameter("username");
        String password = request.getParameter("password");

        // Authenticate using the EJB, which handles the Hashing/Verification logic
        String authResult = userBean.authenticate(username, password);

        // Check if authentication succeeded (starts with "Bearer " based on your UserBean)
        if (authResult != null && authResult.startsWith("Bearer ")) {
            User user = userBean.getUserDetails(username);

            // Prevent Session Fixation
            HttpSession oldSession = request.getSession(false);
            if (oldSession != null) {
                oldSession.invalidate();
            }

            HttpSession newSession = request.getSession(true);
            newSession.setAttribute("username", user.getUsername());
            newSession.setAttribute("role", user.getRole());

            return new ModelAndView("redirect:/books");
        }

        // Encode the custom error message
        String errorMessage = "Invalid username or password";
        String encodedError = URLEncoder.encode(errorMessage, StandardCharsets.UTF_8);

        return new ModelAndView("redirect:/login?error=" + encodedError);
    }

    // 3. LOGOUT
    @ActionGetMethod("/logout")
    public ModelAndView logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        return new ModelAndView("redirect:/login");
    }
}