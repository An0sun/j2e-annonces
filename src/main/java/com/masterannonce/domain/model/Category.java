package com.masterannonce.domain.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.ArrayList;
import java.util.List;

/**
 * Entité représentant une catégorie d'annonces.
 */
@Entity
@Table(name = "category")
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Le libellé est obligatoire")
    @Size(min = 1, max = 50, message = "Le libellé doit contenir entre 1 et 50 caractères")
    @Column(nullable = false, unique = true, length = 50)
    private String label;

    @OneToMany(mappedBy = "category", fetch = FetchType.LAZY)
    private List<Annonce> annonces = new ArrayList<>();

    public Category() {}

    public Category(String label) {
        this.label = label;
    }

    // ===== Getters & Setters =====

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }

    public List<Annonce> getAnnonces() { return annonces; }
    public void setAnnonces(List<Annonce> annonces) { this.annonces = annonces; }
}
