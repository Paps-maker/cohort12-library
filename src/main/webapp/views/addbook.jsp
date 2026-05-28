<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Library | Add Book</title>
    <style>
        @import url('https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght=400;600;800&display=swap');
        body { font-family: 'Plus Jakarta Sans', sans-serif; background: #f8fafc; display: flex; justify-content: center; align-items: center; min-height: 100vh; margin: 0; }
        .box { background: white; padding: 40px; border-radius: 24px; width: 100%; max-width: 450px; box-shadow: 0 20px 25px -5px rgba(0,0,0,0.1); border: 1px solid #e2e8f0; }
        h2 { color: #0f172a; margin-bottom: 24px; font-weight: 800; letter-spacing: -1px; text-align: center; }
        input, textarea { width: 100%; padding: 14px; margin: 12px 0; border: 1px solid #e2e8f0; border-radius: 12px; box-sizing: border-box; font-size: 15px; font-family: inherit; transition: 0.2s; }
        input:focus, textarea:focus { outline: none; border-color: #2563eb; }
        .btn-primary { width: 100%; padding: 16px; border: none; border-radius: 12px; background: #0f172a; color: white; font-weight: 700; cursor: pointer; margin-top: 10px; transition: 0.2s; }
        .btn-primary:hover { background: #1e293b; transform: translateY(-1px); }
        .label-left { text-align: left; display: block; font-weight: 700; color: #475569; font-size: 12px; margin-top: 10px; text-transform: uppercase; letter-spacing: 0.5px; }
    </style>
</head>
<body>
<div class="box">
    <h2>Add New Book</h2>
    <form action="<%= request.getContextPath() %>/library/addbook" method="POST">
        <label class='label-left'>Book Details</label>
        <input type="text" name="title" placeholder="Book Title" required>
        <input type="text" name="author" placeholder="Author Name" required>
        <input type="text" name="isbn" placeholder="ISBN Number" required>
        <input type="text" name="imageUrl" placeholder="Cover Image URL (Optional)">
        <textarea name="description" placeholder="Short Synopsis..." rows="3"></textarea>
        <label class='label-left'>Inventory</label>
        <input type="number" name="copies" value="1" min="1" max="50" required>
        <button type="submit" class="btn-primary">Save to Database</button>
        <a href="<%= request.getContextPath() %>/books" style="display:block; text-align:center; margin-top:15px; font-size:13px; color:#64748b; text-decoration:none;">Cancel</a>
    </form>
</div>
</body>
</html>