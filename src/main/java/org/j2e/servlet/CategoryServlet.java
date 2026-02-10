package org.j2e.servlet;

import org.j2e.bean.Category;
import org.j2e.service.CategoryService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * Servlet pour gérer les catégories (liste + création).
 */
@WebServlet("/Categories")
public class CategoryServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private final CategoryService categoryService = new CategoryService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        List<Category> categories = categoryService.getAllCategories();
        request.setAttribute("categories", categories);
        this.getServletContext().getRequestDispatcher("/CategoryList.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String label = request.getParameter("label");

        try {
            if (label != null && !label.trim().isEmpty()) {
                Category category = new Category(label.trim());
                categoryService.createCategory(category);
                request.setAttribute("message", "Catégorie créée avec succès !");
            }
        } catch (Exception e) {
            request.setAttribute("error", "Erreur : " + e.getMessage());
            request.setAttribute("label", label);
        }

        List<Category> categories = categoryService.getAllCategories();
        request.setAttribute("categories", categories);
        this.getServletContext().getRequestDispatcher("/CategoryList.jsp").forward(request, response);
    }
}
