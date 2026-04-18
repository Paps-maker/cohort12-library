package app;

import app.dao.UserDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.io.PrintWriter;

@WebServlet("/edit-user")
public class EditUserServlet extends HttpServlet {

    UserDAO dao = new UserDAO();

    // SHOW EDIT FORM
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        int id = Integer.parseInt(request.getParameter("id"));
        User user = dao.getUserById(id);

        response.setContentType("text/html");
        PrintWriter out = response.getWriter();

        out.println("<!DOCTYPE html>");
        out.println("<html><head><title>Edit User</title>");

        // ================= CSS =================
        out.println("<style>");

        out.println("body {"
                + "font-family: 'Segoe UI', sans-serif;"
                + "background: linear-gradient(135deg, #1e3c72, #2a5298);"
                + "display:flex; justify-content:center; align-items:center;"
                + "height:100vh; margin:0;"
                + "}");

        out.println(".card {"
                + "background:#fff;"
                + "padding:30px;"
                + "border-radius:15px;"
                + "width:400px;"
                + "box-shadow:0 10px 25px rgba(0,0,0,0.2);"
                + "}");

        out.println("h2 { text-align:center; color:#2c3e50; margin-bottom:20px; }");

        out.println("input, select {"
                + "width:100%;"
                + "padding:12px;"
                + "margin:10px 0;"
                + "border:1px solid #ddd;"
                + "border-radius:8px;"
                + "outline:none;"
                + "transition:0.3s;"
                + "}");

        out.println("input:focus, select:focus { border-color:#2a5298; box-shadow:0 0 5px rgba(42,82,152,0.4); }");

        out.println(".btn {"
                + "width:100%;"
                + "padding:12px;"
                + "background:#2a5298;"
                + "color:white;"
                + "border:none;"
                + "border-radius:8px;"
                + "font-size:16px;"
                + "cursor:pointer;"
                + "transition:0.3s;"
                + "}");

        out.println(".btn:hover { background:#1e3c72; }");

        out.println(".back {"
                + "display:block;"
                + "text-align:center;"
                + "margin-top:15px;"
                + "color:#2a5298;"
                + "text-decoration:none;"
                + "}");

        out.println(".back:hover { text-decoration:underline; }");

        out.println("</style>");
        // =======================================

        out.println("</head><body>");

        out.println("<div class='card'>");

        out.println("<h2> Edit User</h2>");

        out.println("<form method='post'>");

        out.println("<input type='hidden' name='id' value='" + user.getId() + "'/>");

        out.println("<input name='username' value='" + user.getUsername() + "' placeholder='Username' required/>");

        out.println("<input name='email' value='" + user.getEmail() + "' placeholder='Email' required/>");

        out.println("<select name='role'>");
        out.println("<option " + ("ADMIN".equals(user.getRole()) ? "selected" : "") + ">ADMIN</option>");
        out.println("<option " + ("USER".equals(user.getRole()) ? "selected" : "") + ">USER</option>");
        out.println("</select>");

        out.println("<button class='btn' type='submit'>Update User</button>");

        out.println("</form>");

        out.println("<a class='back' href='members'> Back to Members</a>");

        out.println("</div>");

        out.println("</body></html>");
    }

    // UPDATE USER
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        int id = Integer.parseInt(request.getParameter("id"));
        String username = request.getParameter("username");
        String email = request.getParameter("email");
        String role = request.getParameter("role");

        dao.updateUser(new User(id, username, email, null, role));

        response.sendRedirect("members");
    }
}