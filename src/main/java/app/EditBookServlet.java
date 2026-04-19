package app;

import app.dao.BookDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.io.PrintWriter;

@WebServlet("/edit-book")
public class EditBookServlet extends HttpServlet {

    BookDAO dao = new BookDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);

        //  SECURITY
        if (session == null || !"ADMIN".equals(session.getAttribute("role"))) {
            response.sendRedirect("books");
            return;
        }

        int id = Integer.parseInt(request.getParameter("id"));
        Book book = dao.getBookById(id);

        response.setContentType("text/html");
        PrintWriter out = response.getWriter();

        out.println("<!DOCTYPE html>");
        out.println("<html><head><title>Edit Book</title>");

        out.println("<style>");
        out.println("body { font-family:'Segoe UI'; background:#f4f6f8; display:flex; justify-content:center; align-items:center; min-height:100vh; margin:0;}");
        out.println(".card { background:white; padding:30px; border-radius:12px; width:450px; box-shadow:0 4px 12px rgba(0,0,0,0.1); text-align:center;} ");
        out.println("h2 { margin-bottom:15px; color:#2c3e50; }");
        out.println("label { display:block; text-align:left; font-weight:600; color:#555; font-size:14px; margin-top:10px;}");
        out.println("input, textarea { width:100%; padding:12px; margin:8px 0; border-radius:6px; border:1px solid #ccc; box-sizing:border-box; font-family:inherit;}");

        // Styling the textarea
        out.println("textarea { resize: vertical; min-height: 100px; }");

        out.println(".preview-img { width:120px; height:160px; object-fit:cover; margin:10px auto; border-radius:8px; display:block; border:1px solid #ddd; background:#eee;}");
        out.println(".btn { width:100%; padding:12px; border:none; border-radius:6px; font-weight:bold; cursor:pointer; margin-top:10px;}");
        out.println(".update-btn { background:#27ae60; color:white; }");
        out.println(".update-btn:hover { background:#1e8449; }");
        out.println(".back-btn { display:block; text-align:center; margin-top:15px; text-decoration:none; color:#2a5298; font-weight:bold; font-size:14px;}");
        out.println("</style>");

        out.println("</head><body>");

        out.println("<div class='card'>");
        out.println("<h2>Edit Book Details</h2>");

        // Current Image Preview
        String currentImg = (book.getImageUrl() != null && !book.getImageUrl().isEmpty()) ? book.getImageUrl() : "https://via.placeholder.com/120x160?text=No+Cover";
        out.println("<img id='imgPreview' class='preview-img' src='" + currentImg + "' />");

        out.println("<form method='post'>");

        out.println("<input type='hidden' name='id' value='" + book.getId() + "'/>");

        out.println("<label>Book Title</label>");
        out.println("<input name='title' value='" + book.getTitle() + "' required/>");

        out.println("<label>Cover Image URL</label>");
        out.println("<input name='imageUrl' id='urlInput' value='" + (book.getImageUrl() != null ? book.getImageUrl() : "") + "' oninput='updatePreview()'/>");

        //  NEW DESCRIPTION FIELD
        out.println("<label>Description</label>");
        out.println("<textarea name='description' placeholder='Edit summary...'>" + (book.getDescription() != null ? book.getDescription() : "") + "</textarea>");

        out.println("<button class='btn update-btn'>Update Book</button>");
        out.println("</form>");

        out.println("<a class='back-btn' href='books'> Back to Library</a>");

        out.println("<script>");
        out.println("function updatePreview(){");
        out.println("  const url = document.getElementById('urlInput').value;");
        out.println("  const img = document.getElementById('imgPreview');");
        out.println("  if(url) img.src = url;");
        out.println("}");
        out.println("</script>");

        out.println("</div>");
        out.println("</body></html>");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        HttpSession session = request.getSession(false);

        if (session == null || !"ADMIN".equals(session.getAttribute("role"))) {
            response.sendRedirect("books");
            return;
        }

        try {
            int id = Integer.parseInt(request.getParameter("id"));
            String title = request.getParameter("title");
            String imageUrl = request.getParameter("imageUrl");
            String description = request.getParameter("description"); //  Capture Description

            BookDAO dao = new BookDAO();

            //  Use the 4-parameter constructor (id, title, imageUrl, description)
            dao.updateBook(new Book(id, title, imageUrl, description));

            response.sendRedirect("books");

        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect("books?error=update_failed");
        }
    }
}