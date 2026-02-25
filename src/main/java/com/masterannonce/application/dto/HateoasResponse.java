package com.masterannonce.application.dto;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * DTO wrapper léger pour ajouter des liens HATEOAS
 * sans dépendre de spring-boot-starter-hateoas.
 * Produit un JSON de la forme :
 * <pre>
 *   { ...content fields..., "_links": { "self": {"href":"..."}, ... } }
 * </pre>
 */
public record HateoasResponse<T>(
    T content,
    Map<String, LinkRef> _links
) {

    public record LinkRef(String href) {}

    /**
     * Builder utilitaire pour construire une réponse HATEOAS.
     */
    public static <T> Builder<T> of(T content) {
        return new Builder<>(content);
    }

    public static class Builder<T> {
        private final T content;
        private final Map<String, LinkRef> links = new LinkedHashMap<>();

        Builder(T content) {
            this.content = content;
        }

        public Builder<T> link(String rel, String href) {
            links.put(rel, new LinkRef(href));
            return this;
        }

        public HateoasResponse<T> build() {
            return new HateoasResponse<>(content, links);
        }
    }
}
