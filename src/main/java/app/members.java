package app;

import app.dao.UserDAO;
import app.model.User;
import jakarta.inject.Inject; //  Added for CDI
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*; //  Use HttpServlet
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

@WebServlet("/members")
 @AdminOnly // Ensure your custom annotation is correctly handled by a Filter/Interceptor
public class members extends HttpServlet { //  Changed to HttpServlet

    @Inject
    private UserDAO userDAO; //  Let WildFly manage the DAO

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();

        // No need for 'new UserDAO()'
        List<User> users = userDAO.getAllUsers();

        out.println("<!DOCTYPE html><html><head><title>Library Members</title>");

        // CSS Section
        out.println("<style>");
        out.println("body { font-family: 'Segoe UI', sans-serif; background: #f4f6f8; padding: 30px; }");
        out.println(".header { display:flex; justify-content:space-between; align-items:center; max-width:1000px; margin:auto; margin-bottom:20px; }");
        out.println(".container { max-width:1000px; margin:auto; }");
        out.println(".card { background:white; padding:20px; border-radius:15px; box-shadow:0 10px 25px rgba(0,0,0,0.08); }");
        out.println(".user { display:flex; justify-content:space-between; align-items:center; padding:15px; margin-bottom:10px; background:#f9fbfd; border-radius:10px; border: 1px solid #eee; }");
        out.println(".name { font-weight:bold; color:#2c3e50; font-size: 16px; }");
        out.println(".email { font-size:13px; color:#7f8c8d; }");
        out.println(".role { padding:5px 12px; border-radius:20px; font-size:11px; color:white; font-weight:bold; text-transform: uppercase; }");
        out.println(".ADMIN { background:#e74c3c; }");
        out.println(".USER { background:#3498db; }");
        out.println(".actions a { text-decoration:none; padding:6px 12px; margin-left:5px; border-radius:6px; font-size:12px; color:white; }");
        out.println(".edit { background:#27ae60; }");
        out.println(".delete { background:#e74c3c; }");
        out.println(".add-btn { background:#27ae60; color:white; padding:10px 15px; border-radius:8px; text-decoration:none; font-weight:bold; }");
        out.println(".back { background:#2a5298; color:white; padding:10px 15px; border-radius:8px; text-decoration:none; }");
        out.println("</style></head><body>");

        out.println("<div class='header'>");
        out.println("<h2>Library Members (" + users.size() + ")</h2>");
        out.println("<div>");
        out.println("<a class='add-btn' href='register.jsp'> Add Member</a>");
        out.println("<a class='back' href='books' style='margin-left:10px;'>Back to Books</a>");
        out.println("</div>");
        out.println("</div>");

        out.println("<div class='container'>");
        out.println("<div class='card'>");

        if (users.isEmpty()) {
            out.println("<p style='text-align:center; color:#7f8c8d;'>No members found in the database.</p>");
        } else {
            for (User u : users) {
                out.println("<div class='user'>");

                out.println("<div class='info'>");
                out.println("<div class='name'>" + u.getUsername() + "</div>");
                out.println("<div class='email'>" + u.getEmail() + "</div>");
                out.println("</div>");

                out.println("<div>");
                out.println("<span class='role " + u.getRole() + "'>" + u.getRole() + "</span>");
                out.println("<span class='actions' style='margin-left:20px;'>");
                out.println("<a class='edit' href='edit-user?id=" + u.getId() + "'>Edit</a>");
                out.println("<a class='delete' href='delete-user?id=" + u.getId() + "' onclick=\"return confirm('Delete " + u.getUsername() + "?')\">Delete</a>");
                out.println("</span>");
                out.println("</div>");

                out.println("</div>");
            }
        }

        out.println("</div>"); // close card
        out.println("</div>"); // close container
        out.println("</body></html>");
    }
}