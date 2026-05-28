<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Library Dashboard</title>
    <style>
        @import url('https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght=400;500;600;700;800&display=swap');
        :root { --primary: #1e3c72; --secondary: #2a5298; --bg: #ffffff; --card-bg: #f8fafc; --text: #334155; --brand: #4f46e5; --brand-hover: #4338ca; }
        body { font-family: 'Plus Jakarta Sans', sans-serif; margin:0; background:var(--bg); color:var(--text); }
        .header { background: linear-gradient(135deg, #1e3c72, #2a5298); color:white; padding:25px; text-align:center; font-size:24px; font-weight:800; letter-spacing: 1px; }
        .time-container { display: flex; justify-content: center; margin-top: -15px; margin-bottom: 20px; }
        .time-card { background: #0f172a; color: white; padding: 12px 25px; border-radius: 0 0 15px 15px; box-shadow: 0 4px 6px rgba(0,0,0,0.1); text-align: center; }
        .time-value { font-size: 15px; font-weight: 700; }
        .container { max-width: 1200px; margin: auto; padding: 20px; }
        .welcome-row { display: flex; justify-content: space-between; align-items: center; margin-bottom: 25px; }
        .welcome { font-size: 20px; font-weight: 700; color: #0f172a; }
        .fine-alert { background: #fee2e2; color: #b91c1c; padding: 10px 18px; border-radius: 12px; font-size: 13px; font-weight: 800; text-decoration: none; border: 1px solid #fecaca; }
        .stats { display: grid; grid-template-columns: repeat(auto-fit, minmax(220px, 1fr)); gap: 15px; margin-bottom: 30px; }
        .stat-card { background: var(--card-bg); padding: 20px; border-radius: 16px; border: 1px solid #e2e8f0; text-align: center; }
        .stat-title { font-size: 11px; color: #64748b; text-transform: uppercase; font-weight: 800; letter-spacing: 0.5px; }
        .stat-value { font-size: 24px; font-weight: 800; color: var(--primary); margin-top: 5px; }
        .search-wrapper { margin-bottom: 30px; max-width: 100%; }
        .search-wrapper input { width: 100%; box-sizing: border-box; padding: 14px 20px; font-size: 15px; font-family: inherit; border-radius: 12px; border: 1.5px solid #e2e8f0; background: #f8fafc; transition: all 0.2s ease; color: #0f172a; font-weight: 500; }
        .search-wrapper input:focus { background: #ffffff; border-color: var(--brand); outline: none; box-shadow: 0 0 0 4px rgba(79, 70, 229, 0.1); }
        .library-title { font-size: 20px; font-weight: 800; margin-bottom: 20px; border-left: 5px solid var(--secondary); padding-left: 12px; }
        .shelf { display: grid; grid-template-columns: repeat(auto-fill, minmax(240px, 1fr)); gap: 25px; transition: opacity 0.2s ease; }
        .book { background: white; border-radius: 15px; overflow: hidden; border: 1px solid #e2e8f0; display: flex; flex-direction: column; transition: 0.3s; }
        .book:hover { transform: translateY(-5px); box-shadow: 0 12px 20px rgba(0,0,0,0.08); }
        .book-cover { width: 100%; height: 280px; object-fit: cover; background: #f1f5f9; }
        .book-details { padding: 18px; flex-grow: 1; display: flex; flex-direction: column; }
        .book-title { font-size: 16px; font-weight: 800; color: #0f172a; margin-bottom: 8px; line-height: 1.3; }
        .book-description { font-size: 13px; color: #64748b; margin-bottom: 15px; display: -webkit-box; -webkit-line-clamp: 3; -webkit-box-orient: vertical; overflow: hidden; }
        .badge { font-size: 11px; font-weight: 800; padding: 5px 10px; border-radius: 6px; text-transform: uppercase; margin-bottom: 8px; align-self: flex-start; }
        .bg-green { background: #dcfce7; color: #166534; }
        .bg-red { background: #fee2e2; color: #991b1b; }
        .wait-text { font-size: 12px; color: #ef4444; font-weight: 700; margin-bottom: 10px; }
        .book-actions { display: flex; gap: 8px; margin-top: auto; justify-content: flex-start; }
        .btn-borrow-trigger { display: inline-flex; align-items: center; justify-content: center; background: var(--brand); color: white; border: none; padding: 8px 16px; font-size: 13px; font-weight: 600; border-radius: 8px; cursor: pointer; font-family: inherit; letter-spacing: -0.1px; box-shadow: 0 2px 4px rgba(79, 70, 229, 0.15); transition: all 0.2s cubic-bezier(0.16, 1, 0.3, 1); }
        .btn-borrow-trigger:hover { background: var(--brand-hover); transform: translateY(-1px); box-shadow: 0 4px 8px rgba(79, 70, 229, 0.25); }
        .btn-disabled { display: inline-flex; align-items: center; justify-content: center; background: #f1f5f9; color: #94a3b8; border: 1px solid #e2e8f0; padding: 8px 16px; font-size: 13px; font-weight: 600; border-radius: 8px; cursor: not-allowed; font-family: inherit; }
        .modal-overlay { display: none; position: fixed; top: 0; left: 0; width: 100%; height: 100%; background: rgba(15, 23, 42, 0.4); z-index: 9999; backdrop-filter: blur(4px); align-items: center; justify-content: center; }
        .modal-box { background: white; width: 100%; max-width: 440px; border-radius: 16px; box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.1); overflow: hidden; border: 1px solid #e2e8f0; animation: modalFade 0.25s cubic-bezier(0.16, 1, 0.3, 1); }
        .modal-header { background: #ffffff; color: #0f172a; padding: 24px 24px 12px 24px; font-size: 18px; font-weight: 800; text-align: left; border-bottom: 1px solid #f1f5f9; letter-spacing: -0.3px; }
        .modal-form { padding: 24px; display: flex; flex-direction: column; gap: 16px; }
        .modal-input-group { display: flex; flex-direction: column; gap: 6px; }
        .modal-input-group label { font-size: 11px; font-weight: 700; color: #64748b; text-transform: uppercase; }
        .modal-input-group input { padding: 12px 14px; border: 1.5px solid #e2e8f0; border-radius: 10px; font-family: inherit; font-size: 14px; }
        .modal-input-group input[readonly] { background: #f8fafc; color: #475569; cursor: not-allowed; }
        .modal-actions { display: flex; gap: 10px; margin-top: 8px; }
        .modal-btn-confirm { flex: 1.5; background: #2563eb; color: white; border: none; padding: 12px; border-radius: 10px; font-weight: 700; cursor: pointer; }
        .modal-btn-cancel { flex: 1; background: #f1f5f9; color: #475569; border: 1px solid #e2e8f0; padding: 12px; border-radius: 10px; font-weight: 700; cursor: pointer; text-align: center; text-decoration: none; }
        .links { margin-top: 40px; display: flex; flex-wrap: wrap; gap: 12px; }
        .links a { padding: 12px 20px; border-radius: 10px; text-decoration: none; font-size: 14px; font-weight: 700; background: #f1f5f9; color: #1e293b; border: 1px solid #e2e8f0; transition: 0.2s; }
        .links a:hover { background: var(--secondary); color: white; }
        @keyframes modalFade { from { opacity: 0; transform: scale(0.95) translateY(10px); } to { opacity: 1; transform: scale(1) translateY(0); } }
        #chatWidgetContainer { position: fixed; bottom: 20px; right: 20px; width: 340px; background: #ffffff; border-radius: 16px; border: 1px solid #e2e8f0; box-shadow: 0 10px 25px rgba(0,0,0,0.15); z-index: 10000; overflow: hidden; display: flex; flex-direction: column; font-family: inherit; transition: all 0.3s cubic-bezier(0.16, 1, 0.3, 1); }
        .chat-widget-minimized { height: 48px; }
        .chat-widget-expanded { height: 420px; }
        .chat-widget-header { background: linear-gradient(135deg, #1e3c72, #2a5298); color: white; padding: 14px 18px; display: flex; justify-content: space-between; align-items: center; cursor: pointer; font-weight: 700; font-size: 14px; user-select: none; }
        .chat-header-icon { font-size: 11px; transition: transform 0.3s; }
        .chat-widget-body { padding: 14px; display: flex; flex-direction: column; flex-grow: 1; gap: 10px; height: calc(100% - 48px); box-sizing: border-box; background: #f8fafc; }
        .chat-message-log { flex-grow: 1; overflow-y: auto; background: white; border: 1px solid #e2e8f0; border-radius: 10px; padding: 10px; display: flex; flex-direction: column; gap: 8px; font-size: 13px; }
        .chat-msg-row { display: flex; flex-direction: column; max-width: 85%; padding: 8px 12px; border-radius: 12px; line-height: 1.4; word-wrap: break-word; }
        .chat-msg-student { background: #e0e7ff; color: #1e1b4b; align-self: flex-end; border-bottom-right-radius: 2px; }
        .chat-msg-librarian { background: #f1f5f9; color: #0f172a; align-self: flex-start; border-bottom-left-radius: 2px; border: 1px solid #e2e8f0; }
        .chat-msg-meta { font-size: 10px; font-weight: 700; text-transform: uppercase; margin-bottom: 2px; opacity: 0.6; }
        .chat-input-row { display: flex; gap: 6px; }
        #chatWidgetInput { flex-grow: 1; padding: 10px 12px; border: 1.5px solid #e2e8f0; border-radius: 8px; font-family: inherit; font-size: 13px; }
        #chatWidgetInput:focus { outline: none; border-color: var(--brand); }
        #chatWidgetSendBtn { background: var(--brand); color: white; border: none; padding: 0 16px; border-radius: 8px; font-weight: 700; font-size: 13px; cursor: pointer; font-family: inherit; }
        #chatWidgetSendBtn:hover { background: var(--brand-hover); }
    </style>
</head>
<body>

<div class="header"> SCHOOL LIBRARY </div>

<div class="time-container">
    <div class="time-card">
        <div class="time-value">${formattedDate}</div>
        <div id="clock" style="font-size: 13px; opacity: 0.9; margin-top: 4px; font-family: monospace;"></div>
    </div>
</div>

<div class="container">
    <div class="welcome-row">
        <div class="welcome">Welcome back, ${username}</div>
        <c:if test="${totalOwed > 0}">
            <a href="${pageContext.request.contextPath}/fines" class="fine-alert">${fineLabel} <fmt:formatNumber value="${totalOwed}" type="number" minFractionDigits="2" maxFractionDigits="2"/></a>
        </c:if>
    </div>

    <div class="stats">
        <div class="stat-card"><div class="stat-title">Active Users</div><div class="stat-value"><span id="activeUsers">...</span></div></div>
        <div class="stat-card"><div class="stat-title">Total Titles</div><div class="stat-value">${totalUniqueTitles}</div></div>
        <div class="stat-card"><div class="stat-title">${borrowedLabel}</div><div class="stat-value">${displayBorrowedCount}</div></div>
        <div class="stat-card"><div class="stat-title">Copies Available</div><div class="stat-value">${availableCount}</div></div>
    </div>

    <div class="search-wrapper">
        <input type="text" id="librarySearch" onkeyup="filterLibraryBooks()" placeholder="Search by title, author, description...">
    </div>

    <div class="library">
        <div class="library-title">Book Collection</div>
        <div class="shelf" id="bookShelf">
            <c:forEach var="book" items="${allBooks}">
                <div class="book">
                    <img src="${not empty book.imageUrl ? book.imageUrl : 'https://via.placeholder.com/300x450?text=No+Cover'}" class="book-cover">
                    <div class="book-details">
                        <c:choose>
                            <c:when test="${book.availableCopies == 0}">
                                <span class="badge bg-red">All copies borrowed</span>
                                <c:set var="daysLeft" value="${bookBean.getDaysUntilAvailable(book.title)}" />
                                <span class="wait-text">${daysLeft > 0 ? 'Available in '.concat(daysLeft).concat(' days') : 'Check back later'}</span>
                            </c:when>
                            <c:otherwise>
                                <span class="badge bg-green">${book.availableCopies} ${book.availableCopies == 1 ? 'Copy Left' : 'Copies Left'}</span>
                            </c:otherwise>
                        </c:choose>

                        <div class="book-title">${book.title}</div>
                        <div class="book-description">${not empty book.description ? book.description : 'No description.'}</div>

                        <div class="book-actions">
                            <c:choose>
                                <c:when test="${book.availableCopies > 0 && totalOwed == 0}">
                                    <button class="btn-borrow-trigger" onclick="openBorrowModal(${book.id}, '${book.title.replace("'", "\\'")}')">Borrow Book</button>
                                </c:when>
                                <c:when test="${totalOwed > 0}">
                                    <button class="btn-disabled" disabled>Account Blocked</button>
                                </c:when>
                                <c:otherwise>
                                    <button class="btn-disabled" disabled>Unavailable</button>
                                </c:otherwise>
                            </c:choose>
                        </div>
                    </div>
                </div>
            </c:forEach>
        </div>
    </div>

    <div class="links">
        <a href="${pageContext.request.contextPath}/fines" style="background:#f59e0b; color:white; border:none;">Fines Dashboard</a>
        <a href="${pageContext.request.contextPath}/library/loans">Borrowed Books</a>
        <a href="${pageContext.request.contextPath}/logout" style="background:#ef4444; color:white; border:none;">Logout</a>
    </div>
</div>

<div id="borrowModal" class="modal-overlay">
  <div class="modal-box">
    <div class="modal-header">Confirm Checkout Request</div>
    <form action="${pageContext.request.contextPath}/library/borrow" method="POST" class="modal-form">
      <input type="hidden" id="modalBookId" name="bookId" value="">
      <div class="modal-input-group">
        <label>Selected Title</label>
        <input type="text" id="modalBookTitle" readonly>
      </div>
      <div class="modal-input-group">
        <label>Loan Duration (1 - 10 Days)</label>
        <input type="number" name="days" min="1" max="10" value="7" required>
      </div>
      <div class="modal-actions">
        <button type="submit" class="modal-btn-confirm">Borrow Book</button>
        <button type="button" class="modal-btn-cancel" onclick="closeBorrowModal()">Cancel</button>
      </div>
    </form>
  </div>
</div>

<div id="chatWidgetContainer" class="chat-widget-minimized">
  <div class="chat-widget-header" onclick="toggleChatWidget(event)">
    <div style="display:flex; align-items:center; gap:10px;">
      <div style="width:8px; height:8px; background:#10b981; border-radius:50%;"></div>
      <span>Live Support</span>
    </div>
    <span id="chatToggleIcon" class="chat-header-icon">▲</span>
  </div>
  <div class="chat-widget-body">
    <div id="chatMessageArea" class="chat-message-log"></div>
    <div class="chat-input-row">
      <input type="text" id="chatWidgetInput" placeholder="Ask a librarian..." onkeypress="handleChatEnter(event)">
      <button id="chatWidgetSendBtn" onclick="sendChatMessage()">Send</button>
    </div>
  </div>
</div>

<script>
    function updateClock() {
        const now = new Date();
        document.getElementById('clock').innerHTML = now.toLocaleTimeString([], {hour: '2-digit', minute:'2-digit', second:'2-digit'});
    }
    setInterval(updateClock, 1000); updateClock();

    function loadUsers() {
      fetch('active-users').then(r => r.text()).then(d => {
        if (d && d.length < 10) document.getElementById('activeUsers').innerText = d;
      }).catch(() => {});
    }
    setInterval(loadUsers, 5000); loadUsers();

    function filterLibraryBooks() {
        let input = document.getElementById('librarySearch').value.toLowerCase();
        let shelf = document.getElementById('bookShelf');
        let books = shelf.getElementsByClassName('book');
        for (let i = 0; i < books.length; i++) {
            let title = books[i].getElementsByClassName('book-title')[0].innerText.toLowerCase();
            let desc = books[i].getElementsByClassName('book-description')[0].innerText.toLowerCase();
            if (title.includes(input) || desc.includes(input)) {
                books[i].style.display = '';
            } else {
                books[i].style.display = 'none';
            }
        }
    }

    function openBorrowModal(bookId, bookTitle) {
        document.getElementById('modalBookId').value = bookId;
        document.getElementById('modalBookTitle').value = bookTitle;
        document.getElementById('borrowModal').style.display = 'flex';
    }
    document.getElementById('borrowModal').addEventListener('click', function(e) {
        if(e.target === this) closeBorrowModal();
    });
    function closeBorrowModal() {
        document.getElementById('borrowModal').style.display = 'none';
    }

    let wsChatClient = null;
    const currentUsername = "${username}";

    function initChatWebSocket() {
      const loc = window.location;
      const wsProtocol = loc.protocol === 'https:' ? 'wss:' : 'ws:';
      const wsUrl = wsProtocol + '//' + loc.host + loc.pathname.substring(0, loc.pathname.indexOf('/', 1)) + '/websocket/chat?role=client&username=' + encodeURIComponent(currentUsername);

      wsChatClient = new WebSocket(wsUrl);
      wsChatClient.onopen = function() {
        setInterval(() => { if(wsChatClient.readyState === WebSocket.OPEN) wsChatClient.send('ping'); }, 25000);
      };
      wsChatClient.onmessage = function(event) {
        if (event.data === 'pong') return;
        try {
          const data = JSON.parse(event.data);
          if (data.text) { renderReceivedMessage(data.sender, data.text, data.username); }
        } catch(e) {}
      };
      wsChatClient.onclose = function() { setTimeout(initChatWebSocket, 5000); };
    }

    function toggleChatWidget() {
      const widget = document.getElementById('chatWidgetContainer');
      const icon = document.getElementById('chatToggleIcon');
      if (widget.classList.contains('chat-widget-minimized')) {
        widget.classList.remove('chat-widget-minimized');
        widget.classList.add('chat-widget-expanded');
        icon.innerText = '▼';
        if (!wsChatClient) { initChatWebSocket(); }
      } else {
        widget.classList.remove('chat-widget-expanded');
        widget.classList.add('chat-widget-minimized');
        icon.innerText = '▲';
      }
    }

    function sendChatMessage() {
      const input = document.getElementById('chatWidgetInput');
      const msgText = input.value.trim();
      if (!msgText || !wsChatClient || wsChatClient.readyState !== WebSocket.OPEN) return;
      wsChatClient.send(JSON.stringify({ text: msgText }));
      input.value = '';
    }

    function handleChatEnter(event) { if (event.key === 'Enter') { sendChatMessage(); } }

    function renderReceivedMessage(sender, text, username) {
      const log = document.getElementById('chatMessageArea');
      const isLibrarian = sender === 'Librarian';
      const row = document.createElement('div');
      row.className = 'chat-msg-row ' + (isLibrarian ? 'chat-msg-librarian' : 'chat-msg-student');
      const label = isLibrarian ? 'Librarian' : (username || 'Me');
      row.innerHTML = '<span class="chat-msg-meta">' + label + '</span><span>' + escapeHTML(text) + '</span>';
      log.appendChild(row);
      log.scrollTop = log.scrollHeight;
    }
    function escapeHTML(str) { return str.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;'); }
</script>
</body>
</html>