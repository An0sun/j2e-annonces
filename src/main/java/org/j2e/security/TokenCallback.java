package org.j2e.security;

import javax.security.auth.callback.Callback;

/**
 * Callback custom pour transmettre le token Bearer au TokenLoginModule.
 * Utilisé dans le CallbackHandler du SecurityFilter JAAS.
 */
public class TokenCallback implements Callback {

    private String token;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
