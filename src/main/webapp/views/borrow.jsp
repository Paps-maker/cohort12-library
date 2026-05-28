<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Library | Borrow</title>
    <style>
        @import url('https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght=400;600;700;800&display=swap');
        body { font-family: 'Plus Jakarta Sans', sans-serif; background: #f0f2f5; margin: 0; padding: 40px; color: #1a1f36; }
        .container { max-width: 500px; margin: auto; background: white; border-radius: 24px; overflow: hidden; box-shadow: 0 10px 25px rgba(0,0,0,0.05); border: 1px solid #e2e8f0; }
        header { background: #0f172a; color: white; padding: 30px; text-align: center; }
        header h1 { margin: 0; font-size: 24px; letter-spacing: -0.5px; }
        section { padding: 30px; }
        h3 { margin-top: 0; color: #1e293b; }
        .fine-msg { background: #fff1f2; color: #e11d48; padding: 15px; border-radius: 12px; border-left: 4px solid #e11d48; font-weight: 600; margin-bottom: 20px; }
        .admin-msg { background: #eff6ff; color: #1e40af; padding: 15px; border-radius: 12px; border-left: 4px solid #1e40af; font-weight: 600; margin-bottom: 20px; font-size: 14px; }
        select, input[type='number'], input[type='text'] { width: 100%; padding: 14px; border: 1px solid #e2e8f0; border-radius: 12px; font-size: 16px; margin: 10px 0; transition: 0.2s; box-sizing: border-box; font-family: inherit; }
        select:focus, input[type='number']:focus, input[type='text']:focus { outline: none; border-color: #2563eb; }
        label { font-size: 13px; color: #64748b; font-weight: 700; text-transform: uppercase; letter-spacing: 0.5px; }
        button { background: #0f172a; color: white; border: none; width: 100%; padding: 16px; border-radius: 12px; font-weight: 700; cursor: pointer; transition: 0.3s; margin-top: 10px; }
        button:hover { background: #1e293b; transform: translateY(-1px); }
        .verification-badge { font-size: 13px; font-weight: 700; padding: 8px 12px; border-radius: 8px; margin-top: -4px; margin-bottom: 12px; display: none; }
        .badge-success { background: #dcfce7; color: #166534; border: 1px solid #bbf7d0; }
        .badge-danger { background: #fee2e2; color: #991b1b; border: 1px solid #fecaca; }
        .nav-links { margin-top: 25px; display: flex; justify-content: center; gap: 20px; }
        .nav-links a { color: #64748b; text-decoration: none; font-size: 14px; font-weight: 600; padding: 8px 12px; border-radius: 8px; transition: 0.2s; }
        .nav-links a:hover { color: #0f172a; background: #f1f5f9; }
    </style>
    <script>
        let isStudentVerified = false;
        let searchTimeout = null;

        function updateHiddenBookId() {
            const val = document.getElementById('bookSearchInput').value;
            const options = document.getElementById('bookCollection').options;
            const hiddenInput = document.getElementById('hiddenBookId');
            hiddenInput.value = '';
            for (let i = 0; i < options.length; i++) {
                if (options[i].value === val) {
                    hiddenInput.value = options[i].getAttribute('data-id');
                    break;
                }
            }
        }

        function triggerLiveVerification() {
            clearTimeout(searchTimeout);
            const inputVal = document.getElementById('studentTargetInput').value.trim();
            const badge = document.getElementById('verificationBadge');

            if(inputVal.length === 0) {
                badge.style.display = 'none';
                isStudentVerified = false;
                return;
            }

            badge.style.display = 'block';
            badge.className = 'verification-badge';
            badge.style.background = '#f1f5f9';
            badge.style.color = '#475569';
            badge.innerText = 'Searching database records...';

            searchTimeout = setTimeout(() => {
                fetch('${pageContext.request.contextPath}/library/verify-student?target=' + encodeURIComponent(inputVal))
                    .then(res => res.json())
                    .then(data => {
                        if(data.found) {
                            badge.className = 'verification-badge badge-success';
                            badge.innerText = '✅ Active Account Verified: ' + data.username;
                            isStudentVerified = true;
                        } else {
                            badge.className = 'verification-badge badge-danger';
                            badge.innerText = '❌ Unregistered Account. You must register this student first!';
                            isStudentVerified = false;
                        }
                    }).catch(() => {
                        badge.innerText = 'Verification tracking offline';
                        isStudentVerified = false;
                    });
            }, 400);
        }

        function validateFormSubmission(isAdminSession) {
            if(isAdminSession && !isStudentVerified) {
                alert('Cannot execute borrow request  register the student account first.');
                return false;
            }

            const bookIdVal = document.getElementById('hiddenBookId').value;
            if(!bookIdVal) {
                alert('Please pick or type a valid, available book option matched from the collection.');
                return false;
            }
            return true;
        }
    </script>
</head>
<body>
<div class="container">
    <header><h1>${isAdmin ? "Walk-in Borrow request" : "Library Catalog"}</h1></header>
    <section>
        <c:choose>
            <c:when test="${isAdmin}">
                <h3>Walk-In Student Desk</h3>
                <div class="admin-msg">Students or stuff should be a registered user</div>
            </c:when>
            <c:otherwise>
                <h3>Welcome, ${username}</h3>
                <c:if test="${not empty unpaidFines and unpaidFines > 0}">
                    <div class="fine-msg">Unpaid Fine: KSH <fmt:formatNumber value="${unpaidFines}" minFractionDigits="2" maxFractionDigits="2"/><br><small>Borrowing disabled.</small></div>
                </c:if>
            </c:otherwise>
        </c:choose>

        <c:set var="canBorrow" value="${isAdmin or (empty unpaidFines or unpaidFines <= 0)}" />

        <form method="POST" action="${pageContext.request.contextPath}/library/borrow" id="borrowForm" onsubmit="return validateFormSubmission(${isAdmin})">
            <c:if test="${isAdmin}">
                <label>Student or Stuff Name or Email</label>
                <input type="text" id="studentTargetInput" name="targetStudent" placeholder="Enter student username..." oninput="triggerLiveVerification()" required>
                <div id="verificationBadge" class="verification-badge"></div>
            </c:if>

            <label>Select Book</label>
            <input type="text" list="bookCollection" name="bookIdDisplay" id="bookSearchInput" placeholder="-- Type Book Name or Serial No --" required ${not canBorrow ? "disabled" : ""} onchange="updateHiddenBookId()">

            <datalist id="bookCollection">
                <c:forEach var="b" items="${books}">
                    <c:if test="${b.availableCopies > 0}">
                        <option value="${b.title} [Serial: ${b.isbn}]" data-id="${b.id}">
                    </c:if>
                </c:forEach>
            </datalist>

            <input type="hidden" name="bookId" id="hiddenBookId" required>

            <label>Borrow Duration (1-10 Days)</label>
            <input type="number" name="days" min="1" max="10" value="7" required ${not canBorrow ? "disabled" : ""}>

            <button type="submit" id="submitBtn" ${not canBorrow ? "style='background:#cbd5e1; cursor:not-allowed;' disabled" : ""}>
                ${isAdmin ? "Borrow Book" : "Confirm Checkout"}
            </button>
        </form>
    </section>
</div>
<div class="nav-links">
    <a href="${pageContext.request.contextPath}/library/loans">Borrow History</a>
    <a href="${pageContext.request.contextPath}/books">Back</a>
</div>
</body>
</html>