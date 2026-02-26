package com.masterannonce.adapter.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.masterannonce.AbstractIntegrationTest;
import com.masterannonce.application.dto.AnnonceCreateDTO;
import com.masterannonce.application.dto.LoginRequest;
import com.masterannonce.application.dto.LoginResponse;
import com.masterannonce.domain.model.*;
import com.masterannonce.infrastructure.persistence.AnnonceRepository;
import com.masterannonce.infrastructure.persistence.CategoryRepository;
import com.masterannonce.infrastructure.persistence.UserRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests d'intégration REST pour le CRUD Annonces.
 * Utilise Testcontainers PostgreSQL via AbstractIntegrationTest.
 */
@AutoConfigureMockMvc
class AnnonceControllerIT extends AbstractIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private UserRepository userRepository;
    @Autowired private CategoryRepository categoryRepository;
    @Autowired private AnnonceRepository annonceRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    private String userToken;
    private String adminToken;
    private Long userId;
    private Long adminId;
    private Long categoryId;

    @BeforeEach
    void setUp() throws Exception {
        annonceRepository.deleteAll();
        userRepository.deleteAll();
        categoryRepository.deleteAll();

        // Créer un utilisateur standard
        User user = new User("user1", "user1@test.com", passwordEncoder.encode("password"));
        user.setRole(Role.ROLE_USER);
        user = userRepository.save(user);
        userId = user.getId();

        // Créer un admin
        User admin = new User("admin", "admin@test.com", passwordEncoder.encode("password"));
        admin.setRole(Role.ROLE_ADMIN);
        admin = userRepository.save(admin);
        adminId = admin.getId();

        // Créer une catégorie
        Category cat = new Category("Immobilier");
        cat = categoryRepository.save(cat);
        categoryId = cat.getId();

        // Obtenir les tokens
        userToken = login("user1", "password");
        adminToken = login("admin", "password");
    }

    private String login(String username, String password) throws Exception {
        LoginRequest req = new LoginRequest(username, password);
        MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isOk())
            .andReturn();
        return objectMapper.readValue(
            result.getResponse().getContentAsString(), LoginResponse.class).token();
    }

    // ===== GET (public) =====

    @Test
    @DisplayName("GET /api/v1/annonces — Liste publique paginée")
    void listAnnoncesPublic() throws Exception {
        mockMvc.perform(get("/api/v1/annonces"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isArray());
    }

    // ===== POST (authentifié) =====

    @Test
    @DisplayName("POST /api/v1/annonces — Création réussie retourne 201")
    void createAnnonce() throws Exception {
        AnnonceCreateDTO dto = new AnnonceCreateDTO(
            "Appartement 3 pièces", "Bel appartement lumineux", "Paris 11e", "contact@test.com", categoryId);

        mockMvc.perform(post("/api/v1/annonces")
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.content.title").value("Appartement 3 pièces"))
            .andExpect(jsonPath("$.content.status").value("DRAFT"))
            .andExpect(jsonPath("$.content.authorUsername").value("user1"))
            .andExpect(jsonPath("$._links.self.href").exists());
    }

    @Test
    @DisplayName("POST /api/v1/annonces — Sans token retourne 401")
    void createAnnonceWithoutToken() throws Exception {
        AnnonceCreateDTO dto = new AnnonceCreateDTO("Titre", "Desc", "Paris", "t@t.com", null);

        mockMvc.perform(post("/api/v1/annonces")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
            .andExpect(status().isUnauthorized());
    }

    // ===== Cycle de vie complet =====

    @Test
    @DisplayName("CRUD complet : Create → Read → Update → Publish → Archive → Delete")
    void fullCrudLifecycle() throws Exception {
        // CRÉATION
        AnnonceCreateDTO createDto = new AnnonceCreateDTO(
            "Voiture occasion", "Renault Clio 2020", "Lyon", "vente@test.com", categoryId);

        MvcResult createResult = mockMvc.perform(post("/api/v1/annonces")
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createDto)))
            .andExpect(status().isCreated())
            .andReturn();

        Long annonceId = objectMapper.readTree(
            createResult.getResponse().getContentAsString()).get("content").get("id").asLong();

        // LECTURE
        mockMvc.perform(get("/api/v1/annonces/" + annonceId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content.title").value("Voiture occasion"));

        // MISE À JOUR (PUT)
        String updateBody = """
            {
                "title": "Voiture occasion MODIFIÉE",
                "description": "Renault Clio 2020 - mise à jour",
                "address": "Lyon 6e",
                "mail": "vente@test.com",
                "categoryId": %d
            }
            """.formatted(categoryId);

        mockMvc.perform(put("/api/v1/annonces/" + annonceId)
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateBody))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content.title").value("Voiture occasion MODIFIÉE"));

        // PUBLICATION
        mockMvc.perform(patch("/api/v1/annonces/" + annonceId + "/publish")
                .header("Authorization", "Bearer " + userToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content.status").value("PUBLISHED"));

        // MISE À JOUR doit ÉCHOUER (PUBLISHED)
        mockMvc.perform(put("/api/v1/annonces/" + annonceId)
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateBody))
            .andExpect(status().isBadRequest());

        // ARCHIVAGE (ADMIN uniquement)
        mockMvc.perform(patch("/api/v1/annonces/" + annonceId + "/archive")
                .header("Authorization", "Bearer " + adminToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content.status").value("ARCHIVED"));

        // SUPPRESSION
        mockMvc.perform(delete("/api/v1/annonces/" + annonceId)
                .header("Authorization", "Bearer " + userToken))
            .andExpect(status().isNoContent());

        // Vérifier suppression
        mockMvc.perform(get("/api/v1/annonces/" + annonceId))
            .andExpect(status().isNotFound());
    }

    // ===== Sécurité : rôles =====

    @Test
    @DisplayName("PATCH /api/v1/annonces/{id}/archive — USER non-admin reçoit 403")
    void archiveByNonAdmin() throws Exception {
        // Créer une annonce
        AnnonceCreateDTO dto = new AnnonceCreateDTO("Titre", "Desc", "Paris", "t@t.com", null);
        MvcResult result = mockMvc.perform(post("/api/v1/annonces")
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
            .andExpect(status().isCreated())
            .andReturn();

        Long id = objectMapper.readTree(result.getResponse().getContentAsString()).get("content").get("id").asLong();

        // Un USER ne peut pas archiver
        mockMvc.perform(patch("/api/v1/annonces/" + id + "/archive")
                .header("Authorization", "Bearer " + userToken))
            .andExpect(status().isForbidden());
    }

    // ===== Filtres avec token invalide =====

    @Test
    @DisplayName("POST avec token invalide retourne 401")
    void createWithInvalidToken() throws Exception {
        AnnonceCreateDTO dto = new AnnonceCreateDTO("Titre", "Desc", "Paris", "t@t.com", null);

        mockMvc.perform(post("/api/v1/annonces")
                .header("Authorization", "Bearer invalid-token-xyz")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
            .andExpect(status().isUnauthorized());
    }
}
