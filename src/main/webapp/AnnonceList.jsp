<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Liste des annonces</title>
    <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap.min.css">
</head>
<body>

<div class="container">
    <h2 class="page-header">
        <span class="glyphicon glyphicon-home"></span> Liste des annonces
    </h2>

    <div style="margin-bottom: 20px;">
        <a href="AnnonceAdd" class="btn btn-success">
            <span class="glyphicon glyphicon-plus"></span> Ajouter une annonce
        </a>
    </div>

    <div class="list-group">

        <c:forEach items="${annonces}" var="item">
            <div class="list-group-item">
                <h4 class="list-group-item-heading">
                        ${item.title} <small>(${item.mail})</small>

                    <div class="pull-right">
                        <a href="AnnonceUpdate?id=${item.id}" class="btn btn-warning btn-xs" title="Modifier">
                            <span class="glyphicon glyphicon-pencil"></span>
                        </a>
                        <a href="AnnonceDelete?id=${item.id}" class="btn btn-danger btn-xs" title="Supprimer"
                           onclick="return confirm('Êtes-vous sûr de vouloir supprimer cette annonce ?');">
                            <span class="glyphicon glyphicon-trash"></span>
                        </a>
                    </div>
                </h4>

                <p class="list-group-item-text">${item.description}</p>

                <div class="text-right text-muted" style="font-size: 0.9em; margin-top: 10px;">
                    <span class="glyphicon glyphicon-map-marker"></span> ${item.adress} &nbsp;|&nbsp;
                    <span class="glyphicon glyphicon-time"></span>
                    <fmt:formatDate value="${item.date}" pattern="dd/MM/yyyy HH:mm"/>
                </div>
            </div>
        </c:forEach>

        <c:if test="${empty annonces}">
            <div class="alert alert-warning">Aucune annonce disponible pour le moment.</div>
        </c:if>

    </div>
</div>

</body>
</html>