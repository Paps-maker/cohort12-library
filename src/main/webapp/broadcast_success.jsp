<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Broadcast Confirmation  Library System</title>
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
    <style>
        @import url('https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@500;700;800&display=swap');

        :root {
            --slate-950: #020617;
            --slate-900: #0f172a;
            --slate-800: #1e293b;
            --slate-400: #94a3b8;
            --indigo-600: #4f46e5;
            --indigo-700: #4338ca;
            --emerald-500: #10b981;
            --bg-main: #f8fafc;
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

        .confirmation-card {
            background: white;
            padding: 48px;
            border-radius: 24px;
            border: 1px solid #e2e8f0;
            box-shadow: 0 10px 25px -5px rgba(0, 0, 0, 0.05), 0 8px 10px -6px rgba(0, 0, 0, 0.05);
            text-align: center;
            max-width: 480px;
            width: 100%;
            box-sizing: border-box;
        }

        .success-icon-wrapper {
            width: 80px;
            height: 80px;
            background-color: #f0fdf4;
            border: 2px solid #bbf7d0;
            border-radius: 50%;
            display: flex;
            align-items: center;
            justify-content: center;
            margin: 0 auto 28px auto;
            color: var(--emerald-500);
            font-size: 32px;
            animation: scaleIn 0.4s cubic-bezier(0.34, 1.56, 0.64, 1);
        }

        .title {
            font-size: 24px;
            font-weight: 800;
            color: var(--slate-900);
            margin: 0 0 12px 0;
            letter-spacing: -0.5px;
        }

        .feedback-message {
            font-size: 15px;
            color: var(--slate-400);
            line-height: 1.6;
            margin: 0 0 32px 0;
            font-weight: 500;
        }

        .action-btn {
            display: inline-flex;
            align-items: center;
            justify-content: center;
            gap: 8px;
            background-color: var(--indigo-600);
            color: white;
            text-decoration: none;
            padding: 14px 28px;
            border-radius: 12px;
            font-weight: 700;
            font-size: 14px;
            transition: all 0.2s ease;
            box-shadow: 0 4px 12px rgba(79, 70, 229, 0.2);
            width: 100%;
            box-sizing: border-box;
        }

        .action-btn:hover {
            background-color: var(--indigo-700);
            transform: translateY(-1px);
            box-shadow: 0 6px 16px rgba(79, 70, 229, 0.25);
        }

        @keyframes scaleIn {
            0% { transform: scale(0); opacity: 0; }
            100% { transform: scale(1); opacity: 1; }
        }
    </style>
</head>
<body>

    <div class="confirmation-card">
        <div class="success-icon-wrapper">
            <i class="fa-solid fa-circle-check"></i>
        </div>

        <h1 class="title">Transmission Complete</h1>

        <p class="feedback-message">
            ${not empty broadcastSuccess ? broadcastSuccess : "The broadcast notification has been securely processed and queued for background email dispatch."}
        </p>

        <a href="${pageContext.request.contextPath}/books" class="action-btn">
            <i class="fa-solid fa-arrow-left"></i> Return to Dashboard Hub
        </a>
    </div>

</body>
</html>