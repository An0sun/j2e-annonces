<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
    <title>Détail Annonce - MasterAnnonce</title>
    <style>
        body { font-family: Arial, sans-serif; background: #f5f5f5; margin: 0; padding: 20px; }
        .container { max-width: 600px; margin: 0 auto; }
        .header { background: #343a40; color: white; padding: 15px 25px; border-radius: 8px; margin-bottom: 20px; display: flex; justify-content: space-between; align-items: center; }
        .header h2 { margin: 0; }
        .header a { color: white; text-decoration: none; }
        .card { background: white; padding: 30px; border-radius: 8px; box-shadow: 0 1px 3px rgba(0,0,0,0.1); }
        .field { margin-bottom: 15px; }
        .field label { font-weight: bold; color: #555; display: block; margin-bottom: 3px; }
        .field .value { color: #333; font-size: 16px; }
        .badge { padding: 4px 10px; border-radius: 12px; font-size: 12px; font-weight: bold; }
        .badge-draft { background: #ffc107; color: #333; }
        .badge-published { background: #28a745; color: white; }
        .badge-archived { background: #6c757d; color: white; }
        .actions { margin-top: 25px; display: flex; gap: 10px; }
        .btn { padding: 10px 20px; border: none; border-radius: 4px; cursor: pointer; font-size: 14px; text-decoration: none; display: inline-block; }
        .btn-warning { background: #ffc107; color: #333; }
        .btn-danger { background: #dc3545; color: white; }
        .btn-success { background: #28a745; color: white; }
        .btn-info { background: #17a2b8; color: white; }
        .btn:hover { opacity: 0.85; }
    </style>
</head>
<body>
    <div class="container">
        <div class="header">
            <h2>📄 Détail de l'annonce</h2>
            <a href="AnnonceList">⬅ Retour à la liste</a>
        </div>

        <div class="card">
            <div class="field">
                <label>Titre</label>
                <div class="value">${annonce.title}</div>
            </div>
            <div class="field">
                <label>Description</label>
                <div class="value">${annonce.description != null ? annonce.description : '-'}</div>
            </div>
            <div class="field">
                <label>Adresse</label>
                <div class="value">${annonce.adress != null ? annonce.adress : '-'}</div>
            </div>
            <div class="field">
                <label>Email</label>
                <div class="value">${annonce.mail != null ? annonce.mail : '-'}</div>
            </div>
            <div class="field">
                <label>Catégorie</label>
                <div class="value">${annonce.category != null ? annonce.category.label : 'Aucune'}</div>
            </div>
            <div class="field">
                <label>Auteur</label>
                <div class="value">${annonce.author != null ? annonce.author.username : '-'}</div>
            </div>
            <div class="field">
                <label>Statut</label>
                <div class="value">
                    <c:choose>
                        <c:when test="${annonce.status == 'DRAFT'}"><span class="badge badge-draft">DRAFT</span></c:when>
                        <c:when test="${annonce.status == 'PUBLISHED'}"><span class="badge badge-published">PUBLISHED</span></c:when>
                        <c:when test="${annonce.status == 'ARCHIVED'}"><span class="badge badge-archived">ARCHIVED</span></c:when>
                    </c:choose>
                </div>
            </div>
            <div class="field">
                <label>Date de création</label>
                <div class="value">${annonce.date}</div>
            </div>

            <div class="actions">
                <a href="AnnonceUpdate?id=${annonce.id}" class="btn btn-warning">✏️ Modifier</a>
                <c:if test="${annonce.status == 'DRAFT'}">
                    <a href="AnnonceAction?id=${annonce.id}&action=publish" class="btn btn-success">📢 Publier</a>
                </c:if>
                <c:if test="${annonce.status == 'PUBLISHED'}">
                    <a href="AnnonceAction?id=${annonce.id}&action=archive" class="btn btn-info">📦 Archiver</a>
                </c:if>
                <a href="AnnonceDelete?id=${annonce.id}" class="btn btn-danger"
                   onclick="return confirm('Supprimer cette annonce ?')">🗑️ Supprimer</a>
            </div>
        </div>
    </div>
</body>
</html>
