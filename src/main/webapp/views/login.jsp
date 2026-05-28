<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    String contextPath = request.getContextPath();
    String authError = (String) request.getAttribute("error");
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Library System | Authorization Workspace</title>

    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">

    <style>
        @import url('https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@400;500;600;700;800&family=Inter:wght@400;500;600;700&display=swap');

        :root {
            --slate-950: #020617;
            --slate-900: #0f172a;
            --slate-800: #1e293b;
            --slate-700: #334155;
            --slate-600: #475569;
            --slate-400: #94a3b8;
            --indigo-600: #4f46e5;
            --indigo-700: #4338ca;
            --rose-600: #e11d48;
            --rose-50: #fff1f2;
            --bg-main: #f8fafc;
            --card-shadow: 0 10px 25px -5px rgba(2, 6, 23, 0.05), 0 8px 10px -6px rgba(2, 6, 23, 0.05);
        }

        body {
            font-family: 'Plus Jakarta Sans', sans-serif;
            margin: 0;
            padding: 0;
            background-color: var(--bg-main);
            color: var(--slate-800);
            display: flex;
            align-items: center;
            justify-content: center;
            min-height: 100vh;
            -webkit-font-smoothing: antialiased;
        }

        /* Glassmorphic Entrance Shell Container */
        .auth-container {
            width: 100%;
            max-width: 440px;
            padding: 20px;
            box-sizing: border-box;
        }

        .auth-card {
            background-color: #ffffff;
            border-radius: 24px;
            border: 1px solid #e2e8f0;
            padding: 40px 36px;
            box-shadow: var(--card-shadow);
        }

        /* Identity Branding Typography */
        .auth-brand {
            display: flex;
            align-items: center;
            justify-content: center;
            gap: 12px;
            font-size: 22px;
            font-weight: 800;
            letter-spacing: -0.5px;
            color: var(--slate-950);
            margin-bottom: 12px;
        }

        .auth-brand i {
            color: var(--indigo-600);
            font-size: 24px;
        }

        .auth-heading {
            font-size: 24px;
            font-weight: 800;
            color: var(--slate-900);
            text-align: center;
            margin: 0 0 6px 0;
            letter-spacing: -0.5px;
        }

        .auth-subheading {
            font-size: 14px;
            color: var(--slate-400);
            text-align: center;
            margin-bottom: 32px;
            font-weight: 500;
        }

        /* Enterprise Input Matrix Form Control */
        .form-group {
            margin-bottom: 20px;
            display: flex;
            flex-direction: column;
            gap: 6px;
        }

        .form-group label {
            font-size: 13px;
            font-weight: 700;
            color: var(--slate-700);
            letter-spacing: -0.1px;
        }

        .input-wrapper {
            position: relative;
            display: flex;
            align-items: center;
        }

        .input-wrapper i {
            position: absolute;
            left: 16px;
            color: var(--slate-400);
            font-size: 16px;
            pointer-events: none;
            transition: color 0.2s ease;
        }

        .form-control {
            font-family: 'Inter', sans-serif;
            width: 100%;
            padding: 14px 16px 14px 44px;
            box-sizing: border-box;
            border-radius: 12px;
            border: 1px solid #e2e8f0;
            font-size: 14px;
            color: var(--slate-900);
            background-color: #ffffff;
            outline: none;
            transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
        }

        .form-control::placeholder {
            color: var(--slate-400);
        }

        .form-control:focus {
            border-color: var(--indigo-600);
            box-shadow: 0 0 0 4px rgba(79, 70, 229, 0.1);
        }

        .form-control:focus + i {
            color: var(--indigo-600);
        }

        /* Security Error Display Layer */
        .error-banner {
            background-color: var(--rose-50);
            border: 1px solid #fecaca;
            border-left: 4px solid var(--rose-600);
            border-radius: 12px;
            padding: 14px 16px;
            margin-bottom: 24px;
            display: flex;
            align-items: flex-start;
            gap: 12px;
            color: #991b1b;
            font-size: 13px;
            font-weight: 600;
            line-height: 1.4;
        }

        .error-banner i {
            font-size: 16px;
            margin-top: 1px;
            color: var(--rose-600);
        }

        /* Action Controls Layout Matrix */
        .btn-submit {
            width: 100%;
            background-color: var(--indigo-600);
            color: white;
            padding: 14px 24px;
            border-radius: 12px;
            font-weight: 700;
            font-size: 14px;
            font-family: 'Plus Jakarta Sans', sans-serif;
            border: none;
            cursor: pointer;
            display: inline-flex;
            align-items: center;
            justify-content: center;
            gap: 8px;
            box-shadow: 0 4px 12px rgba(79, 70, 229, 0.2);
            transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
            margin-top: 10px;
        }

        .btn-submit:hover {
            background-color: var(--indigo-700);
            transform: translateY(-1px);
            box-shadow: 0 6px 16px rgba(79, 70, 229, 0.25);
        }

        .btn-submit:active {
            transform: translateY(0);
        }

        .auth-footer {
            margin-top: 28px;
            text-align: center;
            font-size: 13px;
            color: var(--slate-400);
            font-weight: 500;
        }

        .auth-footer a {
            color: var(--indigo-600);
            text-decoration: none;
            font-weight: 700;
            transition: color 0.15s ease;
        }

        .auth-footer a:hover {
            color: var(--indigo-700);
            text-decoration: underline;
        }
    </style>
</head>
<body>

    <div class="auth-container">
        <div class="auth-card">

            <div class="auth-brand">
                <i class="fa-solid fa-graduation-cap"></i>
                <span>School Library</span>
            </div>

            <h2 class="auth-heading">Login</h2>
            <p class="auth-subheading">Enter your administrative workspace credentials</p>

            <% if (authError != null && !authError.trim().isEmpty()) { %>
                <div class="error-banner">
                    <i class="fa-solid fa-circle-exclamation"></i>
                    <div><%= authError %></div>
                </div>
            <% } %>

            <form action="<%= contextPath %>/authenticate" method="POST">
                <div class="form-group">
                    <label for="username">Username Account</label>
                    <div class="input-wrapper">
                        <input type="text" id="username" name="username" class="form-control" placeholder="Enter your username" required autocomplete="username">
                        <i class="fa-regular fa-user"></i>
                    </div>
                </div>

                <div class="form-group" style="margin-bottom: 24px;">
                    <label for="password">Security Password</label>
                    <div class="input-wrapper">
                        <input type="password" id="password" name="password" class="form-control" placeholder="••••••••••••" required autocomplete="current-password">
                        <i class="fa-solid fa-lock"></i>
                    </div>
                </div>

                <button type="submit" class="btn-submit">
                    <span>Login</span>
                    <i class="fa-solid fa-arrow-right"></i>
                </button>
            </form>

            <div class="auth-footer">
                Don't have an account? <a href="<%= contextPath %>/register.jsp">Register</a>
            </div>
        </div>
    </div>

</body>
</html>