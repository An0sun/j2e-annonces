package com.masterannonce.application.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

/**
 * DTO de mise à jour partielle (PATCH).
 * Tous les champs sont nullable — seuls les champs non-null sont appliqués.
 * Classe standard (pas record) car les champs doivent être nullable et mutables pour MapStruct.
 */
public class AnnoncePatchDTO {

    @Size(min = 1, max = 64, message = "Le titre doit contenir entre 1 et 64 caractères")
    private String title;

    @Size(max = 256, message = "La description ne peut pas dépasser 256 caractères")
    private String description;

    @Size(max = 64, message = "L'adresse ne peut pas dépasser 64 caractères")
    private String address;

    @Email(message = "L'email doit être valide")
    @Size(max = 64, message = "L'email ne peut pas dépasser 64 caractères")
    private String mail;

    private Long categoryId;
    private Long version;

    // ===== Getters & Setters =====

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getMail() { return mail; }
    public void setMail(String mail) { this.mail = mail; }

    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }

    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }
}
