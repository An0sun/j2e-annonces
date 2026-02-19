package org.j2e.rest.dto;

import javax.validation.constraints.NotNull;

/**
 * DTO pour la requête de login.
 */
public class LoginDTO {

    @NotNull(message = "Le nom d'utilisateur est obligatoire")
    private String username;

    @NotNull(message = "Le mot de passe est obligatoire")
    private String password;

    public LoginDTO() {
    }

    public LoginDTO(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
