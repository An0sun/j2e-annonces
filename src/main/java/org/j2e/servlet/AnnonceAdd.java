package org.j2e.servlet;

import org.j2e.bean.Annonce;
import org.j2e.dao.AnnonceDao;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

// URL d'accès : http://localhost:8080/J2E/AnnonceAdd
@WebServlet("/AnnonceAdd")
public class AnnonceAdd extends HttpServlet {

    private static final long serialVersionUID = 1L;

    // Méthode appelée quand on demande la page (GET)
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // On affiche simplement la JSP
        this.getServletContext().getRequestDispatcher("/AnnonceAdd.jsp").forward(request, response);
    }

    // Méthode appelée quand on valide le formulaire (POST)
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        // 1. Récupération des données du formulaire
        String title = request.getParameter("title");
        String description = request.getParameter("description");
        String adress = request.getParameter("adress");
        String mail = request.getParameter("mail");

        // 2. Création de l'objet Annonce
        Annonce nouvelleAnnonce = new Annonce(title, description, adress, mail);

        // 3. Appel au DAO pour enregistrer en base de données
        AnnonceDao dao = new AnnonceDao();
        dao.create(nouvelleAnnonce);

        // 4. Confirmation et rechargement de la page
        request.setAttribute("message", "Annonce enregistrée avec succès !");
        this.getServletContext().getRequestDispatcher("/AnnonceAdd.jsp").forward(request, response);
    }
}