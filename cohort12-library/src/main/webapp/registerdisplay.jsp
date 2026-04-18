<%@ page contentType="text/html;charset=UTF-8" %>
<!DOCTYPE html>
<html>
<head>
    <title>Registration Successful</title>

    <style>
        body {
            font-family: 'Segoe UI';
            background: #f4f6f8;
            display: flex;
            justify-content: center;
            align-items: center;
            height: 100vh;
        }

        .box {
            background: white;
            padding: 30px;
            border-radius: 12px;
            text-align: center;
            width: 400px;
            box-shadow: 0 4px 12px rgba(0,0,0,0.1);
        }

        h2 {
            color: #27ae60;
        }

        p {
            color: #555;
        }

        a {
            display: inline-block;
            margin-top: 15px;
            padding: 10px 15px;
            background: #2a5298;
            color: white;
            text-decoration: none;
            border-radius: 6px;
        }

        a:hover {
            background: #1e3c72;
        }
    </style>
</head>

<body>

<div class="box">

    <h2>✅ Registration Successful</h2>

    <p>Welcome, <strong><%= request.getParameter("username") %></strong></p>

    <p>Your account has been created successfully. Login with your Username and password</p>

    <p><strong>Username:</strong> <%= request.getParameter("username") %></p>
    <p><strong>password:</strong> <%= request.getParameter("password") %></p>

    <a href="index.jsp"> Go to Login</a>

</div>

</body>
</html>