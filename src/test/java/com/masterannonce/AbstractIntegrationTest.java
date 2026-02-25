package com.masterannonce;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Classe de base pour les tests d'intégration.
 * Utilise Testcontainers via l'URL JDBC (jdbc:tc:postgresql://...)
 * pour démarrer automatiquement PostgreSQL.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Testcontainers
public abstract class AbstractIntegrationTest {
    // PostgreSQL est démarré automatiquement via la JDBC URL Testcontainers
    // dans application-test.yml : jdbc:tc:postgresql:16-alpine:///MasterAnnonce
}
