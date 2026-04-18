<%@ page contentType="text/html;charset=UTF-8" %>
<!DOCTYPE html>
<html>
<head>
    <title>Register</title>

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
            width: 350px;
            box-shadow: 0 4px 12px rgba(0,0,0,0.1);
        }

        h2 {
            text-align: center;
            color: #2c3e50;
        }

        input {
            width: 100%;
            padding: 10px;
            margin: 10px 0;
            border-radius: 6px;
            border: 1px solid #ccc;
        }

        button {
            width: 100%;
            padding: 10px;
            background: #2a5298;
            color: white;
            border: none;
            border-radius: 6px;
        }

        button:hover {
            background: #1e3c72;
        }
       .back-home-btn {
           display: inline-block;
           margin-left: 10px;
           margin-top: 10px; /* 👈 adds space downward */
           padding: 10px 16px;
           background: #2c3e50;
           color: white;
           text-decoration: none;
           border-radius: 6px;
           font-weight: 600;
           transition: all 0.3s ease;
       }

       .back-home-btn:hover {
           background: #1a252f;
           transform: translateY(-2px);
       }

       .back-home-btn:active {
           transform: scale(0.98);
       }
    </style>
</head>

<body>

<div class="box">
    <h2>📝 Register</h2>

    <form method="post" action="register-submit.jsp">

        <input type="text" name="username" placeholder="Username" required />

        <input type="email" name="email" placeholder="Email" required />

        <input type="password" name="password" placeholder="Password" required />

        <button type="submit">Create Account</button>

        <a href="index.jsp" class="back-home-btn"> Back Home</a>

    </form>
</div>

</body>
</html>