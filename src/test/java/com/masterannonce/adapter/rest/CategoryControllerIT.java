package com.masterannonce.adapter.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.masterannonce.AbstractIntegrationTest;
import com.masterannonce.application.dto.CategoryCreateDTO;
import com.masterannonce.domain.model.Category;
import com.masterannonce.domain.model.Role;
import com.masterannonce.domain.model.User;
import com.masterannonce.infrastructure.persistence.CategoryRepository;
import com.masterannonce.infrastructure.persistence.UserRepository;
import com.masterannonce.infrastructure.security.JwtService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests d'intégration REST pour les catégories.
 */
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class CategoryControllerIT extends AbstractIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private CategoryRepository categoryRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private JwtService jwtService;

    private String adminToken;

    @BeforeEach
    void setUp() {
        // Créer un admin si inexistant
        if (userRepository.findByUsername("catadmin").isEmpty()) {
            User admin = new User("catadmin", "catadmin@test.com", passwordEncoder.encode("pass"));
            admin.setRole(Role.ROLE_ADMIN);
            userRepository.save(admin);
        }
        User admin = userRepository.findByUsername("catadmin").orElseThrow();
        adminToken = jwtService.generateToken(admin.getId(), admin.getUsername(), admin.getRole().name());
    }

    @Test
    @Order(1)
    @DisplayName("GET /api/v1/categories — retourne la liste des catégories")
    void listCategories() throws Exception {
        mockMvc.perform(get("/api/v1/categories"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$").isArray());
    }

    @Test
    @Order(2)
    @DisplayName("POST /api/v1/categories — créer une catégorie (ADMIN)")
    void createCategory() throws Exception {
        CategoryCreateDTO dto = new CategoryCreateDTO("TestCat");

        mockMvc.perform(post("/api/v1/categories")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.label").value("TestCat"));
    }

    @Test
    @Order(3)
    @DisplayName("GET /api/v1/categories/{id} — retourne une catégorie par ID")
    void getCategoryById() throws Exception {
        // S'assurer qu'une catégorie existe
        Category cat = categoryRepository.findAll().stream().findFirst()
            .orElseGet(() -> categoryRepository.save(new Category("GetTest")));

        mockMvc.perform(get("/api/v1/categories/" + cat.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(cat.getId()));
    }

    @Test
    @Order(4)
    @DisplayName("GET /api/v1/categories/{id} — 404 pour ID inexistant")
    void getCategoryById_notFound() throws Exception {
        mockMvc.perform(get("/api/v1/categories/99999"))
            .andExpect(status().isNotFound());
    }

    @Test
    @Order(5)
    @DisplayName("POST /api/v1/categories — 403 sans token admin")
    void createCategory_forbidden() throws Exception {
        // Créer un user normal
        if (userRepository.findByUsername("normaluser").isEmpty()) {
            User user = new User("normaluser", "normal@test.com", passwordEncoder.encode("pass"));
            user.setRole(Role.ROLE_USER);
            userRepository.save(user);
        }
        User user = userRepository.findByUsername("normaluser").orElseThrow();
        String userToken = jwtService.generateToken(user.getId(), user.getUsername(), user.getRole().name());

        CategoryCreateDTO dto = new CategoryCreateDTO("Unauthorized");

        mockMvc.perform(post("/api/v1/categories")
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
            .andExpect(status().isForbidden());
    }
}
