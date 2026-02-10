package org.j2e.servlet;

import org.j2e.service.AnnonceService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Servlet pour publier ou archiver une annonce.
 */
@WebServlet("/AnnonceAction")
public class AnnonceActionServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private final AnnonceService annonceService = new AnnonceService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String idStr = request.getParameter("id");
        String action = request.getParameter("action");

        if (idStr != null && action != null) {
            Long id = Long.parseLong(idStr);

            switch (action) {
                case "publish":
                    annonceService.publishAnnonce(id);
                    break;
                case "archive":
                    annonceService.archiveAnnonce(id);
                    break;
            }
        }

        response.sendRedirect("AnnonceList");
    }
}
