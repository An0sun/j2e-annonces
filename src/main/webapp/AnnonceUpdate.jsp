<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Modifier l'annonce</title>
    <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap.min.css">
</head>
<body>

<div class="container">
    <h2>Modifier l'annonce</h2>

    <form action="AnnonceUpdate" method="post" class="well">

        <input type="hidden" name="id" value="${annonce.id}">

        <div class="form-group">
            <label>Titre</label>
            <input type="text" name="title" class="form-control" value="${annonce.title}" required>
        </div>

        <div class="form-group">
            <label>Description</label>
            <textarea name="description" class="form-control" rows="3" required>${annonce.description}</textarea>
        </div>

        <div class="form-group">
            <label>Adresse</label>
            <input type="text" name="adress" class="form-control" value="${annonce.adress}" required>
        </div>

        <div class="form-group">
            <label>Email</label>
            <input type="email" name="mail" class="form-control" value="${annonce.mail}" required>
        </div>

        <button type="submit" class="btn btn-warning">Modifier</button>
        <a href="AnnonceList" class="btn btn-default">Annuler</a>
    </form>
</div>

</body>
</html>