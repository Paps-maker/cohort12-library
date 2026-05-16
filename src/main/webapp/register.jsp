<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<head>
    <title>Library | Register</title>
    <style>
        @import url('https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@400;600;800&display=swap');
        body {
            font-family: 'Plus Jakarta Sans', sans-serif;
            background: #f8fafc;
            display: flex;
            justify-content: center;
            align-items: center;
            height: 100vh;
            margin: 0;
            color: #1e293b;
        }
        .box {
            background: white;
            padding: 40px;
            border-radius: 24px;
            width: 380px;
            box-shadow: 0 20px 25px -5px rgba(0,0,0,0.1);
            border: 1px solid #f1f5f9;
        }
        h2 { margin-top: 0; font-weight: 800; letter-spacing: -1px; color: #0f172a; text-align: center; }
        p { text-align: center; color: #64748b; font-size: 14px; margin-bottom: 25px; }
        input {
            width: 100%;
            padding: 12px;
            margin: 10px 0;
            border: 1px solid #e2e8f0;
            border-radius: 12px;
            box-sizing: border-box;
            font-family: inherit;
        }
        input:focus { outline: none; border-color: #3b82f6; box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1); }
        button {
            width: 100%;
            padding: 12px;
            margin-top: 20px;
            background: #2563eb;
            color: white;
            border: none;
            border-radius: 12px;
            font-weight: 600;
            cursor: pointer;
            transition: 0.2s;
        }
        button:hover { background: #1d4ed8; transform: translateY(-1px); }
        .login-link { text-align: center; margin-top: 20px; font-size: 14px; }
        .login-link a { color: #2563eb; text-decoration: none; font-weight: 600; }
    </style>
</head>
<body>

<div class="box">
    <h2>Create Account</h2>
    <p>Join the School Library system today.</p>

    <%--  ACTION changed to point to your new RegisterServlet --%>
    <form action="registerProcess" method="post">
        <label style="font-size: 13px; font-weight: 600;">Username</label>
        <input type="text" name="username" placeholder="official name's" required/>

        <label style="font-size: 13px; font-weight: 600;">Email Address</label>
        <input type="email" name="email" placeholder="your school email" required/>

        <label style="font-size: 13px; font-weight: 600;">Password</label>
        <input type="password" name="password" placeholder="••••••••" required/>

        <button type="submit">Create Account</button>
    </form>

    <div class="login-link">
        Already have an account? <a href="login">Sign In</a>
    </div>
</div>

</body>
</html>