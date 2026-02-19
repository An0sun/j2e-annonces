package org.j2e.rest.dto;

import java.sql.Timestamp;

/**
 * DTO de réponse pour une annonce.
 * Utilise le pattern Builder pour faciliter le mapping Entity → DTO.
 */
public class AnnonceDTO {

    private Long id;
    private String title;
    private String description;
    private String adress;
    private String mail;
    private Timestamp date;
    private String status;
    private String authorUsername;
    private Long authorId;
    private String categoryLabel;
    private Long categoryId;
    private Long version;

    public AnnonceDTO() {
    }

    // ===== Builder Pattern =====
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final AnnonceDTO dto = new AnnonceDTO();

        public Builder id(Long id) {
            dto.id = id;
            return this;
        }

        public Builder title(String title) {
            dto.title = title;
            return this;
        }

        public Builder description(String description) {
            dto.description = description;
            return this;
        }

        public Builder adress(String adress) {
            dto.adress = adress;
            return this;
        }

        public Builder mail(String mail) {
            dto.mail = mail;
            return this;
        }

        public Builder date(Timestamp date) {
            dto.date = date;
            return this;
        }

        public Builder status(String status) {
            dto.status = status;
            return this;
        }

        public Builder authorUsername(String authorUsername) {
            dto.authorUsername = authorUsername;
            return this;
        }

        public Builder authorId(Long authorId) {
            dto.authorId = authorId;
            return this;
        }

        public Builder categoryLabel(String categoryLabel) {
            dto.categoryLabel = categoryLabel;
            return this;
        }

        public Builder categoryId(Long categoryId) {
            dto.categoryId = categoryId;
            return this;
        }

        public Builder version(Long version) {
            dto.version = version;
            return this;
        }

        public AnnonceDTO build() {
            return dto;
        }
    }

    // ===== Getters & Setters =====
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getAuthorUsername() {
        return authorUsername;
    }

    public void setAuthorUsername(String authorUsername) {
        this.authorUsername = authorUsername;
    }

    public Long getAuthorId() {
        return authorId;
    }

    public void setAuthorId(Long authorId) {
        this.authorId = authorId;
    }

    public String getCategoryLabel() {
        return categoryLabel;
    }

    public void setCategoryLabel(String categoryLabel) {
        this.categoryLabel = categoryLabel;
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
