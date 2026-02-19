package org.j2e.rest;

import io.swagger.v3.jaxrs2.integration.resources.OpenApiResource;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
import java.util.HashSet;
import java.util.Set;

/**
 * Point d'entrée JAX-RS.
 * Toutes les ressources REST sont exposées sous /api.
 *
 * Choix de configuration : On utilise Jersey (implémentation JAX-RS de
 * référence)
 * car c'est l'implémentation la plus mature et la mieux intégrée avec les
 * serveurs
 * d'applications Java EE. La configuration se fait via @ApplicationPath sans
 * XML.
 */
@ApplicationPath("/api")
public class JaxRsApplication extends Application {

    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> classes = new HashSet<>();

        // Resources REST
        classes.add(org.j2e.rest.resource.HelloResource.class);
        classes.add(org.j2e.rest.resource.AnnonceResource.class);
        classes.add(org.j2e.rest.resource.AuthResource.class);

        // Exception Mappers
        classes.add(org.j2e.rest.exception.ValidationExceptionMapper.class);
        classes.add(org.j2e.rest.exception.NotFoundExceptionMapper.class);
        classes.add(org.j2e.rest.exception.BusinessExceptionMapper.class);
        classes.add(org.j2e.rest.exception.ForbiddenExceptionMapper.class);
        classes.add(org.j2e.rest.exception.GenericExceptionMapper.class);

        // Filtre de sécurité
        classes.add(org.j2e.rest.filter.SecurityFilter.class);

        // OpenAPI / Swagger
        classes.add(OpenApiResource.class);

        return classes;
    }
}
