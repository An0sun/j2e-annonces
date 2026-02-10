package org.j2e.servlet;

import org.j2e.bean.Annonce;
import org.j2e.bean.Category;
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
 * Servlet pour modifier une annonce.
 */
@WebServlet("/AnnonceUpdate")
public class AnnonceUpdate extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private final AnnonceService annonceService = new AnnonceService();
    private final CategoryService categoryService = new CategoryService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String idStr = request.getParameter("id");

        if (idStr != null && !idStr.isEmpty()) {
            Long id = Long.parseLong(idStr);
            Annonce annonce = annonceService.getAnnonceById(id);

            if (annonce != null) {
                List<Category> categories = categoryService.getAllCategories();
                request.setAttribute("annonce", annonce);
                request.setAttribute("categories", categories);
                this.getServletContext().getRequestDispatcher("/AnnonceUpdate.jsp").forward(request, response);
                return;
            }
        }
        response.sendRedirect("AnnonceList");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        Long id = Long.parseLong(request.getParameter("id"));
        String title = request.getParameter("title");
        String description = request.getParameter("description");
        String adress = request.getParameter("adress");
        String mail = request.getParameter("mail");
        String categoryIdStr = request.getParameter("categoryId");

        try {
            Annonce annonce = new Annonce(title, description, adress, mail);
            annonce.setId(id);
            Long categoryId = (categoryIdStr != null && !categoryIdStr.isEmpty()) ?
                              Long.parseLong(categoryIdStr) : null;

            annonceService.updateAnnonce(annonce, categoryId);
            response.sendRedirect("AnnonceList");
        } catch (Exception e) {
            request.setAttribute("error", "Erreur : " + e.getMessage());
            Annonce annonce = annonceService.getAnnonceById(id);
            List<Category> categories = categoryService.getAllCategories();
            request.setAttribute("annonce", annonce);
            request.setAttribute("categories", categories);
            this.getServletContext().getRequestDispatcher("/AnnonceUpdate.jsp").forward(request, response);
        }
    }
}