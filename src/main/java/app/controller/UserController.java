package app.controller;

import app.framework.ActionGetMethod;
import app.framework.ActionPostMethod;
import app.framework.ModelAndView;
import app.model.User;
import app.ejbs.UserBean;
import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.util.List;
import app.framework.Controller;

@Controller
public class UserController {

    @Inject
    private UserBean userBean;

    // Helper method to standardize security checks
    private boolean isAdmin(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        return session != null && "ADMIN".equals(session.getAttribute("role"));
    }

    // =========================================================================
    // 1. LIST VIEW (MEMBERS DASHBOARD)
    // =========================================================================
    @ActionGetMethod("/members")
    public ModelAndView viewMembers(HttpServletRequest req) {
        if (!isAdmin(req)) {
            return new ModelAndView("redirect:/login");
        }

        return new ModelAndView("/views/members.jsp")
                .addObject("users", userBean.getAllUsers());
    }

    // 2. EDIT VIEW (MODERN FORM)
    @ActionGetMethod("/edit-user")
    public ModelAndView showEditForm(HttpServletRequest req) {
        if (!isAdmin(req)) {
            return new ModelAndView("redirect:/members");
        }

        String idParam = req.getParameter("id");
        if (idParam == null) return new ModelAndView("redirect:/members");

        return new ModelAndView("/views/edit-user.jsp")
                .addObject("user", userBean.getUserById(Integer.parseInt(idParam)));
    }

    // 3. LOGIC HANDLERS (POST)
    @ActionPostMethod("/edit-user")
    public ModelAndView processUpdate(HttpServletRequest req) {
        if (!isAdmin(req)) return new ModelAndView("redirect:/members");

        try {
            int id = Integer.parseInt(req.getParameter("id"));
            User existingUser = userBean.getUserById(id);

            if (existingUser != null) {
                User updated = new User(
                        id,
                        req.getParameter("username"),
                        req.getParameter("email"),
                        existingUser.getPassword(), // Preserve existing password
                        req.getParameter("role")
                );
                userBean.updateUser(updated);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ModelAndView("redirect:/members");
    }

    @ActionPostMethod("/delete-user")
    public ModelAndView processDelete(HttpServletRequest req) {
        if (!isAdmin(req)) return new ModelAndView("redirect:/members");

        String idParam = req.getParameter("id");
        if (idParam != null) {
            userBean.deleteUser(Integer.parseInt(idParam));
        }
        return new ModelAndView("redirect:/members");
    }
}