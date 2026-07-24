package com.secondhand.backend.service;

import com.secondhand.backend.constant.Role;
import com.secondhand.backend.entity.User;
import com.secondhand.backend.exception.custom.ResourceNotFoundException;
import com.secondhand.backend.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link UserService}: admin-role detection used by
 * admin-only operations such as approving ads and adding cities.
 *
 * @author Bita Ghiasvand Jozani
 * @author Ata Torkamani Zadeh Alamdari
 * @version 1.0
 */
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User userWithRole(Long id, Role role) {
        User user = new User();
        user.setId(id);
        user.setUsername("user" + id);
        user.setRole(role);
        return user;
    }

    /** A user whose role is ADMIN must be recognized as admin. */
    @Test
    void isAdmin_adminUser_returnsTrue() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(userWithRole(1L, Role.ADMIN)));

        assertTrue(userService.isAdmin(1L));
    }

    /** A regular user must not be recognized as admin. */
    @Test
    void isAdmin_regularUser_returnsFalse() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(userWithRole(2L, Role.USER)));

        assertFalse(userService.isAdmin(2L));
    }

    /** Asking about an unknown user id must fail with 404. */
    @Test
    void isAdmin_unknownUser_throwsNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.isAdmin(99L));
    }
}
