package com.masterannonce.adapter.rest;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test unitaire pour le MetaController (introspection des champs Annonce).
 */
class MetaControllerTest {

    private final MetaController controller = new MetaController();

    @Test
    @DisplayName("getAnnonceMetadata — retourne l'entité et les champs")
    void getAnnonceMetadata_returnsEntityAndFields() {
        Map<String, Object> result = controller.getAnnonceMetadata();

        assertThat(result.get("entity")).isEqualTo("Annonce");
        assertThat(result.get("fields")).isInstanceOf(List.class);

        @SuppressWarnings("unchecked")
        List<Map<String, String>> fields = (List<Map<String, String>>) result.get("fields");
        assertThat(fields).isNotEmpty();

        // Le champ "version" est exclu
        assertThat(fields).noneMatch(f -> "version".equals(f.get("name")));
    }

    @Test
    @DisplayName("getAnnonceMetadata — identifie les champs filtrables")
    void getAnnonceMetadata_filterableFields() {
        Map<String, Object> result = controller.getAnnonceMetadata();

        @SuppressWarnings("unchecked")
        List<Map<String, String>> fields = (List<Map<String, String>>) result.get("fields");

        // title est filtrable
        Map<String, String> titleField = fields.stream()
            .filter(f -> "title".equals(f.get("name")))
            .findFirst().orElseThrow();
        assertThat(titleField.get("filterable")).isEqualTo("true");

        // mail n'est pas filtrable
        Map<String, String> mailField = fields.stream()
            .filter(f -> "mail".equals(f.get("name")))
            .findFirst().orElseThrow();
        assertThat(mailField.get("filterable")).isEqualTo("false");
    }

    @Test
    @DisplayName("getAnnonceMetadata — identifie les champs triables")
    void getAnnonceMetadata_sortableFields() {
        Map<String, Object> result = controller.getAnnonceMetadata();

        @SuppressWarnings("unchecked")
        List<String> sortableFields = (List<String>) result.get("sortableFields");

        assertThat(sortableFields).contains("id", "title", "createdAt", "status");
        assertThat(sortableFields).doesNotContain("description", "mail");
    }

    @Test
    @DisplayName("getAnnonceMetadata — identifie les champs searchable (String, sauf mail)")
    void getAnnonceMetadata_searchableFields() {
        Map<String, Object> result = controller.getAnnonceMetadata();

        @SuppressWarnings("unchecked")
        List<String> searchable = (List<String>) result.get("searchableStringFields");

        assertThat(searchable).contains("title", "description", "address");
        assertThat(searchable).doesNotContain("mail"); // exclu pour raison de sensibilité
    }

    @Test
    @DisplayName("getAnnonceMetadata — chaque champ contient name, type, filterable, sortable")
    void getAnnonceMetadata_fieldStructure() {
        Map<String, Object> result = controller.getAnnonceMetadata();

        @SuppressWarnings("unchecked")
        List<Map<String, String>> fields = (List<Map<String, String>>) result.get("fields");

        for (Map<String, String> field : fields) {
            assertThat(field).containsKeys("name", "type", "filterable", "sortable");
        }
    }
}
