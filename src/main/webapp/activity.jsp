<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<head>
    <title>Library | Premium Activity Monitor</title>
    <style>
        @import url('https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@400;500;600;700&display=swap');

        :root {
            --bg-gradient: linear-gradient(135deg, #f1f5f9 0%, #e2e8f0 100%);
            --surface: rgba(255, 255, 255, 0.85); /* Translucent glass style */
            --text-main: #0f172a;
            --text-muted: #475569;
            --border: rgba(226, 232, 240, 0.7);
            --primary: #3b82f6;
            --primary-gradient: linear-gradient(135deg, #3b82f6 0%, #1d4ed8 100%);
            --success: #10b981;
            --success-bg: #ecfdf5;
            --error: #ef4444;
            --error-bg: #fef2f2;
            --radius-lg: 20px;
            --radius-md: 12px;
            --shadow-sm: 0 4px 6px -1px rgba(0, 0, 0, 0.05), 0 2px 4px -1px rgba(0, 0, 0, 0.03);
            --shadow-lg: 0 20px 25px -5px rgba(15, 23, 42, 0.08), 0 10px 10px -5px rgba(15, 23, 42, 0.04);
        }

        body {
            font-family: 'Plus Jakarta Sans', sans-serif;
            background: var(--bg-gradient);
            background-attachment: fixed;
            color: var(--text-main);
            margin: 0;
            padding: 0;
            min-height: 100vh;
            -webkit-font-smoothing: antialiased;
        }

        .dashboard-container {
            max-width: 960px;
            margin: 50px auto;
            padding: 0 24px;
        }

        /* Glassmorphism Header Panel */
        .header-panel {
            display: flex;
            justify-content: space-between;
            align-items: center;
            background: var(--surface);
            backdrop-filter: blur(12px); /* Blurred background glass effect */
            -webkit-backdrop-filter: blur(12px);
            padding: 28px 36px;
            border-radius: var(--radius-lg);
            border: 1px solid rgba(255, 255, 255, 0.6);
            box-shadow: var(--shadow-lg);
            margin-bottom: 28px;
        }

        h1 {
            margin: 0;
            font-size: 26px;
            font-weight: 700;
            letter-spacing: -0.75px;
            background: var(--primary-gradient);
            -webkit-background-clip: text;
            -webkit-text-fill-color: transparent;
        }

        .subtitle {
            color: var(--text-muted);
            margin: 8px 0 0 0;
            font-size: 14.5px;
            font-weight: 500;
        }

        /* Modernized Pulsing Status Badges */
        .status-badge {
            padding: 10px 20px;
            border-radius: 99px;
            font-size: 13.5px;
            font-weight: 600;
            display: inline-flex;
            align-items: center;
            gap: 10px;
            box-shadow: var(--shadow-sm);
            transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
        }

        .status-badge::before {
            content: '';
            width: 10px;
            height: 10px;
            border-radius: 50%;
            display: inline-block;
        }

        .status-connected {
            background: var(--success-bg);
            color: var(--success);
            border: 1px solid rgba(16, 185, 129, 0.2);
        }
        .status-connected::before {
            background: var(--success);
            animation: pulse-ring 2s infinite;
        }

        .status-disconnected {
            background: var(--error-bg);
            color: var(--error);
            border: 1px solid rgba(239, 68, 68, 0.2);
        }
        .status-disconnected::before {
            background: var(--error);
        }

        /* Gorgeous Feed Container layout */
        .feed-card {
            background: var(--surface);
            backdrop-filter: blur(12px);
            -webkit-backdrop-filter: blur(12px);
            border: 1px solid rgba(255, 255, 255, 0.5);
            border-radius: var(--radius-lg);
            box-shadow: var(--shadow-lg);
            overflow: hidden;
        }

        .feed-card-header {
            padding: 22px 36px;
            border-bottom: 1px solid var(--border);
            font-weight: 700;
            font-size: 15px;
            text-transform: uppercase;
            letter-spacing: 1px;
            color: var(--text-muted);
            background: rgba(248, 250, 252, 0.5);
        }

        #activity-feed {
            height: 540px;
            overflow-y: auto;
            padding: 12px 0;
            scroll-behavior: smooth;
        }

        /* List Rows Styling */
        .feed-item {
            padding: 18px 36px;
            border-bottom: 1px solid var(--border);
            display: flex;
            align-items: center;
            justify-content: space-between;
            gap: 24px;
            transition: background-color 0.2s ease;
            animation: slideInRow 0.45s cubic-bezier(0.16, 1, 0.3, 1);
        }

        .feed-item:hover {
            background-color: rgba(241, 245, 249, 0.4);
        }

        .feed-item:last-child {
            border-bottom: none;
        }

        .feed-item-left {
            display: flex;
            align-items: center;
            gap: 18px;
            flex-grow: 1;
        }

        /* Dynamic Visual Icon badge */
        .feed-badge-icon {
            width: 42px;
            height: 42px;
            border-radius: var(--radius-md);
            background: var(--primary-gradient);
            color: white;
            display: flex;
            align-items: center;
            justify-content: center;
            font-size: 16px;
            font-weight: 600;
            box-shadow: 0 4px 10px rgba(59, 130, 246, 0.25);
            flex-shrink: 0;
        }

        .feed-message-text {
            color: var(--text-main);
            font-size: 15.5px;
            font-weight: 500;
            line-height: 1.5;
        }

        .feed-timestamp {
            color: var(--text-muted);
            font-size: 13px;
            font-weight: 600;
            white-space: nowrap;
            background: rgba(241, 245, 249, 0.8);
            border: 1px solid var(--border);
            padding: 6px 12px;
            border-radius: 8px;
        }

        /* Modern Scrollbar customization */
        #activity-feed::-webkit-scrollbar {
            width: 8px;
        }
        #activity-feed::-webkit-scrollbar-track {
            background: transparent;
        }
        #activity-feed::-webkit-scrollbar-thumb {
            background: rgba(100, 116, 139, 0.2);
            border-radius: 99px;
        }
        #activity-feed::-webkit-scrollbar-thumb:hover {
            background: rgba(100, 116, 139, 0.4);
        }

        @keyframes slideInRow {
            from { opacity: 0; transform: translateY(12px); }
            to { opacity: 1; transform: translateY(0); }
        }

        @keyframes pulse-ring {
            0% { transform: scale(0.95); box-shadow: 0 0 0 0 rgba(16, 185, 129, 0.4); }
            70% { transform: scale(1); box-shadow: 0 0 0 8px rgba(16, 185, 129, 0); }
            100% { transform: scale(0.95); box-shadow: 0 0 0 0 rgba(16, 185, 129, 0); }
        }
    </style>
</head>
<body>

<div class="dashboard-container">
    <div class="header-panel">
        <div>
            <h1>Activity Hub</h1>
            <p class="subtitle">Real-time update stream covering recent library system events.</p>
        </div>
        <div id="statusIndicator" class="status-badge status-disconnected">
            <span>Disconnected</span>
        </div>
    </div>

    <div class="feed-card">
        <div class="feed-card-header">Live Updates</div>
        <div id="activity-feed"></div>
    </div>
</div>

<script>
    const activityFeed = document.getElementById('activity-feed');
    const statusIndicator = document.getElementById('statusIndicator');

    let pingInterval; // Heartbeat tracker variable

    const wsProtocol = window.location.protocol === "https:" ? "wss://" : "ws://";
    const wsUrl = wsProtocol + window.location.host + "${pageContext.request.contextPath}/websocket/activity";

    console.log("Connecting WebSocket to: " + wsUrl);
    const socket = new WebSocket(wsUrl);

    // Event: Connection Successfully Formed
    socket.onopen = function() {
        statusIndicator.className = "status-badge status-connected";
        statusIndicator.innerHTML = "<span>Monitoring Live</span>";

        // 🌟 KEEP-ALIVE SYSTEM: Sends an invisible ping frame every 30 seconds.
        // This stops tomcat/wildfly/undertow from shutting down an idle pipeline automatically!
        pingInterval = setInterval(() => {
            if (socket.readyState === WebSocket.OPEN) {
                socket.send("ping");
            }
        }, 30000);
    };

    // Event: Message arrives from Server
    socket.onmessage = function(event) {
        // Filter out accidental return reflections of your heartbeat mechanism
        if (event.data === "pong" || event.data === "ping") return;

        appendLog(event.data);
    };

    // Event: Connection Broken
    socket.onclose = function() {
        statusIndicator.className = "status-badge status-disconnected";
        statusIndicator.innerHTML = "<span>Offline</span>";

        // Kill your interval sequence clean to avoid client browser leakage loops
        clearInterval(pingInterval);

        appendLog("The connection to the updates server was dropped. Please refresh the page.");
    };

    function appendLog(message) {
        const now = new Date();
        const timeOptions = { hour: '2-digit', minute: '2-digit', hour12: true };
        const timeString = now.toLocaleTimeString([], timeOptions);

        const item = document.createElement('div');
        item.className = 'feed-item';

        const iconSymbol = message.trim().charAt(0).toUpperCase() || "•";

        item.innerHTML = `
            <div class="feed-item-left">
                <div class="feed-badge-icon">\${iconSymbol}</div>
                <div class="feed-message-text">\${escapeHtml(message)}</div>
            </div>
            <div class="feed-timestamp">\${timeString}</div>
        `;

        activityFeed.appendChild(item);
        activityFeed.scrollTop = activityFeed.scrollHeight;
    }

    function escapeHtml(text) {
        return text.replace(/&/g, "&amp;")
                   .replace(/</g, "&lt;")
                   .replace(/>/g, "&gt;");
    }
</script>
</body>
</html>