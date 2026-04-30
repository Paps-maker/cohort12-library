<%
    String name = request.getParameter("name");
    String email = request.getParameter("email");
    String message = request.getParameter("message");
%>

<jsp:forward page="contactdisplay.jsp">
    <jsp:param name="name" value="<%= name %>" />
    <jsp:param name="email" value="<%= email %>" />
    <jsp:param name="message" value="<%= message %>" />
</jsp:forward>