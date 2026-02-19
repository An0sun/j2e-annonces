package org.j2e.rest.resource;

import org.j2e.bean.Annonce;
import org.j2e.bean.AnnonceStatus;
import org.j2e.rest.dto.*;
import org.j2e.rest.exception.BusinessException;
import org.j2e.rest.exception.ForbiddenException;
import org.j2e.rest.exception.NotFoundException;
import org.j2e.rest.filter.Secured;
import org.j2e.rest.mapper.AnnonceMapper;
import org.j2e.service.AnnonceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.Valid;
import javax.ws.rs.*;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.List;

/**
 * Resource REST pour les annonces.
 * Exercice 2 : CRUD complet avec pagination, DTOs, et codes HTTP corrects.
 * Exercice 7 : Règles métier avancées (auteur, statut, archivage).
 */
@Path("/annonces")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AnnonceResource {

    private static final Logger log = LoggerFactory.getLogger(AnnonceResource.class);
    private final AnnonceService annonceService = new AnnonceService();

    /**
     * GET /api/annonces - Liste paginée des annonces.
     * Accessible sans authentification.
     */
    @GET
    public Response listAnnonces(
            @QueryParam("page") @DefaultValue("1") int page,
            @QueryParam("size") @DefaultValue("10") int size) {

        log.info("GET /api/annonces - page={}, size={}", page, size);

        List<Annonce> annonces = annonceService.listPaginated(page, size);
        long total = annonceService.countAnnonces();

        List<AnnonceDTO> dtos = AnnonceMapper.toDTOList(annonces);
        PaginatedResponse<AnnonceDTO> response = new PaginatedResponse<>(dtos, page, size, total);

        return Response.ok(response).build();
    }

    /**
     * GET /api/annonces/{id} - Détail d'une annonce.
     * Accessible sans authentification.
     */
    @GET
    @Path("/{id}")
    public Response getAnnonce(@PathParam("id") Long id) {
        log.info("GET /api/annonces/{}", id);

        Annonce annonce = annonceService.getAnnonceById(id);
        if (annonce == null) {
            throw new NotFoundException("Annonce", id);
        }

        return Response.ok(AnnonceMapper.toDTO(annonce)).build();
    }

    /**
     * POST /api/annonces - Création d'une annonce.
     * Nécessite authentification. Le statut est automatiquement DRAFT.
     */
    @POST
    @Secured
    public Response createAnnonce(@Valid AnnonceCreateDTO dto,
            @Context ContainerRequestContext requestContext) {
        Long userId = (Long) requestContext.getProperty("userId");
        log.info("POST /api/annonces - userId={}, title={}", userId, dto.getTitle());

        Annonce annonce = new Annonce(
                dto.getTitle(), dto.getDescription(),
                dto.getAdress(), dto.getMail());

        annonceService.createAnnonce(annonce, userId, dto.getCategoryId());

        AnnonceDTO responseDto = AnnonceMapper.toDTO(
                annonceService.getAnnonceById(annonce.getId()));

        return Response.created(URI.create("/api/annonces/" + annonce.getId()))
                .entity(responseDto)
                .build();
    }

    /**
     * PUT /api/annonces/{id} - Mise à jour complète.
     * Nécessite authentification. Seul l'auteur peut modifier.
     * Une annonce PUBLISHED ne peut plus être modifiée.
     */
    @PUT
    @Path("/{id}")
    @Secured
    public Response updateAnnonce(@PathParam("id") Long id,
            @Valid AnnonceUpdateDTO dto,
            @Context ContainerRequestContext requestContext) {
        Long userId = (Long) requestContext.getProperty("userId");
        log.info("PUT /api/annonces/{} - userId={}", id, userId);

        Annonce existing = annonceService.getAnnonceById(id);
        if (existing == null) {
            throw new NotFoundException("Annonce", id);
        }

        // Règle métier : seul l'auteur peut modifier
        if (existing.getAuthor() != null && !existing.getAuthor().getId().equals(userId)) {
            throw new ForbiddenException("Seul l'auteur peut modifier cette annonce");
        }

        // Règle métier : une annonce PUBLISHED ne peut plus être modifiée
        if (existing.getStatus() == AnnonceStatus.PUBLISHED) {
            throw new BusinessException("Une annonce publiée ne peut plus être modifiée");
        }

        Annonce toUpdate = new Annonce(
                dto.getTitle(), dto.getDescription(),
                dto.getAdress(), dto.getMail());
        toUpdate.setId(id);
        toUpdate.setVersion(dto.getVersion());

        annonceService.updateAnnonce(toUpdate, dto.getCategoryId());

        AnnonceDTO responseDto = AnnonceMapper.toDTO(annonceService.getAnnonceById(id));
        return Response.ok(responseDto).build();
    }

    /**
     * PATCH /api/annonces/{id} - Mise à jour partielle (bonus).
     * Seuls les champs non-null sont appliqués.
     */
    @PATCH
    @Path("/{id}")
    @Secured
    public Response patchAnnonce(@PathParam("id") Long id,
            @Valid AnnoncePatchDTO dto,
            @Context ContainerRequestContext requestContext) {
        Long userId = (Long) requestContext.getProperty("userId");
        log.info("PATCH /api/annonces/{} - userId={}", id, userId);

        Annonce existing = annonceService.getAnnonceById(id);
        if (existing == null) {
            throw new NotFoundException("Annonce", id);
        }

        if (existing.getAuthor() != null && !existing.getAuthor().getId().equals(userId)) {
            throw new ForbiddenException("Seul l'auteur peut modifier cette annonce");
        }

        if (existing.getStatus() == AnnonceStatus.PUBLISHED) {
            throw new BusinessException("Une annonce publiée ne peut plus être modifiée");
        }

        // Appliquer uniquement les champs non-null
        Annonce toUpdate = new Annonce();
        toUpdate.setId(id);
        toUpdate.setTitle(dto.getTitle() != null ? dto.getTitle() : existing.getTitle());
        toUpdate.setDescription(dto.getDescription() != null ? dto.getDescription() : existing.getDescription());
        toUpdate.setAdress(dto.getAdress() != null ? dto.getAdress() : existing.getAdress());
        toUpdate.setMail(dto.getMail() != null ? dto.getMail() : existing.getMail());
        toUpdate.setVersion(dto.getVersion());

        Long categoryId = dto.getCategoryId() != null ? dto.getCategoryId()
                : (existing.getCategory() != null ? existing.getCategory().getId() : null);

        annonceService.updateAnnonce(toUpdate, categoryId);

        AnnonceDTO responseDto = AnnonceMapper.toDTO(annonceService.getAnnonceById(id));
        return Response.ok(responseDto).build();
    }

    /**
     * DELETE /api/annonces/{id} - Suppression.
     * Nécessite authentification. Seul l'auteur peut supprimer.
     * L'annonce doit être ARCHIVED avant suppression.
     */
    @DELETE
    @Path("/{id}")
    @Secured
    public Response deleteAnnonce(@PathParam("id") Long id,
            @Context ContainerRequestContext requestContext) {
        Long userId = (Long) requestContext.getProperty("userId");
        log.info("DELETE /api/annonces/{} - userId={}", id, userId);

        Annonce existing = annonceService.getAnnonceById(id);
        if (existing == null) {
            throw new NotFoundException("Annonce", id);
        }

        if (existing.getAuthor() != null && !existing.getAuthor().getId().equals(userId)) {
            throw new ForbiddenException("Seul l'auteur peut supprimer cette annonce");
        }

        // Règle métier : archivage obligatoire avant suppression
        if (existing.getStatus() != AnnonceStatus.ARCHIVED) {
            throw new BusinessException("L'annonce doit être archivée avant d'être supprimée (statut actuel: "
                    + existing.getStatus() + ")");
        }

        annonceService.deleteAnnonce(id);
        return Response.noContent().build();
    }

    /**
     * POST /api/annonces/{id}/publish - Publier une annonce (DRAFT → PUBLISHED).
     */
    @POST
    @Path("/{id}/publish")
    @Secured
    public Response publishAnnonce(@PathParam("id") Long id,
            @Context ContainerRequestContext requestContext) {
        Long userId = (Long) requestContext.getProperty("userId");
        log.info("POST /api/annonces/{}/publish - userId={}", id, userId);

        Annonce existing = annonceService.getAnnonceById(id);
        if (existing == null) {
            throw new NotFoundException("Annonce", id);
        }

        if (existing.getAuthor() != null && !existing.getAuthor().getId().equals(userId)) {
            throw new ForbiddenException("Seul l'auteur peut publier cette annonce");
        }

        if (existing.getStatus() != AnnonceStatus.DRAFT) {
            throw new BusinessException("Seule une annonce en brouillon (DRAFT) peut être publiée (statut actuel: "
                    + existing.getStatus() + ")");
        }

        annonceService.publishAnnonce(id);

        AnnonceDTO responseDto = AnnonceMapper.toDTO(annonceService.getAnnonceById(id));
        return Response.ok(responseDto).build();
    }

    /**
     * POST /api/annonces/{id}/archive - Archiver une annonce (→ ARCHIVED).
     */
    @POST
    @Path("/{id}/archive")
    @Secured
    public Response archiveAnnonce(@PathParam("id") Long id,
            @Context ContainerRequestContext requestContext) {
        Long userId = (Long) requestContext.getProperty("userId");
        log.info("POST /api/annonces/{}/archive - userId={}", id, userId);

        Annonce existing = annonceService.getAnnonceById(id);
        if (existing == null) {
            throw new NotFoundException("Annonce", id);
        }

        if (existing.getAuthor() != null && !existing.getAuthor().getId().equals(userId)) {
            throw new ForbiddenException("Seul l'auteur peut archiver cette annonce");
        }

        annonceService.archiveAnnonce(id);

        AnnonceDTO responseDto = AnnonceMapper.toDTO(annonceService.getAnnonceById(id));
        return Response.ok(responseDto).build();
    }
}
