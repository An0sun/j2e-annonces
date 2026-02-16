package org.j2e.security;

import java.io.Serializable;
import java.security.Principal;

/**
 * Principal JAAS représentant un utilisateur authentifié.
 * Porte le userId et le username dans le Subject.
 */
public class UserPrincipal implements Principal, Serializable {

    private static final long serialVersionUID = 1L;

    private final Long userId;
    private final String username;

    public UserPrincipal(Long userId, String username) {
        this.userId = userId;
        this.username = username;
    }

    @Override
    public String getName() {
        return username;
    }

    public Long getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    @Override
    public String toString() {
        return "UserPrincipal{userId=" + userId + ", username='" + username + "'}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        UserPrincipal that = (UserPrincipal) o;
        return userId.equals(that.userId) && username.equals(that.username);
    }

    @Override
    public int hashCode() {
        return 31 * userId.hashCode() + username.hashCode();
    }
}
