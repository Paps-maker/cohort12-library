<%@ page import="app.User, app.dao.UserDAO" %>

<%
    String username = request.getParameter("username");
    String email = request.getParameter("email");
    String password = request.getParameter("password");

    // ✔ Correct constructor (matches your updated User class)
    User newUser = new User(username, email, password, "USER");

    UserDAO userDAO = new UserDAO();

    boolean success = userDAO.createUser(newUser);

    if (success) {
        request.setAttribute("username", username);
        request.setAttribute("email", email);
        request.setAttribute("status", "success");
    } else {
        request.setAttribute("status", "exists");
    }
%>

<jsp:forward page="registerdisplay.jsp"/>