<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    // Rediriger vers la liste des annonces si connecté
    if (session.getAttribute("user") != null) {
        response.sendRedirect("AnnonceList");
    } else {
        response.sendRedirect("login.jsp");
    }
%>