package org.j2e.rest.dto;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * DTO de mise à jour complète (PUT) d'une annonce.
 */
public class AnnonceUpdateDTO {

    @NotNull(message = "Le titre est obligatoire")
    @Size(min = 1, max = 64, message = "Le titre doit contenir entre 1 et 64 caractères")
    private String title;

    @Size(max = 256, message = "La description ne peut pas dépasser 256 caractères")
    private String description;

    @Size(max = 64, message = "L'adresse ne peut pas dépasser 64 caractères")
    private String adress;

    @Email(message = "L'email doit être valide")
    @Size(max = 64, message = "L'email ne peut pas dépasser 64 caractères")
    private String mail;

    private Long categoryId;

    private Long version;

    public AnnonceUpdateDTO() {
    }

    // Getters & Setters
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

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }
}
