<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
    <title>Modifier Annonce - MasterAnnonce</title>
    <style>
        body { font-family: Arial, sans-serif; background: #f5f5f5; margin: 0; padding: 20px; }
        .container { max-width: 600px; margin: 0 auto; }
        .header { background: #343a40; color: white; padding: 15px 25px; border-radius: 8px; margin-bottom: 20px; display: flex; justify-content: space-between; align-items: center; }
        .header h2 { margin: 0; }
        .header a { color: white; text-decoration: none; }
        .card { background: white; padding: 30px; border-radius: 8px; box-shadow: 0 1px 3px rgba(0,0,0,0.1); }
        .form-group { margin-bottom: 15px; }
        label { display: block; margin-bottom: 5px; font-weight: bold; color: #555; }
        input[type="text"], input[type="email"], textarea, select { width: 100%; padding: 10px; border: 1px solid #ddd; border-radius: 4px; box-sizing: border-box; }
        textarea { resize: vertical; min-height: 80px; }
        .btn { padding: 12px 24px; border: none; border-radius: 4px; cursor: pointer; font-size: 16px; }
        .btn-warning { background: #ffc107; color: #333; }
        .btn:hover { opacity: 0.85; }
        .error { color: #dc3545; background: #f8d7da; padding: 10px; border-radius: 4px; margin-bottom: 15px; }
        .info { color: #666; font-size: 13px; margin-bottom: 5px; }
    </style>
</head>
<body>
    <div class="container">
        <div class="header">
            <h2>✏️ Modifier Annonce #${annonce.id}</h2>
            <a href="AnnonceList">⬅ Retour à la liste</a>
        </div>

        <div class="card">
            <c:if test="${not empty error}">
                <div class="error">${error}</div>
            </c:if>

            <p class="info">Statut actuel : <strong>${annonce.status}</strong></p>

            <form method="post" action="AnnonceUpdate">
                <input type="hidden" name="id" value="${annonce.id}">

                <div class="form-group">
                    <label>Titre *</label>
                    <input type="text" name="title" maxlength="64" required value="${annonce.title}">
                </div>
                <div class="form-group">
                    <label>Description</label>
                    <textarea name="description" maxlength="256">${annonce.description}</textarea>
                </div>
                <div class="form-group">
                    <label>Adresse</label>
                    <input type="text" name="adress" maxlength="64" value="${annonce.adress}">
                </div>
                <div class="form-group">
                    <label>Email</label>
                    <input type="email" name="mail" maxlength="64" value="${annonce.mail}">
                </div>
                <div class="form-group">
                    <label>Catégorie</label>
                    <select name="categoryId">
                        <option value="">-- Aucune --</option>
                        <c:forEach var="cat" items="${categories}">
                            <option value="${cat.id}"
                                ${annonce.category != null && annonce.category.id == cat.id ? 'selected' : ''}>
                                ${cat.label}
                            </option>
                        </c:forEach>
                    </select>
                </div>
                <button type="submit" class="btn btn-warning">Enregistrer les modifications</button>
            </form>
        </div>
    </div>
</body>
</html>