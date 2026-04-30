package app;

import app.dao.BookDAO;
import app.model.Book;
import jakarta.inject.Inject; // ✅ Required for Injection
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.io.PrintWriter;

@WebServlet("/edit-book")
public class EditBookServlet extends HttpServlet {

    //  WildFly manages this instance; DataSource will be properly injected
    @Inject
    private BookDAO bookDAO;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        String role = (session != null) ? (String) session.getAttribute("role") : null;
        String contextPath = request.getContextPath();

        //  SECURITY: Only ADMIN can see the edit form
        if (!"ADMIN".equals(role)) {
            response.sendRedirect(contextPath + "/books");
            return;
        }

        String idStr = request.getParameter("id");
        if (idStr == null) {
            response.sendRedirect(contextPath + "/books");
            return;
        }

        int id = Integer.parseInt(idStr);
        //  Use the injected bookDAO
        Book book = bookDAO.getBookById(id);

        if (book == null) {
            response.sendRedirect(contextPath + "/books?error=not_found");
            return;
        }

        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();

        out.println("<!DOCTYPE html><html><head><title>Edit Book</title>");
        out.println("<style>");
        out.println("body { font-family:'Segoe UI', sans-serif; background:#f4f6f8; display:flex; justify-content:center; align-items:center; min-height:100vh; margin:0;}");
        out.println(".card { background:white; padding:30px; border-radius:12px; width:450px; box-shadow:0 4px 12px rgba(0,0,0,0.1); text-align:center;} ");
        out.println("h2 { margin-bottom:15px; color:#2c3e50; }");
        out.println("label { display:block; text-align:left; font-weight:600; color:#555; font-size:14px; margin-top:10px;}");
        out.println("input, textarea { width:100%; padding:12px; margin:8px 0; border-radius:6px; border:1px solid #ccc; box-sizing:border-box; font-family:inherit;}");
        out.println(".preview-img { width:120px; height:160px; object-fit:cover; margin:10px auto; border-radius:8px; display:block; border:1px solid #ddd; background:#eee;}");
        out.println(".update-btn { width:100%; padding:12px; border:none; border-radius:6px; font-weight:bold; cursor:pointer; background:#27ae60; color:white; margin-top:10px;}");
        out.println(".back-btn { display:block; text-align:center; margin-top:15px; text-decoration:none; color:#2a5298; font-weight:bold; font-size:14px;}");
        out.println("</style></head><body>");

        out.println("<div class='card'>");
        out.println("<h2>Edit Book Details</h2>");

        String currentImg = (book.getImageUrl() != null && !book.getImageUrl().isEmpty()) ? book.getImageUrl() : "https://via.placeholder.com/120x160?text=No+Cover";
        out.println("<img id='imgPreview' class='preview-img' src='" + currentImg + "' />");

        // ✅ Explicitly set the form action
        out.println("<form action='" + contextPath + "/edit-book' method='post'>");
        out.println("<input type='hidden' name='id' value='" + book.getId() + "'/>");

        out.println("<label>Book Title</label>");
        out.println("<input name='title' value='" + book.getTitle() + "' required/>");

        out.println("<label>Cover Image URL</label>");
        out.println("<input name='imageUrl' id='urlInput' value='" + (book.getImageUrl() != null ? book.getImageUrl() : "") + "' oninput='updatePreview()'/>");

        out.println("<label>Description</label>");
        out.println("<textarea name='description' rows='4'>" + (book.getDescription() != null ? book.getDescription() : "") + "</textarea>");

        out.println("<button class='update-btn' type='submit'>Save Changes</button>");
        out.println("</form>");

        out.println("<a class='back-btn' href='books'>← Back to Library</a>");

        out.println("<script>function updatePreview(){const url = document.getElementById('urlInput').value; const img = document.getElementById('imgPreview'); if(url) img.src = url;}</script>");
        out.println("</div></body></html>");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        HttpSession session = request.getSession(false);
        String role = (session != null) ? (String) session.getAttribute("role") : null;
        String contextPath = request.getContextPath();

        if (!"ADMIN".equals(role)) {
            response.sendRedirect(contextPath + "/books");
            return;
        }

        try {
            int id = Integer.parseInt(request.getParameter("id"));
            String title = request.getParameter("title");
            String imageUrl = request.getParameter("imageUrl");
            String description = request.getParameter("description");

            // ✅ Use the injected bookDAO
            boolean updated = bookDAO.updateBook(new Book(id, title, imageUrl, description));

            if (updated) {
                response.sendRedirect(contextPath + "/books?success=updated");
            } else {
                response.sendRedirect(contextPath + "/edit-book?id=" + id + "&error=failed");
            }

        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect(contextPath + "/books?error=system_error");
        }
    }
}