package com.masterannonce.application.service;

import com.masterannonce.domain.exception.BusinessException;
import com.masterannonce.domain.exception.ResourceNotFoundException;
import com.masterannonce.domain.model.Role;
import com.masterannonce.domain.model.User;
import com.masterannonce.infrastructure.persistence.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for UserService with Mockito.
 */
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @InjectMocks private UserService userService;

    private User sampleUser;

    @BeforeEach
    void setUp() {
        sampleUser = new User("testuser", "test@test.com", "rawPassword");
        sampleUser.setId(1L);
        sampleUser.setRole(Role.ROLE_USER);
    }

    @Test
    @DisplayName("register — success: password is hashed, user is saved")
    void register_success() {
        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(userRepository.existsByEmail("test@test.com")).thenReturn(false);
        when(passwordEncoder.encode("rawPassword")).thenReturn("$2a$10$hashedPassword");
        when(userRepository.save(any(User.class))).thenReturn(sampleUser);

        User result = userService.register(sampleUser);

        assertThat(result).isNotNull();
        verify(passwordEncoder).encode("rawPassword");
        verify(userRepository).save(sampleUser);
    }

    @Test
    @DisplayName("register — duplicate username throws BusinessException")
    void register_duplicateUsername() {
        when(userRepository.existsByUsername("testuser")).thenReturn(true);

        assertThatThrownBy(() -> userService.register(sampleUser))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("nom d'utilisateur");

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("register — duplicate email throws BusinessException")
    void register_duplicateEmail() {
        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(userRepository.existsByEmail("test@test.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.register(sampleUser))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("email");

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("register — default role is ROLE_USER")
    void register_defaultRole() {
        User newUser = new User("newuser", "new@test.com", "pass");
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hashed");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User result = userService.register(newUser);

        assertThat(result.getRole()).isEqualTo(Role.ROLE_USER);
    }

    @Test
    @DisplayName("getUserByUsername — success")
    void getUserByUsername_success() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(sampleUser));

        User result = userService.getUserByUsername("testuser");

        assertThat(result.getUsername()).isEqualTo("testuser");
    }

    @Test
    @DisplayName("getUserByUsername — not found throws ResourceNotFoundException")
    void getUserByUsername_notFound() {
        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserByUsername("unknown"))
            .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("getUserById — not found throws ResourceNotFoundException")
    void getUserById_notFound() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserById(999L))
            .isInstanceOf(ResourceNotFoundException.class);
    }
}
