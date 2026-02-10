<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
    <title>Catégories - MasterAnnonce</title>
    <style>
        body { font-family: Arial, sans-serif; background: #f5f5f5; margin: 0; padding: 20px; }
        .container { max-width: 600px; margin: 0 auto; }
        .header { background: #343a40; color: white; padding: 15px 25px; border-radius: 8px; margin-bottom: 20px; display: flex; justify-content: space-between; align-items: center; }
        .header h2 { margin: 0; }
        .header a { color: white; text-decoration: none; }
        .card { background: white; padding: 25px; border-radius: 8px; box-shadow: 0 1px 3px rgba(0,0,0,0.1); margin-bottom: 20px; }
        .form-inline { display: flex; gap: 10px; }
        .form-inline input { flex: 1; padding: 10px; border: 1px solid #ddd; border-radius: 4px; }
        .btn { padding: 10px 20px; border: none; border-radius: 4px; cursor: pointer; font-size: 14px; }
        .btn-success { background: #28a745; color: white; }
        .btn:hover { opacity: 0.85; }
        table { width: 100%; border-collapse: collapse; }
        th { background: #007bff; color: white; padding: 10px; text-align: left; }
        td { padding: 10px; border-bottom: 1px solid #eee; }
        .error { color: #dc3545; background: #f8d7da; padding: 10px; border-radius: 4px; margin-bottom: 15px; }
        .message { color: #155724; background: #d4edda; padding: 10px; border-radius: 4px; margin-bottom: 15px; }
        .empty { text-align: center; color: #999; padding: 20px; }
    </style>
</head>
<body>
    <div class="container">
        <div class="header">
            <h2>🏷️ Catégories</h2>
            <a href="AnnonceList">⬅ Retour aux annonces</a>
        </div>

        <div class="card">
            <h3>Ajouter une catégorie</h3>
            <c:if test="${not empty message}"><div class="message">${message}</div></c:if>
            <c:if test="${not empty error}"><div class="error">${error}</div></c:if>
            <form method="post" action="Categories" class="form-inline">
                <input type="text" name="label" placeholder="Nom de la catégorie" required
                       value="${label != null ? label : ''}">
                <button type="submit" class="btn btn-success">Ajouter</button>
            </form>
        </div>

        <div class="card">
            <h3>Liste des catégories</h3>
            <c:choose>
                <c:when test="${empty categories}">
                    <div class="empty">Aucune catégorie pour le moment.</div>
                </c:when>
                <c:otherwise>
                    <table>
                        <thead><tr><th>ID</th><th>Libellé</th></tr></thead>
                        <tbody>
                            <c:forEach var="cat" items="${categories}">
                                <tr><td>${cat.id}</td><td>${cat.label}</td></tr>
                            </c:forEach>
                        </tbody>
                    </table>
                </c:otherwise>
            </c:choose>
        </div>
    </div>
</body>
</html>
