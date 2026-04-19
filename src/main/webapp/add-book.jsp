<%@ page import="app.Book, app.dao.BookDAO" %>

<!DOCTYPE html>
<html>
<head>
    <title>Add Book</title>

    <style>
        body {
            font-family: 'Segoe UI', sans-serif;
            background: #f4f6f8;
            margin: 0;
            display: flex;
            justify-content: center;
            align-items: center;
            min-height: 100vh;
            padding: 20px;
        }

        .box {
            background: white;
            padding: 30px;
            border-radius: 12px;
            width: 450px;
            box-shadow: 0 5px 15px rgba(0,0,0,0.1);
            text-align: center;
        }

        h2 { color: #2c3e50; margin-bottom: 20px; }

        input, textarea {
            width: 100%;
            padding: 12px;
            margin: 10px 0;
            border: 1px solid #ddd;
            border-radius: 8px;
            outline: none;
            box-sizing: border-box;
            font-family: inherit;
        }

        /* ✅ Styling for the new Description box */
        textarea {
            resize: vertical;
            min-height: 80px;
        }

        #imgPreview {
            width: 100px;
            height: 140px;
            object-fit: cover;
            border-radius: 5px;
            margin: 10px auto;
            display: none;
            border: 2px solid #eee;
        }

        button {
            width: 100%;
            padding: 12px;
            border: none;
            border-radius: 8px;
            background: #2a5298;
            color: white;
            font-weight: bold;
            cursor: pointer;
            transition: 0.3s;
            margin-top: 10px;
        }

        button:hover { background: #1e3c72; }
        .success { color: green; margin-top: 10px; font-weight: bold; }
        .error { color: red; margin-top: 10px; font-weight: bold; }

        .back {
            display: inline-block;
            margin-top: 15px;
            padding: 10px 15px;
            background: #27ae60;
            color: white;
            text-decoration: none;
            border-radius: 8px;
            transition: 0.3s;
        }
        .back:hover { background: #1e8449; }
    </style>
</head>

<body>

<div class="box">
    <h2>Add New Book</h2>

    <form method="post">
        <input type="text" name="title" placeholder="Enter Book Title" required>

        <input type="text" name="imageUrl" id="urlInput" placeholder="Enter Image URL" oninput="previewImage()">

        <textarea name="description" placeholder="Enter Book Description/Summary..."></textarea>

        <img id="imgPreview" src="" alt="Preview">

        <button type="submit">Add Book</button>
    </form>

    <script>
        function previewImage() {
            const url = document.getElementById('urlInput').value;
            const img = document.getElementById('imgPreview');
            if(url) {
                img.src = url;
                img.style.display = 'block';
            } else {
                img.style.display = 'none';
            }
        }
    </script>

<%
    String title = request.getParameter("title");
    String imageUrl = request.getParameter("imageUrl");
    String description = request.getParameter("description"); //  Capture description

    if (title != null && !title.trim().isEmpty()) {
        BookDAO dao = new BookDAO();

        //  Use the 3-parameter constructor (title, imageUrl, description)
        Book newBook = new Book(title, imageUrl, description);
        boolean added = dao.addBook(newBook);

        if (added) {
%>
            <p class="success">Book Added Successfully!</p>
            <script>
                setTimeout(() => { window.location.href = "books"; }, 1500);
            </script>
<%
        } else {
%>
            <p class="error">Failed to add book. Check DB connection.</p>
<%
        }
    }
%>

    <a class="back" href="books">Back to Book List</a>
</div>

</body>
</html>