package org.j2e.servlet;

import org.j2e.bean.Annonce;
import org.j2e.bean.AnnonceStatus;
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
 * Servlet pour lister les annonces avec pagination et filtrage.
 */
@WebServlet("/AnnonceList")
public class AnnonceList extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final int PAGE_SIZE = 5;

    private final AnnonceService annonceService = new AnnonceService();
    private final CategoryService categoryService = new CategoryService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Pagination
        int page = 1;
        String pageStr = request.getParameter("page");
        if (pageStr != null && !pageStr.isEmpty()) {
            try { page = Integer.parseInt(pageStr); } catch (NumberFormatException ignored) {}
        }

        // Filtrage optionnel
        String keyword = request.getParameter("keyword");
        String categoryIdStr = request.getParameter("categoryId");
        String statusStr = request.getParameter("status");

        List<Annonce> annonces;
        long totalCount;

        if (keyword != null && !keyword.trim().isEmpty()) {
            // Recherche par mot-clé
            annonces = annonceService.search(keyword.trim());
            totalCount = annonces.size();
        } else if ((categoryIdStr != null && !categoryIdStr.isEmpty()) ||
                   (statusStr != null && !statusStr.isEmpty())) {
            // Filtrage par catégorie et/ou statut
            Long categoryId = (categoryIdStr != null && !categoryIdStr.isEmpty()) ?
                              Long.parseLong(categoryIdStr) : null;
            AnnonceStatus status = (statusStr != null && !statusStr.isEmpty()) ?
                                   AnnonceStatus.valueOf(statusStr) : null;
            annonces = annonceService.filter(categoryId, status);
            totalCount = annonces.size();
        } else {
            // Liste paginée
            annonces = annonceService.listPaginated(page, PAGE_SIZE);
            totalCount = annonceService.countAnnonces();
        }

        int totalPages = (int) Math.ceil((double) totalCount / PAGE_SIZE);

        // Charger les catégories pour le menu de filtre
        List<Category> categories = categoryService.getAllCategories();

        request.setAttribute("annonces", annonces);
        request.setAttribute("categories", categories);
        request.setAttribute("currentPage", page);
        request.setAttribute("totalPages", totalPages);
        request.setAttribute("keyword", keyword);
        request.setAttribute("categoryId", categoryIdStr);
        request.setAttribute("status", statusStr);
        request.setAttribute("statuses", AnnonceStatus.values());

        this.getServletContext().getRequestDispatcher("/AnnonceList.jsp").forward(request, response);
    }
}