package org.j2e.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectionDB {
    // Port 5433 car c'est ma config Docker
    private static String url = "jdbc:postgresql://localhost:5433/MasterAnnonce";
    private static String user = "postgres";
    private static String passwd = "password13";
    private static Connection connect;

    public static Connection getInstance() {
        if (connect == null) {
            try {
                // Chargement du Driver
                Class.forName("org.postgresql.Driver");
                connect = DriverManager.getConnection(url, user, passwd);
                System.out.println("Connexion BDD réussie !");
            } catch (SQLException | ClassNotFoundException e) {
                e.printStackTrace();
                System.out.println("Echec connexion BDD : " + e.getMessage());
            }
        }
        return connect;
    }
}