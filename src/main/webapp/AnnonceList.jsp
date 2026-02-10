<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
    <title>Liste des Annonces - MasterAnnonce</title>
    <style>
        body { font-family: Arial, sans-serif; background: #f5f5f5; margin: 0; padding: 20px; }
        .header { display: flex; justify-content: space-between; align-items: center; background: #343a40; color: white; padding: 15px 25px; border-radius: 8px; margin-bottom: 20px; }
        .header h1 { margin: 0; font-size: 22px; }
        .header a { color: white; text-decoration: none; margin-left: 15px; }
        .header a:hover { text-decoration: underline; }
        .nav { display: flex; align-items: center; gap: 15px; }
        .container { max-width: 1000px; margin: 0 auto; }

        /* Filtres */
        .filters { background: white; padding: 15px 20px; border-radius: 8px; margin-bottom: 20px; box-shadow: 0 1px 3px rgba(0,0,0,0.1); }
        .filters form { display: flex; gap: 10px; align-items: center; flex-wrap: wrap; }
        .filters input[type="text"], .filters select { padding: 8px; border: 1px solid #ddd; border-radius: 4px; }
        .filters input[type="text"] { flex: 1; min-width: 200px; }

        /* Tableau */
        table { width: 100%; background: white; border-collapse: collapse; border-radius: 8px; overflow: hidden; box-shadow: 0 1px 3px rgba(0,0,0,0.1); }
        th { background: #007bff; color: white; padding: 12px 15px; text-align: left; }
        td { padding: 10px 15px; border-bottom: 1px solid #eee; }
        tr:hover { background: #f8f9fa; }

        /* Status badges */
        .badge { padding: 4px 10px; border-radius: 12px; font-size: 12px; font-weight: bold; }
        .badge-draft { background: #ffc107; color: #333; }
        .badge-published { background: #28a745; color: white; }
        .badge-archived { background: #6c757d; color: white; }

        /* Boutons */
        .btn { padding: 6px 12px; border: none; border-radius: 4px; cursor: pointer; text-decoration: none; font-size: 13px; display: inline-block; }
        .btn-primary { background: #007bff; color: white; }
        .btn-success { background: #28a745; color: white; }
        .btn-warning { background: #ffc107; color: #333; }
        .btn-danger { background: #dc3545; color: white; }
        .btn-info { background: #17a2b8; color: white; }
        .btn:hover { opacity: 0.85; }

        /* Pagination */
        .pagination { margin-top: 20px; text-align: center; }
        .pagination a, .pagination span { padding: 8px 14px; margin: 0 3px; border-radius: 4px; text-decoration: none; }
        .pagination a { background: white; color: #007bff; border: 1px solid #ddd; }
        .pagination a:hover { background: #007bff; color: white; }
        .pagination span.current { background: #007bff; color: white; }
        .actions { display: flex; gap: 5px; flex-wrap: wrap; }

        .message { color: #155724; background: #d4edda; padding: 10px; border-radius: 4px; margin-bottom: 15px; }
        .empty { text-align: center; color: #999; padding: 40px; }
    </style>
</head>
<body>
    <div class="container">
        <!-- Header -->
        <div class="header">
            <h1>📋 MasterAnnonce</h1>
            <div class="nav">
                <span>Bienvenue, ${sessionScope.user.username}</span>
                <a href="Categories">🏷️ Catégories</a>
                <a href="AnnonceAdd">➕ Nouvelle annonce</a>
                <a href="Logout">🚪 Déconnexion</a>
            </div>
        </div>

        <!-- Filtres et recherche -->
        <div class="filters">
            <form method="get" action="AnnonceList">
                <input type="text" name="keyword" placeholder="🔍 Rechercher..." value="${keyword}">
                <select name="categoryId">
                    <option value="">-- Catégorie --</option>
                    <c:forEach var="cat" items="${categories}">
                        <option value="${cat.id}" ${cat.id == categoryId ? 'selected' : ''}>${cat.label}</option>
                    </c:forEach>
                </select>
                <select name="status">
                    <option value="">-- Statut --</option>
                    <c:forEach var="st" items="${statuses}">
                        <option value="${st}" ${st == status ? 'selected' : ''}>${st}</option>
                    </c:forEach>
                </select>
                <button type="submit" class="btn btn-primary">Filtrer</button>
                <a href="AnnonceList" class="btn btn-info">Réinitialiser</a>
            </form>
        </div>

        <!-- Tableau des annonces -->
        <c:choose>
            <c:when test="${empty annonces}">
                <div class="empty">
                    <h3>Aucune annonce trouvée</h3>
                    <p><a href="AnnonceAdd">Créer votre première annonce</a></p>
                </div>
            </c:when>
            <c:otherwise>
                <table>
                    <thead>
                        <tr>
                            <th>ID</th>
                            <th>Titre</th>
                            <th>Catégorie</th>
                            <th>Auteur</th>
                            <th>Statut</th>
                            <th>Date</th>
                            <th>Actions</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:forEach var="annonce" items="${annonces}">
                            <tr>
                                <td>${annonce.id}</td>
                                <td><a href="AnnonceDetail?id=${annonce.id}">${annonce.title}</a></td>
                                <td>${annonce.category != null ? annonce.category.label : '-'}</td>
                                <td>${annonce.author != null ? annonce.author.username : '-'}</td>
                                <td>
                                    <c:choose>
                                        <c:when test="${annonce.status == 'DRAFT'}">
                                            <span class="badge badge-draft">DRAFT</span>
                                        </c:when>
                                        <c:when test="${annonce.status == 'PUBLISHED'}">
                                            <span class="badge badge-published">PUBLISHED</span>
                                        </c:when>
                                        <c:when test="${annonce.status == 'ARCHIVED'}">
                                            <span class="badge badge-archived">ARCHIVED</span>
                                        </c:when>
                                    </c:choose>
                                </td>
                                <td>${annonce.date}</td>
                                <td>
                                    <div class="actions">
                                        <a href="AnnonceUpdate?id=${annonce.id}" class="btn btn-warning">✏️</a>
                                        <c:if test="${annonce.status == 'DRAFT'}">
                                            <a href="AnnonceAction?id=${annonce.id}&action=publish" class="btn btn-success">📢</a>
                                        </c:if>
                                        <c:if test="${annonce.status == 'PUBLISHED'}">
                                            <a href="AnnonceAction?id=${annonce.id}&action=archive" class="btn btn-info">📦</a>
                                        </c:if>
                                        <a href="AnnonceDelete?id=${annonce.id}" class="btn btn-danger"
                                           onclick="return confirm('Supprimer cette annonce ?')">🗑️</a>
                                    </div>
                                </td>
                            </tr>
                        </c:forEach>
                    </tbody>
                </table>

                <!-- Pagination -->
                <c:if test="${totalPages > 1}">
                    <div class="pagination">
                        <c:if test="${currentPage > 1}">
                            <a href="AnnonceList?page=${currentPage - 1}">« Précédent</a>
                        </c:if>
                        <c:forEach begin="1" end="${totalPages}" var="i">
                            <c:choose>
                                <c:when test="${i == currentPage}">
                                    <span class="current">${i}</span>
                                </c:when>
                                <c:otherwise>
                                    <a href="AnnonceList?page=${i}">${i}</a>
                                </c:otherwise>
                            </c:choose>
                        </c:forEach>
                        <c:if test="${currentPage < totalPages}">
                            <a href="AnnonceList?page=${currentPage + 1}">Suivant »</a>
                        </c:if>
                    </div>
                </c:if>
            </c:otherwise>
        </c:choose>
    </div>
</body>
</html>