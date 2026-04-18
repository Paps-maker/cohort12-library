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
            height: 100vh;
        }

        .box {
            background: white;
            padding: 25px;
            border-radius: 12px;
            width: 400px;
            box-shadow: 0 5px 15px rgba(0,0,0,0.1);
            text-align: center;
        }

        h2 {
            color: #2c3e50;
            margin-bottom: 15px;
        }

        input {
            width: 100%;
            padding: 12px;
            margin: 10px 0;
            border: 1px solid #ddd;
            border-radius: 8px;
            outline: none;
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
        }

        button:hover {
            background: #1e3c72;
        }

        .success {
            color: green;
            margin-top: 10px;
        }

        .error {
            color: red;
            margin-top: 10px;
        }

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

        .back:hover {
            background: #1e8449;
        }
    </style>
</head>

<body>

<div class="box">

    <h2> Add New Book</h2>

    <form method="post">
        <input type="text" name="title" placeholder="Enter Book Title" required>
        <button type="submit">Add Book</button>
    </form>

<%
    String title = request.getParameter("title");

    if (title != null && !title.trim().isEmpty()) {

        BookDAO dao = new BookDAO();
        boolean added = dao.addBook(new Book(title));

        if (added) {
%>
            <p class="success"> Book Added Successfully!</p>

            <!--  AUTO REFRESH BOOK LIST -->
            <script>
                setTimeout(() => {
                    if (window.opener && window.opener.loadBooks) {
                        window.opener.loadBooks(); // refresh main page
                    }
                }, 500);
            </script>

<%
        } else {
%>
            <p class="error"> Failed to add book</p>
<%
        }
    }
%>

    <!-- BACK BUTTON -->
    <a class="back" href="books">Back to Book List</a>

</div>

</body>
</html>