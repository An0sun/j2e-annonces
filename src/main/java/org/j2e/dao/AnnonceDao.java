package org.j2e.dao;

import org.j2e.bean.Annonce;
import java.sql.*;
import java.util.ArrayList;

public class AnnonceDao {

    // Méthode pour SAUVEGARDER une annonce
    public void create(Annonce annonce) {
        try {
            Connection con = ConnectionDB.getInstance();
            String sql = "INSERT INTO annonce (title, description, adress, mail, date) VALUES (?, ?, ?, ?, ?)";

            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, annonce.getTitle());
            ps.setString(2, annonce.getDescription());
            ps.setString(3, annonce.getAdress());
            ps.setString(4, annonce.getMail());
            ps.setTimestamp(5, annonce.getDate());

            ps.executeUpdate();
            System.out.println("Annonce créée en base !");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Méthode pour LISTER les annonces
    public ArrayList<Annonce> list() {
        ArrayList<Annonce> list = new ArrayList<>();
        try {
            Connection con = ConnectionDB.getInstance();
            String sql = "SELECT * FROM annonce";
            ResultSet rs = con.createStatement().executeQuery(sql);

            while (rs.next()) {
                Annonce a = new Annonce();
                a.setId(rs.getInt("id"));
                a.setTitle(rs.getString("title"));
                a.setDescription(rs.getString("description"));
                a.setAdress(rs.getString("adress"));
                a.setMail(rs.getString("mail"));
                a.setDate(rs.getTimestamp("date"));
                list.add(a);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    // Méthode pour trouver une annonce via son ID (pour pré-remplir le formulaire)
    public Annonce find(int id) {
        Annonce annonce = null;
        try {
            Connection con = ConnectionDB.getInstance();
            String sql = "SELECT * FROM annonce WHERE id = ?";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                annonce = new Annonce();
                annonce.setId(rs.getInt("id"));
                annonce.setTitle(rs.getString("title"));
                annonce.setDescription(rs.getString("description"));
                annonce.setAdress(rs.getString("adress"));
                annonce.setMail(rs.getString("mail"));
                annonce.setDate(rs.getTimestamp("date"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return annonce;
    }

    // Méthode pour mettre à jour une annonce
    public void update(Annonce annonce) {
        try {
            Connection con = ConnectionDB.getInstance();
            String sql = "UPDATE annonce SET title=?, description=?, adress=?, mail=? WHERE id=?";

            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, annonce.getTitle());
            ps.setString(2, annonce.getDescription());
            ps.setString(3, annonce.getAdress());
            ps.setString(4, annonce.getMail());
            ps.setInt(5, annonce.getId()); // Important : l'ID pour la clause WHERE

            ps.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    // Méthode pour supprimer une annonce
    public void delete(int id) {
        try {
            Connection con = ConnectionDB.getInstance();
            String sql = "DELETE FROM annonce WHERE id = ?";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}