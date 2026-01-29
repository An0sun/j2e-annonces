package org.j2e.servlet;

import org.j2e.bean.Annonce;
import org.j2e.dao.AnnonceDao;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/AnnonceUpdate")
public class AnnonceUpdate extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // 1. On récupère l'ID depuis l'URL
        String idStr = request.getParameter("id");

        if (idStr != null && !idStr.isEmpty()) {
            int id = Integer.parseInt(idStr);
            AnnonceDao dao = new AnnonceDao();
            // 2. On cherche l'annonce correspondante
            Annonce annonce = dao.find(id);
            // 3. On l'envoie à la JSP pour pré-remplir les champs
            request.setAttribute("annonce", annonce);
            this.getServletContext().getRequestDispatcher("/AnnonceUpdate.jsp").forward(request, response);
        } else {
            // Si pas d'ID, on renvoie vers la liste
            response.sendRedirect("AnnonceList");
        }
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // 1. Récupération des données (y compris l'ID caché)
        int id = Integer.parseInt(request.getParameter("id"));
        String title = request.getParameter("title");
        String description = request.getParameter("description");
        String adress = request.getParameter("adress");
        String mail = request.getParameter("mail");

        Annonce annonce = new Annonce(title, description, adress, mail);
        annonce.setId(id); // Important !

        // 2. Mise à jour en BDD
        AnnonceDao dao = new AnnonceDao();
        dao.update(annonce);

        // 3. Retour à la liste
        response.sendRedirect("AnnonceList");
    }
}