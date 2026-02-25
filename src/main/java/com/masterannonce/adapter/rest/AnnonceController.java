package com.masterannonce.adapter.rest;

import com.masterannonce.application.dto.*;
import com.masterannonce.application.mapper.AnnonceMapper;
import com.masterannonce.application.service.AnnonceService;
import com.masterannonce.domain.model.Annonce;
import com.masterannonce.domain.model.AnnonceStatus;
import com.masterannonce.infrastructure.security.AuthenticatedUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.sql.Timestamp;

/**
 * Controller REST pour les Annonces.
 * Pas de logique métier ici — tout est délégué au service.
 */
@RestController
@RequestMapping("/api/annonces")
@Tag(name = "Annonces", description = "CRUD et gestion du cycle de vie des annonces")
public class AnnonceController {

    private final AnnonceService annonceService;
    private final AnnonceMapper annonceMapper;

    public AnnonceController(AnnonceService annonceService, AnnonceMapper annonceMapper) {
        this.annonceService = annonceService;
        this.annonceMapper = annonceMapper;
    }

    // ===== GET (publics) =====

    @GetMapping
    @Operation(summary = "Lister les annonces", description = "Recherche paginée avec filtres via Specifications")
    public ResponseEntity<Page<AnnonceDTO>> listAnnonces(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) AnnonceStatus status,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long authorId,
            @RequestParam(required = false) Timestamp fromDate,
            @RequestParam(required = false) Timestamp toDate,
            @PageableDefault(size = 10, sort = "createdAt") Pageable pageable) {

        Page<Annonce> page = annonceService.searchAnnonces(q, status, categoryId, authorId, fromDate, toDate, pageable);
        Page<AnnonceDTO> dtoPage = page.map(annonceMapper::toDTO);
        return ResponseEntity.ok(dtoPage);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Détail d'une annonce")
    public ResponseEntity<AnnonceDTO> getAnnonce(@PathVariable Long id) {
        Annonce annonce = annonceService.getAnnonceById(id);
        return ResponseEntity.ok(annonceMapper.toDTO(annonce));
    }

    // ===== POST / PUT / PATCH / DELETE (protégés) =====

    @PostMapping
    @Operation(summary = "Créer une annonce", description = "L'annonce est créée en statut DRAFT")
    public ResponseEntity<AnnonceDTO> createAnnonce(
            @Valid @RequestBody AnnonceCreateDTO dto,
            @AuthenticationPrincipal AuthenticatedUser user) {

        Annonce entity = annonceMapper.toEntity(dto);
        Annonce saved = annonceService.createAnnonce(entity, user.userId(), dto.categoryId());
        AnnonceDTO response = annonceMapper.toDTO(saved);

        return ResponseEntity.created(URI.create("/api/annonces/" + saved.getId())).body(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Modifier une annonce", description = "Seul l'auteur peut modifier. Interdit si PUBLISHED.")
    public ResponseEntity<AnnonceDTO> updateAnnonce(
            @PathVariable Long id,
            @Valid @RequestBody AnnonceUpdateDTO dto,
            @AuthenticationPrincipal AuthenticatedUser user) {

        Annonce updates = annonceMapper.toEntity(dto);
        updates.setVersion(dto.version());
        Annonce updated = annonceService.updateAnnonce(id, updates, dto.categoryId(), user.userId());
        return ResponseEntity.ok(annonceMapper.toDTO(updated));
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Modifier partiellement une annonce (PATCH)",
               description = "Seuls les champs fournis sont mis à jour")
    public ResponseEntity<AnnonceDTO> patchAnnonce(
            @PathVariable Long id,
            @Valid @RequestBody AnnoncePatchDTO dto,
            @AuthenticationPrincipal AuthenticatedUser user) {

        Annonce patched = annonceService.patchAnnonce(id, dto, user.userId());
        return ResponseEntity.ok(annonceMapper.toDTO(patched));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer une annonce", description = "L'annonce doit être archivée avant suppression")
    public ResponseEntity<Void> deleteAnnonce(
            @PathVariable Long id,
            @AuthenticationPrincipal AuthenticatedUser user) {

        annonceService.deleteAnnonce(id, user.userId());
        return ResponseEntity.noContent().build();
    }

    // ===== Transitions de statut =====

    @PatchMapping("/{id}/publish")
    @Operation(summary = "Publier une annonce", description = "Transition DRAFT → PUBLISHED")
    public ResponseEntity<AnnonceDTO> publishAnnonce(
            @PathVariable Long id,
            @AuthenticationPrincipal AuthenticatedUser user) {

        Annonce published = annonceService.publishAnnonce(id, user.userId());
        return ResponseEntity.ok(annonceMapper.toDTO(published));
    }

    @PatchMapping("/{id}/archive")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Archiver une annonce", description = "Seul un ADMIN peut archiver")
    public ResponseEntity<AnnonceDTO> archiveAnnonce(@PathVariable Long id) {

        Annonce archived = annonceService.archiveAnnonce(id);
        return ResponseEntity.ok(annonceMapper.toDTO(archived));
    }
}
