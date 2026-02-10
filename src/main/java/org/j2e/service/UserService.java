package org.j2e.service;

import org.j2e.bean.User;
import org.j2e.dao.UserRepository;

/**
 * Service métier pour les Utilisateurs.
 * Gère l'authentification et l'inscription.
 */
public class UserService {

    private final UserRepository userRepository = new UserRepository();

    /**
     * Inscription d'un nouvel utilisateur.
     * @throws IllegalArgumentException si le username existe déjà
     */
    public void register(User user) {
        // Vérifier que le username n'existe pas déjà
        User existing = userRepository.findByUsername(user.getUsername());
        if (existing != null) {
            throw new IllegalArgumentException("Ce nom d'utilisateur est déjà pris");
        }
        userRepository.save(user);
    }

    /**
     * Authentification d'un utilisateur.
     * @return l'utilisateur si les credentials sont valides, null sinon
     */
    public User login(String username, String password) {
        User user = userRepository.findByUsername(username);
        if (user != null && user.getPassword().equals(password)) {
            return user;
        }
        return null;
    }

    /**
     * Récupérer un utilisateur par ID.
     */
    public User getUserById(Long id) {
        return userRepository.findById(id);
    }
}
