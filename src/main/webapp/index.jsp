<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Accueil - MasterAnnonce</title>
    <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap.min.css">
    <style>
        /* Un peu de style pour aérer la page */
        body { padding-top: 20px; padding-bottom: 20px; }
        .marketing { margin-top: 30px; }
    </style>
</head>
<body>

<nav class="navbar navbar-default">
    <div class="container-fluid">
        <div class="navbar-header">
      <span class="navbar-brand">
        <span class="glyphicon glyphicon-user"></span>
        Bonjour
        <c:choose>
            <c:when test="${!empty sessionScope.userPrenom}">
                <strong>${sessionScope.userPrenom} ${sessionScope.userNom}</strong>
            </c:when>
            <c:otherwise>Invité</c:otherwise>
        </c:choose>
      </span>
        </div>
        <ul class="nav navbar-nav navbar-right">
            <li><a href="login.jsp"><span class="glyphicon glyphicon-log-out"></span> Changer d'utilisateur</a></li>
        </ul>
    </div>
</nav>

<div class="container">

    <div class="jumbotron text-center">
        <h1>MasterAnnonce</h1>
        <p class="lead">Bienvenue sur votre application de gestion d'annonces développée en Java EE.</p>

        <p>
            <a class="btn btn-lg btn-success" href="AnnonceList" role="button">
                <span class="glyphicon glyphicon-list-alt"></span> Accéder aux annonces
            </a>
        </p>
    </div>

    <div class="row marketing">

        <div class="col-lg-6">
            <h3><span class="glyphicon glyphicon-cog"></span> Fonctionnalités</h3>
            <p>Cette application permet une gestion complète (CRUD) :</p>
            <ul class="list-group">
                <li class="list-group-item list-group-item-success"><strong>Lister</strong> toutes les annonces disponibles.</li>
                <li class="list-group-item list-group-item-info"><strong>Ajouter</strong> une nouvelle annonce via un formulaire.</li>
                <li class="list-group-item list-group-item-warning"><strong>Modifier</strong> les détails d'une annonce existante.</li>
                <li class="list-group-item list-group-item-danger"><strong>Supprimer</strong> une annonce de la base de données.</li>
            </ul>
        </div>

        <div class="col-lg-6">
            <h3><span class="glyphicon glyphicon-hdd"></span> Technique & Architecture</h3>
            <p>Ce projet a été réalisé en mettant en œuvre :</p>
            <ul class="list-group">
                <li class="list-group-item">Architecture <strong>MVC</strong> (Modèle - Vue - Contrôleur).</li>
                <li class="list-group-item"><strong>Servlets & JSP</strong> avec la librairie JSTL.</li>
                <li class="list-group-item">Base de données <strong>PostgreSQL</strong> (via Docker).</li>
                <li class="list-group-item">Connexion via <strong>JDBC</strong> et pattern <strong>DAO</strong>.</li>
                <li class="list-group-item">Design responsive avec <strong>Bootstrap 3</strong>.</li>
            </ul>
        </div>
    </div>

    <footer class="footer text-center text-muted" style="margin-top:50px; border-top: 1px solid #eee; padding-top: 10px;">
        <p>&copy; 2026 TP Java EE - BUT 3A Kyllian MERIDJA</p>
    </footer>

</div> </body>
</html>