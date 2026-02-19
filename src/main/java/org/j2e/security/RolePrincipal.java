package org.j2e.security;

import java.io.Serializable;
import java.security.Principal;

/**
 * Principal JAAS représentant un rôle utilisateur.
 * Permet de matérialiser les rôles dans le Subject JAAS.
 */
public class RolePrincipal implements Principal, Serializable {

    private static final long serialVersionUID = 1L;

    public static final String ROLE_USER = "ROLE_USER";

    private final String role;

    public RolePrincipal(String role) {
        this.role = role;
    }

    @Override
    public String getName() {
        return role;
    }

    public String getRole() {
        return role;
    }

    @Override
    public String toString() {
        return "RolePrincipal{role='" + role + "'}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        RolePrincipal that = (RolePrincipal) o;
        return role.equals(that.role);
    }

    @Override
    public int hashCode() {
        return role.hashCode();
    }
}
