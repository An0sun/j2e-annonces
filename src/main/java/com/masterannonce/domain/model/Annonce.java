package com.masterannonce.domain.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.sql.Timestamp;

/**
 * Entité représentant une annonce.
 * Le modèle de domaine est enrichi avec des méthodes métier
 * pour gérer le cycle de vie du statut (DRAFT → PUBLISHED → ARCHIVED).
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
    private String address;

    @Email(message = "L'email doit être valide")
    @Size(max = 64, message = "L'email ne peut pas dépasser 64 caractères")
    @Column(length = 64)
    private String mail;

    @Column(name = "created_at", updatable = false)
    private Timestamp createdAt;

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

    @Version
    @Column(name = "version")
    private Long version;

    public Annonce() {
        this.createdAt = new Timestamp(System.currentTimeMillis());
        this.status = AnnonceStatus.DRAFT;
    }

    public Annonce(String title, String description, String address, String mail) {
        this();
        this.title = title;
        this.description = description;
        this.address = address;
        this.mail = mail;
    }

    // ===== Méthodes métier (domaine riche) =====

    /**
     * Vérifie si l'annonce peut être modifiée.
     * Une annonce publiée ne peut plus être modifiée.
     */
    public boolean canBeModified() {
        return this.status != AnnonceStatus.PUBLISHED;
    }

    /**
     * Publie l'annonce (DRAFT → PUBLISHED).
     * @throws IllegalStateException si l'annonce n'est pas en brouillon
     */
    public void publish() {
        if (this.status != AnnonceStatus.DRAFT) {
            throw new IllegalStateException(
                "Seule une annonce en brouillon (DRAFT) peut être publiée (statut actuel: " + this.status + ")");
        }
        this.status = AnnonceStatus.PUBLISHED;
    }

    /**
     * Archive l'annonce (→ ARCHIVED).
     */
    public void archive() {
        this.status = AnnonceStatus.ARCHIVED;
    }

    /**
     * Vérifie si l'annonce peut être supprimée.
     * L'annonce doit être archivée avant suppression.
     */
    public boolean canBeDeleted() {
        return this.status == AnnonceStatus.ARCHIVED;
    }

    /**
     * Vérifie si l'utilisateur donné est l'auteur de cette annonce.
     */
    public boolean isAuthor(Long userId) {
        return this.author != null && this.author.getId().equals(userId);
    }

    // ===== Getters & Setters =====

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getMail() { return mail; }
    public void setMail(String mail) { this.mail = mail; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public AnnonceStatus getStatus() { return status; }
    public void setStatus(AnnonceStatus status) { this.status = status; }

    public User getAuthor() { return author; }
    public void setAuthor(User author) { this.author = author; }

    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }

    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }
}
