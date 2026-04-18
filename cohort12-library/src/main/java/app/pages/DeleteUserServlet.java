package app;

import app.dao.UserDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;

@WebServlet("/delete-user")
public class DeleteUserServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String idStr = request.getParameter("id");

        if (idStr != null) {

            int id = Integer.parseInt(idStr);

            UserDAO dao = new UserDAO();
            boolean deleted = dao.deleteUser(id);

            if (deleted) {
                System.out.println("USER DELETED: " + id);
            } else {
                System.out.println("DELETE FAILED: " + id);
            }
        }

        response.sendRedirect("members");
    }
}