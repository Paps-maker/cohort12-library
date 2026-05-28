<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="app.model.Book" %>
<%
    Book book = (Book) request.getAttribute("book");
    String contextPath = request.getContextPath();
%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Edit Book</title>
    <style>
        @import url('https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght=400;500;600;700;800&display=swap');
        :root { --secondary: #2a5298; }
        body { font-family: 'Plus Jakarta Sans', sans-serif; margin:0; background:#ffffff; color:#334155; }
        .header { background: linear-gradient(135deg, #1e3c72, #2a5298); color:white; padding:25px; text-align:center; font-size:24px; font-weight:800; letter-spacing: 1px; text-transform: uppercase; }
        .container { max-width: 1200px; margin: auto; padding: 20px; }
        .form-wrapper { max-width: 550px; margin: 30px auto; background: white; padding: 35px; border-radius: 20px; box-shadow: 0 10px 25px rgba(0,0,0,0.05); border: 1px solid #e2e8f0; }
        .add-form { display: flex; flex-direction: column; gap: 18px; }
        .input-group { display: flex; flex-direction: column; gap: 8px; }
        .input-group label { font-size: 12px; font-weight: 800; color: #64748b; text-transform: uppercase; }
        .input-group input, .input-group textarea { padding: 14px; border: 1.5px solid #e2e8f0; border-radius: 10px; font-family: inherit; font-size: 14px; box-sizing: border-box; width: 100%; }
        .input-group input:focus { border-color: var(--secondary); outline: none; }
        .form-actions { display: flex; gap: 12px; margin-top: 15px; }
        .btn-save { flex: 2; background: #10b981; color: white; border: none; padding: 14px; border-radius: 10px; font-weight: 700; cursor: pointer; font-size: 15px; }
        .btn-cancel { flex: 1; text-align: center; background: #f8fafc; color: #64748b; text-decoration: none; padding: 14px; border-radius: 10px; font-weight: 700; border: 1.5px solid #e2e8f0; cursor: pointer; }
    </style>
</head>
<body>
<% if (book != null) { %>
    <div class="header"> EDIT: <%= book.getTitle().toUpperCase() %> </div>
    <div class="container">
        <div class="form-wrapper">
            <form action="<%= contextPath %>/update-book" method="POST" class="add-form">
                <input type="hidden" name="id" value="<%= book.getId() %>"/>
                <div class="input-group">
                    <label>Book Title</label>
                    <input type="text" name="title" value="<%= book.getTitle() %>" required>
                </div>
                <div class="input-group">
                    <label>ISBN / ISDN Code</label>
                    <input type="text" name="isbn" value="<%= book.getIsbn() != null ? book.getIsbn() : "" %>" placeholder="e.g. 978-3-16-148410-0" required>
                </div>
                <div class="input-group">
                    <label>Image URL</label>
                    <input type="url" name="imageUrl" value="<%= book.getImageUrl() != null ? book.getImageUrl() : "" %>">
                </div>
                <div class="input-group">
                    <label>Current Quantity: <%= book.getQuantity() %> (Add extra copies below)</label>
                    <input type="number" name="addCopies" value="0" min="0">
                </div>
                <div class="input-group">
                    <label>Description</label>
                    <textarea name="description" rows="4"><%= book.getDescription() != null ? book.getDescription() : "" %></textarea>
                </div>
                <div class="form-actions">
                    <button type="submit" class="btn-save">Save Changes</button>
                    <a href="<%= contextPath %>/books" class="btn-cancel">Cancel</a>
                </div>
            </form>
        </div>
    </div>
<% } %>
</body>
</html>