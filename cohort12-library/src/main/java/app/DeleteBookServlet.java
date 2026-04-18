package app;

import app.dao.BookDAO;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;

@WebServlet("/delete-book")
public class DeleteBookServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        // ✅ CHECK SESSION
        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute("role") == null) {
            response.sendRedirect("login");
            return;
        }

        String role = (String) session.getAttribute("role");

        // ✅ ONLY ADMIN CAN DELETE
        if (!"ADMIN".equals(role)) {
            response.sendRedirect("books");
            return;
        }

        try {
            int id = Integer.parseInt(request.getParameter("id"));

            BookDAO dao = new BookDAO();
            boolean deleted = dao.deleteBook(id);

            if (deleted) {
                System.out.println("✅ Book deleted: ID " + id);
            } else {
                System.out.println("⚠ Delete failed for ID " + id);
            }

        } catch (Exception e) {
            System.out.println("❌ Invalid delete request");
            e.printStackTrace();
        }

        response.sendRedirect("books");
    }
}