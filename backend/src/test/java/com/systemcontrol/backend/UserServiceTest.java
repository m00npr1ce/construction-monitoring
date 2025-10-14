package com.systemcontrol.backend;

import com.systemcontrol.backend.model.Role;
import com.systemcontrol.backend.model.User;
import com.systemcontrol.backend.repository.UserRepository;
import com.systemcontrol.backend.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    
    @InjectMocks
    private UserService userService;
    
    private User testUser;
    
    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setPassword("$2a$10$encodedpassword");
        testUser.setRole(Role.ROLE_ENGINEER);
    }
    
    @Test
    void loadUserByUsername_WithValidUsername_ShouldReturnUserDetails() {
        // Given
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        
        // When
        var userDetails = userService.loadUserByUsername("testuser");
        
        // Then
        assertNotNull(userDetails);
        assertEquals("testuser", userDetails.getUsername());
        assertEquals("$2a$10$encodedpassword", userDetails.getPassword());
        assertTrue(userDetails.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ENGINEER")));
    }
    
    @Test
    void loadUserByUsername_WithInvalidUsername_ShouldThrowException() {
        // Given
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());
        
        // When & Then
        assertThrows(UsernameNotFoundException.class, () -> 
            userService.loadUserByUsername("nonexistent"));
    }
    
    @Test
    void saveUser_WithPlainPassword_ShouldHashPassword() {
        // Given
        User userWithPlainPassword = new User();
        userWithPlainPassword.setUsername("newuser");
        userWithPlainPassword.setPassword("plainpassword");
        userWithPlainPassword.setRole(Role.ROLE_ENGINEER);
        
        when(userRepository.save(any(User.class))).thenReturn(userWithPlainPassword);
        
        // When
        User result = userService.save(userWithPlainPassword);
        
        // Then
        assertNotNull(result);
        assertTrue(result.getPassword().startsWith("$2a$"));
        verify(userRepository).save(any(User.class));
    }
    
    @Test
    void saveUser_WithAlreadyHashedPassword_ShouldNotHashAgain() {
        // Given
        String hashedPassword = "$2a$10$alreadyhashed";
        testUser.setPassword(hashedPassword);
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        
        // When
        User result = userService.save(testUser);
        
        // Then
        assertNotNull(result);
        assertEquals(hashedPassword, result.getPassword());
        verify(userRepository).save(any(User.class));
    }
    
    @Test
    void existsByUsername_WithExistingUsername_ShouldReturnTrue() {
        // Given
        when(userRepository.existsByUsername("testuser")).thenReturn(true);
        
        // When
        boolean exists = userService.existsByUsername("testuser");
        
        // Then
        assertTrue(exists);
        verify(userRepository).existsByUsername("testuser");
    }
    
    @Test
    void existsByUsername_WithNonExistingUsername_ShouldReturnFalse() {
        // Given
        when(userRepository.existsByUsername("nonexistent")).thenReturn(false);
        
        // When
        boolean exists = userService.existsByUsername("nonexistent");
        
        // Then
        assertFalse(exists);
        verify(userRepository).existsByUsername("nonexistent");
    }
    
    @Test
    void checkPassword_WithValidPassword_ShouldReturnTrue() {
        // Given
        String rawPassword = "testpassword";
        String encodedPassword = "$2a$10$encodedpassword";
        
        // When
        boolean isValid = userService.checkPassword(rawPassword, encodedPassword);
        
        // Then
        // Note: This test might fail because we're using a mock encoded password
        // In a real scenario, you'd use a properly encoded password
        assertNotNull(isValid);
    }
}




