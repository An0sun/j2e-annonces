package org.j2e.rest.resource;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Map;

/**
 * Resource de test pour vérifier que JAX-RS fonctionne.
 * Exercice 1 : endpoint simple + passage de paramètres.
 */
@Path("/")
@Produces(MediaType.APPLICATION_JSON)
public class HelloResource {

    /**
     * GET /api/helloWorld
     * Endpoint simple pour vérifier le fonctionnement de JAX-RS.
     */
    @GET
    @Path("/helloWorld")
    public Response helloWorld() {
        return Response.ok(Map.of("message", "Hello World")).build();
    }

    /**
     * GET /api/params?name=xxx
     * Démonstration de @QueryParam.
     */
    @GET
    @Path("/params")
    public Response queryParams(@QueryParam("name") @DefaultValue("World") String name) {
        return Response.ok(Map.of(
                "type", "QueryParam",
                "name", name,
                "greeting", "Bonjour " + name + " !")).build();
    }

    /**
     * GET /api/params/{id}
     * Démonstration de @PathParam.
     */
    @GET
    @Path("/params/{id}")
    public Response pathParams(@PathParam("id") Long id) {
        return Response.ok(Map.of(
                "type", "PathParam",
                "id", id,
                "message", "Ressource #" + id)).build();
    }
}
