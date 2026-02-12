package org.j2e.service;

import org.j2e.bean.User;
import org.j2e.dao.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service métier pour les Utilisateurs.
 * Gère l'authentification et l'inscription.
 */
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);
    private final UserRepository userRepository = new UserRepository();

    /**
     * Inscription d'un nouvel utilisateur.
     * 
     * @throws IllegalArgumentException si le username existe déjà
     */
    public void register(User user) {
        log.info("Inscription: username='{}'", user.getUsername());
        // Vérifier que le username n'existe pas déjà
        User existing = userRepository.findByUsername(user.getUsername());
        if (existing != null) {
            log.warn("Inscription refusée, username déjà pris: '{}'", user.getUsername());
            throw new IllegalArgumentException("Ce nom d'utilisateur est déjà pris");
        }
        userRepository.save(user);
        log.info("Inscription réussie: username='{}'", user.getUsername());
    }

    /**
     * Authentification d'un utilisateur.
     * 
     * @return l'utilisateur si les credentials sont valides, null sinon
     */
    public User login(String username, String password) {
        log.debug("Tentative de login: username='{}'", username);
        User user = userRepository.findByUsername(username);
        if (user != null && user.getPassword().equals(password)) {
            log.info("Login réussi: username='{}', userId={}", username, user.getId());
            return user;
        }
        log.warn("Échec de login: username='{}'", username);
        return null;
    }

    /**
     * Récupérer un utilisateur par ID.
     */
    public User getUserById(Long id) {
        return userRepository.findById(id);
    }
}
