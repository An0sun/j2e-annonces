package org.j2e.bean;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;

/**
 * Entité représentant une catégorie d'annonce.
 */
@Entity
@Table(name = "category")
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Le libellé est obligatoire")
    @Size(min = 1, max = 100, message = "Le libellé doit contenir entre 1 et 100 caractères")
    @Column(unique = true, nullable = false, length = 100)
    private String label;

    @OneToMany(mappedBy = "category", fetch = FetchType.LAZY)
    private List<Annonce> annonces = new ArrayList<>();

    public Category() {}

    public Category(String label) {
        this.label = label;
    }

    // Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }

    public List<Annonce> getAnnonces() { return annonces; }
    public void setAnnonces(List<Annonce> annonces) { this.annonces = annonces; }
}
