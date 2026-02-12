package org.j2e.rest.dto;

/**
 * DTO de réponse après un login réussi.
 */
public class TokenResponseDTO {

    private String token;
    private String username;
    private Long userId;

    public TokenResponseDTO() {
    }

    public TokenResponseDTO(String token, String username, Long userId) {
        this.token = token;
        this.username = username;
        this.userId = userId;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
}
