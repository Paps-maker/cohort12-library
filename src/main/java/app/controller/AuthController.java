package app.controller;

import app.dao.UserDAO;
import app.framework.ActionGetMethod;
import app.framework.ActionPostMethod;
import app.framework.Controller;
import app.framework.ModelAndView;
import app.model.User;
import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Controller
public class AuthController {

    @Inject
    private UserDAO userDAO;

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
        User user = userDAO.findUser(username, password);

        if (user != null) {
            // Prevent Session Fixation by invalidating existing session before creating a new one
            HttpSession oldSession = request.getSession(false);
            if (oldSession != null) {
                oldSession.invalidate();
            }

            HttpSession newSession = request.getSession(true);
            newSession.setAttribute("username", user.getUsername());
            newSession.setAttribute("role", user.getRole());

            return new ModelAndView("redirect:/books");
        }

        return new ModelAndView("redirect:/login?error=true");
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