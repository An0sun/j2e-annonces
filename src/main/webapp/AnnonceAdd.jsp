<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Ajouter une annonce</title>
    <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap.min.css">
</head>
<body>

<nav class="navbar navbar-default">
    <div class="container-fluid">
        <div class="navbar-header">
            <a class="navbar-brand" href="index.jsp">MasterAnnonce</a>
        </div>
        <ul class="nav navbar-nav navbar-right">
            <li><a href="AnnonceList"><span class="glyphicon glyphicon-list"></span> Voir la liste</a></li>
        </ul>
    </div>
</nav>

<div class="container">
    <h2 class="page-header">Nouvelle Annonce</h2>

    <c:if test="${!empty message}">
        <div class="alert alert-info" role="alert">
            <span class="glyphicon glyphicon-info-sign"></span> ${message}
        </div>
    </c:if>

    <form action="AnnonceAdd" method="post" class="well">

        <div class="form-group">
            <label>Titre</label>
            <input type="text" name="title" class="form-control" placeholder="Titre de l'annonce" required>
        </div>

        <div class="form-group">
            <label>Description</label>
            <textarea name="description" class="form-control" rows="3" placeholder="Détails de l'annonce..." required></textarea>
        </div>

        <div class="form-group">
            <label>Adresse</label>
            <input type="text" name="adress" class="form-control" placeholder="Votre adresse" required>
        </div>

        <div class="form-group">
            <label>Email</label>
            <input type="email" name="mail" class="form-control" placeholder="contact@exemple.com" required>
        </div>

        <hr>

        <div class="text-right">
            <a href="AnnonceList" class="btn btn-default">
                <span class="glyphicon glyphicon-arrow-left"></span> Retour à la liste
            </a>

            <button type="submit" class="btn btn-primary">
                <span class="glyphicon glyphicon-floppy-disk"></span> Enregistrer
            </button>
        </div>

    </form>
</div>

</body>
</html>