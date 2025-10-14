package com.systemcontrol.backend;

import com.systemcontrol.backend.controller.AuthController;
import com.systemcontrol.backend.dto.AuthRequest;
import com.systemcontrol.backend.dto.AuthResponse;
import com.systemcontrol.backend.dto.RegisterRequest;
import com.systemcontrol.backend.model.Role;
import com.systemcontrol.backend.model.User;
import com.systemcontrol.backend.security.JwtUtil;
import com.systemcontrol.backend.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@org.mockito.junit.jupiter.MockitoSettings(strictness = org.mockito.quality.Strictness.LENIENT)
class AuthControllerTest {

    @Mock
    private AuthenticationManager authenticationManager;
    
    @Mock
    private UserService userService;
    
    @Mock
    private JwtUtil jwtUtil;
    
    @Mock
    private Authentication authentication;
    
    @InjectMocks
    private AuthController authController;
    
    @BeforeEach
    void setUp() {
        when(authentication.isAuthenticated()).thenReturn(true);
    }
    
    @Test
    void register_WithValidData_ShouldReturnSuccess() {
        // Given
        RegisterRequest request = new RegisterRequest();
        request.setUsername("newuser");
        request.setPassword("password123");
        
        when(userService.existsByUsername("newuser")).thenReturn(false);
        when(userService.save(any(User.class))).thenReturn(new User());
        
        // When
        ResponseEntity<?> response = authController.register(request);
        
        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("registered", response.getBody());
        verify(userService).existsByUsername("newuser");
        verify(userService).save(any(User.class));
    }
    
    @Test
    void register_WithExistingUsername_ShouldReturnBadRequest() {
        // Given
        RegisterRequest request = new RegisterRequest();
        request.setUsername("existinguser");
        request.setPassword("password123");
        
        when(userService.existsByUsername("existinguser")).thenReturn(true);
        
        // When
        ResponseEntity<?> response = authController.register(request);
        
        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Username already exists", response.getBody());
        verify(userService).existsByUsername("existinguser");
        verify(userService, never()).save(any(User.class));
    }
    
    @Test
    void register_WithNullUsername_ShouldReturnBadRequest() {
        // Given
        RegisterRequest request = new RegisterRequest();
        request.setUsername(null);
        request.setPassword("password123");
        
        // When
        ResponseEntity<?> response = authController.register(request);
        
        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Invalid", response.getBody());
        verify(userService, never()).save(any(User.class));
    }
    
    @Test
    void register_WithNullPassword_ShouldReturnBadRequest() {
        // Given
        RegisterRequest request = new RegisterRequest();
        request.setUsername("newuser");
        request.setPassword(null);
        
        // When
        ResponseEntity<?> response = authController.register(request);
        
        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Invalid", response.getBody());
        verify(userService, never()).save(any(User.class));
    }
    
    @Test
    void login_WithValidCredentials_ShouldReturnToken() {
        // Given
        AuthRequest request = new AuthRequest();
        request.setUsername("testuser");
        request.setPassword("password123");
        
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(jwtUtil.generateToken("testuser")).thenReturn("jwt-token");
        
        // When
        ResponseEntity<?> response = authController.login(request);
        
        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody() instanceof AuthResponse);
        AuthResponse authResponse = (AuthResponse) response.getBody();
        assertEquals("jwt-token", authResponse.getToken());
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtUtil).generateToken("testuser");
    }
}




