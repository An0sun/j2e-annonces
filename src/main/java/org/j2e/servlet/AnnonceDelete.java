package org.j2e.servlet;

import org.j2e.dao.AnnonceDao;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/AnnonceDelete")
public class AnnonceDelete extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // 1. Récupérer l'ID dans l'URL (ex: AnnonceDelete?id=5)
        String idStr = request.getParameter("id");

        if (idStr != null && !idStr.isEmpty()) {
            int id = Integer.parseInt(idStr);

            // 2. Supprimer via le DAO
            AnnonceDao dao = new AnnonceDao();
            dao.delete(id);
        }

        // 3. Recharger la liste (pour voir qu'elle a disparu)
        response.sendRedirect("AnnonceList");
    }
}