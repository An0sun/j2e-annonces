package com.masterannonce.application.service;

import com.masterannonce.domain.exception.BusinessException;
import com.masterannonce.domain.exception.ResourceNotFoundException;
import com.masterannonce.domain.model.User;
import com.masterannonce.infrastructure.persistence.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service métier pour les Utilisateurs.
 */
@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Inscription d'un nouvel utilisateur.
     * Le mot de passe est hashé avant stockage (BCrypt).
     */
    public User register(User user) {
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new BusinessException("Ce nom d'utilisateur est déjà pris");
        }
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new BusinessException("Cet email est déjà utilisé");
        }

        // Hashage du mot de passe (audit fix : plus jamais en clair)
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    /**
     * Récupérer un utilisateur par ID.
     */
    @Transactional(readOnly = true)
    public User getUserById(Long id) {
        return userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User", id));
    }

    /**
     * Récupérer un utilisateur par username.
     */
    @Transactional(readOnly = true)
    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
            .orElseThrow(() -> new ResourceNotFoundException("User", username));
    }
}
