package org.j2e.bean;

import java.sql.Timestamp;

public class Annonce {
    private Integer id;
    private String title;
    private String description;
    private String adress;
    private String mail;
    private Timestamp date;

    public Annonce() {}

    // Constructeur pratique pour créer une annonce rapidement
    public Annonce(String title, String description, String adress, String mail) {
        this.title = title;
        this.description = description;
        this.adress = adress;
        this.mail = mail;
        this.date = new Timestamp(System.currentTimeMillis());
    }

    // Getters et Setters (Génère-les avec Alt+Inser ou copie ça)
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getAdress() { return adress; }
    public void setAdress(String adress) { this.adress = adress; }
    public String getMail() { return mail; }
    public void setMail(String mail) { this.mail = mail; }
    public Timestamp getDate() { return date; }
    public void setDate(Timestamp date) { this.date = date; }
}