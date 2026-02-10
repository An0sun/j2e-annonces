package org.j2e.servlet;

import org.j2e.bean.Annonce;
import org.j2e.bean.Category;
import org.j2e.bean.User;
import org.j2e.service.AnnonceService;
import org.j2e.service.CategoryService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * Servlet pour ajouter une annonce.
 */
@WebServlet("/AnnonceAdd")
public class AnnonceAdd extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private final AnnonceService annonceService = new AnnonceService();
    private final CategoryService categoryService = new CategoryService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Charger les catégories pour le dropdown
        List<Category> categories = categoryService.getAllCategories();
        request.setAttribute("categories", categories);
        this.getServletContext().getRequestDispatcher("/AnnonceAdd.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String title = request.getParameter("title");
        String description = request.getParameter("description");
        String adress = request.getParameter("adress");
        String mail = request.getParameter("mail");
        String categoryIdStr = request.getParameter("categoryId");

        // Récupérer l'utilisateur connecté
        User currentUser = (User) request.getSession().getAttribute("user");

        try {
            Annonce annonce = new Annonce(title, description, adress, mail);
            Long categoryId = (categoryIdStr != null && !categoryIdStr.isEmpty()) ?
                              Long.parseLong(categoryIdStr) : null;

            annonceService.createAnnonce(annonce, currentUser.getId(), categoryId);

            request.setAttribute("message", "Annonce créée avec succès !");
        } catch (Exception e) {
            request.setAttribute("error", "Erreur : " + e.getMessage());
            // Conserver les valeurs saisies
            request.setAttribute("title", title);
            request.setAttribute("description", description);
            request.setAttribute("adress", adress);
            request.setAttribute("mail", mail);
            request.setAttribute("categoryId", categoryIdStr);
        }

        List<Category> categories = categoryService.getAllCategories();
        request.setAttribute("categories", categories);
        this.getServletContext().getRequestDispatcher("/AnnonceAdd.jsp").forward(request, response);
    }
}