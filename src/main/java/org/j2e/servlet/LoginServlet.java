package org.j2e.servlet;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@WebServlet("/Login")
public class LoginServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // 1. Récupérer les infos du formulaire
        String nom = request.getParameter("nom");
        String prenom = request.getParameter("prenom");

        // 2. Ouvrir la session utilisateur (la mémoire du navigateur)
        HttpSession session = request.getSession();

        // 3. Sauvegarder les infos dedans pour plus tard
        session.setAttribute("userNom", nom);
        session.setAttribute("userPrenom", prenom);

        // 4. Rediriger vers la page d'accueil (index.jsp)
        response.sendRedirect("index.jsp");
    }
}