package org.j2e.servlet;

import org.j2e.bean.Annonce;
import org.j2e.service.AnnonceService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Servlet pour afficher le détail d'une annonce.
 */
@WebServlet("/AnnonceDetail")
public class AnnonceDetailServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private final AnnonceService annonceService = new AnnonceService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String idStr = request.getParameter("id");

        if (idStr != null && !idStr.isEmpty()) {
            Long id = Long.parseLong(idStr);
            Annonce annonce = annonceService.getAnnonceById(id);

            if (annonce != null) {
                request.setAttribute("annonce", annonce);
                this.getServletContext().getRequestDispatcher("/AnnonceDetail.jsp").forward(request, response);
                return;
            }
        }
        response.sendRedirect("AnnonceList");
    }
}
