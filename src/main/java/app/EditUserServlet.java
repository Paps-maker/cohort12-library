package app;

import app.dao.UserDAO;
import app.model.User;
import jakarta.inject.Inject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.io.PrintWriter;

@WebServlet("/edit-user")
public class EditUserServlet extends HttpServlet {

    @Inject
    private UserDAO userDAO; //  Use Managed Injection

    // 1. DISPLAY EDIT FORM
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String idParam = request.getParameter("id");
        if (idParam == null) {
            response.sendRedirect("members");
            return;
        }

        int id = Integer.parseInt(idParam);
        User user = userDAO.getUserById(id);

        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();

        out.println("<!DOCTYPE html><html><head><title>Edit User</title>");
        out.println("<style>");
        out.println("body { font-family: 'Segoe UI', sans-serif; background: #f4f6f8; display: flex; justify-content: center; align-items: center; min-height: 100vh; margin: 0; }");
        out.println(".card { background: white; padding: 30px; border-radius: 15px; width: 400px; box-shadow: 0 10px 25px rgba(0,0,0,0.1); }");
        out.println("h2 { text-align: center; color: #2c3e50; margin-bottom: 20px; }");
        out.println("input, select { width: 100%; padding: 12px; margin: 10px 0; border: 1px solid #ddd; border-radius: 8px; box-sizing: border-box; }");
        out.println(".btn { width: 100%; padding: 12px; background: #2a5298; color: white; border: none; border-radius: 8px; font-weight: bold; cursor: pointer; transition: 0.3s; }");
        out.println(".btn:hover { background: #1e3c72; }");
        out.println(".back { display: block; text-align: center; margin-top: 15px; color: #7f8c8d; text-decoration: none; font-size: 14px; }");
        out.println(".back:hover { color: #2a5298; }");
        out.println("</style></head><body>");

        out.println("<div class='card'>");
        out.println("<h2>Edit Member</h2>");

        if (user != null) {
            out.println("<form action='" + request.getContextPath() + "/edit-user' method='post'>");
            out.println("<input type='hidden' name='id' value='" + user.getId() + "'/>");

            out.println("<label style='font-size:12px; color:#7f8c8d;'>Username</label>");
            out.println("<input name='username' value='" + user.getUsername() + "' required/>");

            out.println("<label style='font-size:12px; color:#7f8c8d;'>Email Address</label>");
            out.println("<input name='email' type='email' value='" + user.getEmail() + "' required/>");

            out.println("<label style='font-size:12px; color:#7f8c8d;'>Account Role</label>");
            out.println("<select name='role'>");
            out.println("<option value='ADMIN' " + ("ADMIN".equals(user.getRole()) ? "selected" : "") + ">ADMIN</option>");
            out.println("<option value='USER' " + ("USER".equals(user.getRole()) ? "selected" : "") + ">USER</option>");
            out.println("</select>");

            out.println("<button class='btn' type='submit'>Update Account</button>");
            out.println("</form>");
        } else {
            out.println("<p style='color:red;'>User not found.</p>");
        }

        out.println("<a class='back' href='members'>← Back to Members</a>");
        out.println("</div></body></html>");
    }

    // 2. PROCESS UPDATE
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        try {
            int id = Integer.parseInt(request.getParameter("id"));
            String username = request.getParameter("username");
            String email = request.getParameter("email");
            String role = request.getParameter("role");

            // We pass null for password so the existing password isn't overwritten/cleared
            User updatedUser = new User(id, username, email, null, role);
            boolean success = userDAO.updateUser(updatedUser);

            if (success) {
                // Redirect back to members list with a success flag
                response.sendRedirect(request.getContextPath() + "/members?updated=true");
            } else {
                // If it fails, send back to form with an error (you could add an error param)
                response.sendRedirect(request.getContextPath() + "/edit-user?id=" + id + "&error=fail");
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect(request.getContextPath() + "/members");
        }
    }
}