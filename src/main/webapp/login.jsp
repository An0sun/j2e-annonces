<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Bienvenue - MasterAnnonce</title>
    <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap.min.css">
    <style>
        /* Style pour centrer parfaitement le formulaire au milieu de l'écran */
        body, html { height: 100%; background-color: #f5f5f5; }
        .login-container {
            height: 100%;
            display: flex;
            align-items: center;
            justify-content: center;
        }
        .login-box {
            background: white;
            padding: 30px;
            border-radius: 5px;
            box-shadow: 0 0 10px rgba(0,0,0,0.1);
            width: 100%;
            max-width: 400px;
        }
    </style>
</head>
<body>

<div class="container login-container">
    <div class="login-box text-center">
        <h2 style="margin-bottom: 20px;">Qui êtes-vous ?</h2>

        <form action="Login" method="post">
            <div class="form-group">
                <input type="text" name="nom" class="form-control input-lg" placeholder="Votre Nom" required>
            </div>
            <div class="form-group">
                <input type="text" name="prenom" class="form-control input-lg" placeholder="Votre Prénom" required>
            </div>
            <button type="submit" class="btn btn-primary btn-lg btn-block">Entrer dans l'application</button>
        </form>
    </div>
</div>

</body>
</html>