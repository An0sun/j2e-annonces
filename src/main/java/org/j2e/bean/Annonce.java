package org.j2e.bean;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.sql.Timestamp;

/**
 * Entité représentant une annonce.
 */
@Entity
@Table(name = "annonce")
public class Annonce {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Le titre est obligatoire")
    @Size(min = 1, max = 64, message = "Le titre doit contenir entre 1 et 64 caractères")
    @Column(nullable = false, length = 64)
    private String title;

    @Size(max = 256, message = "La description ne peut pas dépasser 256 caractères")
    @Column(length = 256)
    private String description;

    @Size(max = 64, message = "L'adresse ne peut pas dépasser 64 caractères")
    @Column(length = 64)
    private String adress;

    @Email(message = "L'email doit être valide")
    @Size(max = 64, message = "L'email ne peut pas dépasser 64 caractères")
    @Column(length = 64)
    private String mail;

    @Column(name = "date")
    private Timestamp date;

    @NotNull(message = "Le statut est obligatoire")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AnnonceStatus status = AnnonceStatus.DRAFT;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id")
    private User author;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @javax.persistence.Version
    @Column(name = "version")
    private Long version;

    public Annonce() {
        this.date = new Timestamp(System.currentTimeMillis());
        this.status = AnnonceStatus.DRAFT;
    }

    public Annonce(String title, String description, String adress, String mail) {
        this();
        this.title = title;
        this.description = description;
        this.adress = adress;
        this.mail = mail;
    }

    // Getters & Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAdress() {
        return adress;
    }

    public void setAdress(String adress) {
        this.adress = adress;
    }

    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    public Timestamp getDate() {
        return date;
    }

    public void setDate(Timestamp date) {
        this.date = date;
    }

    public AnnonceStatus getStatus() {
        return status;
    }

    public void setStatus(AnnonceStatus status) {
        this.status = status;
    }

    public User getAuthor() {
        return author;
    }

    public void setAuthor(User author) {
        this.author = author;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }
}