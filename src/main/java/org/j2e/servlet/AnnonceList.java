package org.j2e.servlet;

import org.j2e.bean.Annonce;
import org.j2e.dao.AnnonceDao;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@WebServlet("/AnnonceList")
public class AnnonceList extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // 1. On appelle le DAO pour récupérer la liste
        AnnonceDao dao = new AnnonceDao();
        List<Annonce> listeDesAnnonces = dao.list();

        // 2. On stocke cette liste dans un "carton" (attribut) pour la JSP
        // "annonces" sera le nom de la variable à utiliser dans le JSP
        request.setAttribute("annonces", listeDesAnnonces);

        // 3. On envoie le tout à la page d'affichage
        this.getServletContext().getRequestDispatcher("/AnnonceList.jsp").forward(request, response);
    }
}