<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Librarian Support Desk</title>
    <style>
        * { box-sizing: border-box; margin: 0; padding: 0; font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif; }
        body { display: flex; height: 100vh; background-color: #f4f6f8; color: #333; }

        /* Layout Grid */
        .admin-container { display: flex; width: 100%; height: 100%; overflow: hidden; }

        /* Sidebar Styles */
        .sidebar { width: 320px; background-color: #ffffff; border-right: 1px solid #e1e4e8; display: flex; flex-direction: column; }
        .sidebar-header { padding: 20px; background-color: #1e293b; color: #ffffff; }
        .sidebar-header h2 { font-size: 1.2rem; font-weight: 600; }
        .sidebar-header p { font-size: 0.8rem; opacity: 0.7; margin-top: 4px; }
        .user-list { flex: 1; overflow-y: auto; list-style: none; }
        .user-item { padding: 15px 20px; border-bottom: 1px solid #f0f2f5; cursor: pointer; display: flex; align-items: center; justify-content: space-between; transition: background 0.2s; }
        .user-item:hover { background-color: #f8fafc; }
        .user-item.active { background-color: #e2e8f0; font-weight: 600; }
        .user-info { display: flex; flex-direction: column; }
        .user-id { font-size: 0.9rem; color: #1e293b; }
        .status-dot { width: 8px; height: 8px; background-color: #10b981; border-radius: 50%; }
        .status-dot.offline { background-color: #94a3b8; }

        /* Main Chat Panel */
        .chat-panel { flex: 1; display: flex; flex-direction: column; background-color: #f8fafc; }
        .chat-header { padding: 20px; background-color: #ffffff; border-bottom: 1px solid #e1e4e8; display: flex; align-items: center; }
        .chat-header h3 { font-size: 1.1rem; color: #1e293b; }

        /* Messages Window */
        .messages-container { flex: 1; padding: 20px; overflow-y: auto; display: flex; flex-direction: column; gap: 12px; }
        .message-wrapper { display: flex; flex-direction: column; max-width: 70%; }
        .message-wrapper.incoming { align-self: flex-start; }
        .message-wrapper.outgoing { align-self: flex-end; }

        .message-bubble { padding: 12px 16px; border-radius: 12px; font-size: 0.95rem; line-height: 1.4; }
        .incoming .message-bubble { background-color: #ffffff; color: #1e293b; border: 1px solid #e2e8f0; border-top-left-radius: 4px; }
        .outgoing .message-bubble { background-color: #2563eb; color: #ffffff; border-top-right-radius: 4px; }

        .message-meta { font-size: 0.75rem; color: #64748b; margin-top: 4px; padding: 0 4px; }
        .outgoing .message-meta { align-self: flex-end; }

        /* Empty Welcome State */
        .empty-state { display: flex; flex-direction: column; align-items: center; justify-content: center; height: 100%; color: #64748b; gap: 10px; }

        /* Input Bar */
        .input-area { padding: 20px; background-color: #ffffff; border-top: 1px solid #e1e4e8; display: flex; gap: 12px; align-items: center; }
        .input-area textarea { flex: 1; height: 44px; padding: 12px; border: 1px solid #cbd5e1; border-radius: 6px; resize: none; font-size: 0.95rem; outline: none; transition: border 0.2s; }
        .input-area textarea:focus { border-color: #2563eb; }
        .input-area button { background-color: #2563eb; color: white; border: none; padding: 0 20px; height: 44px; border-radius: 6px; font-weight: 500; cursor: pointer; transition: background 0.2s; }
        .input-area button:hover { background-color: #1d4ed8; }
    </style>
</head>
<body>

<div class="admin-container">
    <aside class="sidebar">
        <div class="sidebar-header">
            <h2>Support Desk Queue</h2>
            <p id="connection-status">Connecting to server...</p>
        </div>
        <ul class="user-list" id="users-queue">
            <li id="placeholder-item" style="padding: 20px; color: #94a3b8; font-size: 0.9rem; text-align: center;">No active students in queue</li>
        </ul>
    </aside>

    <main class="chat-panel">
        <div id="active-chat-view" style="display: none; flex-direction: column; height: 100%;">
            <div class="chat-header">
                <div style="display: flex; align-items: center; gap: 10px;">
                    <div id="active-status-dot" class="status-dot"></div>
                    <h3 id="current-chat-title">Chatting with Student</h3>
                </div>
            </div>

            <div class="messages-container" id="messages-window">
            </div>

            <div class="input-area">
                <textarea id="message-input" placeholder="Type your reply here... (Press Enter to send)"></textarea>
                <button id="send-btn" onclick="sendReply()">Reply</button>
            </div>
        </div>

        <div id="empty-chat-view" class="empty-state">
            <svg width="48" height="48" fill="none" stroke="currentColor" stroke-width="1.5" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" d="M20.25 8.511c.083.287.13.593.13.911 0 2.854-2.608 5.28-6.102 5.28-1.071 0-2.064-.225-2.903-.618L6.5 16.5v-3.411C4.404 11.977 3 9.877 3 7.5c0-3.59 3.805-6.5 8.5-6.5 4.197 0 7.711 2.311 8.4 5.411z"></path></svg>
            <p>Select an active student request from the sidebar queue to start replying.</p>
        </div>
    </main>
</div>

<script>
    let ws; // Holds the active WebSocket connection instance
    let activeClientSessionId = null; // Tracks which student the librarian is currently talking to
    const conversationHistory = {};
    const sessionNames = {};// Stores usernames mapped to their unique session IDs
    const activeSessionsTracked = new Set(); // Prevents duplicate student items from rendering in the UI list
function playNotificationSound() {
    try {
        const AudioContext = window.AudioContext || window.webkitAudioContext;
        if (!AudioContext) return;
        const ctx = new AudioContext(); // Opens access to the browser's audio engine
        const osc1 = ctx.createOscillator(); // Generates a raw sound wave oscillator
        const gain1 = ctx.createGain(); // Acts as a volume controller

        osc1.type = 'sine'; // Sets a clean, pure tone wave shape
        osc1.frequency.setValueAtTime(587.33, ctx.currentTime); // Sets tone pitch to note D5 (587.33 Hz)
        gain1.gain.setValueAtTime(0.1, ctx.currentTime); // Lowers volume to a subtle 10%
        gain1.gain.exponentialRampToValueAtTime(0.01, ctx.currentTime + 0.15); // Creates a smooth fade-out effect

        osc1.connect(gain1); // Hooks the sound generator to the volume controller
        gain1.connect(ctx.destination); // Hooks the volume controller to your speakers
        osc1.start(); // Starts making sound
        osc1.stop(ctx.currentTime + 0.15); // Auto-stops making sound after 0.15 seconds
    } catch (e) { console.warn("Audio context blocked: ", e); } // Silently ignores if browser blocks auto-play
}
    function connectWebSocket() {
        const loc = window.location;
        const protocol = (loc.protocol === "https:") ? "wss://" : "ws://";
        const wsUrl = protocol + loc.host + "<%= request.getContextPath() %>/websocket/chat?role=librarian";
        ws = new WebSocket(wsUrl);
        ws.onopen = () => {
            document.getElementById("connection-status").innerText = "Desk Engine Live";
            document.getElementById("connection-status").style.color = "#10b981";
        };
        ws.onmessage = (event) => {
            if (event.data === "ping" || event.data === "pong") return;
            try {
                const data = JSON.parse(event.data);
                if (data.system === "userList") renderUserQueue(data.users);
                else if (data.system === "disconnect") handleUserDisconnect(data.clientSessionId);
                else handleIncomingChatMessage(data);
            } catch (err) { console.error("Error: ", err); }
        };
    }

    function renderUserQueue(usersList) {
        const queueEl = document.getElementById("users-queue");
        if (!usersList || usersList.length === 0) return;
        const placeholder = document.getElementById("placeholder-item");
        if (placeholder) placeholder.style.display = "none";
        usersList.forEach(user => {
            const id = (typeof user === 'object') ? user.id : user;
            if (!activeSessionsTracked.has(id)) {
                activeSessionsTracked.add(id);
                const li = document.createElement("li");
                li.className = "user-item";
                li.id = "user-row-" + id;
                li.onclick = () => selectUserChat(id, (user.username || "Student_" + id.substring(0, 4)));
                li.innerHTML = '<div class="user-info"><span id="user-display-'+id+'">' + (user.username || "Student") + '</span></div><div class="status-dot" id="dot-status-'+id+'"></div>';
                queueEl.appendChild(li);
            }
        });
    }

    function selectUserChat(sessionId, displayName) {
        activeClientSessionId = sessionId;
        document.querySelectorAll('.user-item').forEach(el => el.classList.remove('active'));
        const row = document.getElementById("user-row-" + sessionId);
        if (row) row.className = "user-item active";
        document.getElementById("empty-chat-view").style.display = "none";
        document.getElementById("active-chat-view").style.display = "flex";
        document.getElementById("current-chat-title").innerText = "Chatting with: " + displayName;

        // Force the input to be active
        const input = document.getElementById("message-input");
        input.disabled = false;
        input.focus();
        renderActiveConversation();
    }

    function handleIncomingChatMessage(msg) {
        const id = msg.clientSessionId || activeClientSessionId;
        if (!id) return;
        if (!conversationHistory[id]) conversationHistory[id] = [];

        // Prevent exact duplicate entries from recording in the array
        const isDuplicate = conversationHistory[id].some(item => item.text === msg.text && item.sender === msg.sender);
        if (!isDuplicate) {
            conversationHistory[id].push(msg);
        }

        if (msg.sender !== "Librarian") playNotificationSound();
        if (id === activeClientSessionId) renderActiveConversation();
        else {
            const row = document.getElementById("user-row-" + id);
            if (row) row.style.borderLeft = "4px solid #2563eb";
        }
    }

    function renderActiveConversation() {
        const win = document.getElementById("messages-window");
        win.innerHTML = "";
        (conversationHistory[activeClientSessionId] || []).forEach(msg => {
            const div = document.createElement("div");
            div.className = "message-wrapper " + (msg.sender === "Librarian" ? "outgoing" : "incoming");
            div.innerHTML = '<div class="message-bubble">' + escapeHtml(msg.text) + '</div>';
            win.appendChild(div);
        });
        win.scrollTop = win.scrollHeight;
    }

    function sendReply() {
        const input = document.getElementById("message-input");
        if (!input.value.trim() || !activeClientSessionId) return;

        // Send to WebSocket
        ws.send(JSON.stringify({ targetSessionId: activeClientSessionId, text: input.value }));



        input.value = "";
        input.focus();
    }

    function handleUserDisconnect(id) {
        const dot = document.getElementById("dot-status-" + id);
        if (dot) dot.className = "status-dot offline";
    }

    function escapeHtml(text) {
        const div = document.createElement("div");
        div.innerText = text;
        return div.innerHTML;
    }

    document.addEventListener("DOMContentLoaded", () => {
        document.getElementById("message-input").addEventListener("keydown", (e) => {
            if (e.key === "Enter" && !e.shiftKey) { e.preventDefault(); sendReply(); }
        });
        connectWebSocket();
    });
</script>
</body>
</html>