package com.masterannonce.adapter.rest;

import com.masterannonce.domain.model.Annonce;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Controller de méta-données : expose dynamiquement (via réflexion/introspection)
 * les champs filtrables et triables de l'entité Annonce.
 */
@RestController
@RequestMapping("/api/meta")
@Tag(name = "Métadonnées", description = "Introspection des entités pour filtres et tris")
public class MetaController {

    /**
     * Retourne la liste des champs filtrables/triables pour Annonce
     * via getDeclaredFields() (introspection demandée par le TP).
     */
    @GetMapping("/annonces")
    @Operation(summary = "Champs filtrables/triables de Annonce",
               description = "Liste générée dynamiquement via réflexion Java")
    public Map<String, Object> getAnnonceMetadata() {
        Field[] fields = Annonce.class.getDeclaredFields();

        List<Map<String, String>> fieldInfos = Arrays.stream(fields)
            .filter(f -> !f.getName().equals("version")) // exclure les champs internes
            .map(f -> {
                Map<String, String> info = new LinkedHashMap<>();
                info.put("name", f.getName());
                info.put("type", f.getType().getSimpleName());
                info.put("filterable", String.valueOf(isFilterable(f)));
                info.put("sortable", String.valueOf(isSortable(f)));
                return info;
            })
            .collect(Collectors.toList());

        // Champs autorisés pour le tri (validation des paramètres `sort`)
        List<String> sortableFields = fieldInfos.stream()
            .filter(f -> "true".equals(f.get("sortable")))
            .map(f -> f.get("name"))
            .toList();

        // Champs de type String pour la recherche LIKE automatique
        List<String> searchableStringFields = Arrays.stream(fields)
            .filter(f -> f.getType() == String.class)
            .filter(f -> !f.getName().equals("mail")) // exclure les champs sensibles
            .map(Field::getName)
            .toList();

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("entity", "Annonce");
        result.put("fields", fieldInfos);
        result.put("sortableFields", sortableFields);
        result.put("searchableStringFields", searchableStringFields);
        return result;
    }

    private boolean isFilterable(Field field) {
        Set<String> filterableFields = Set.of("title", "description", "address", "status", "createdAt", "author", "category");
        return filterableFields.contains(field.getName());
    }

    private boolean isSortable(Field field) {
        Set<String> sortableFields = Set.of("id", "title", "createdAt", "status");
        return sortableFields.contains(field.getName());
    }
}
