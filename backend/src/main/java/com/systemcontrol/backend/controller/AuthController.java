package com.systemcontrol.backend.controller;

import com.systemcontrol.backend.dto.AuthRequest;
import com.systemcontrol.backend.dto.AuthResponse;
import com.systemcontrol.backend.dto.RegisterRequest;
import com.systemcontrol.backend.model.Role;
import com.systemcontrol.backend.model.User;
import com.systemcontrol.backend.security.JwtUtil;
import com.systemcontrol.backend.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final JwtUtil jwtUtil;

    public AuthController(AuthenticationManager authenticationManager, UserService userService, JwtUtil jwtUtil) {
        this.authenticationManager = authenticationManager;
        this.userService = userService;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        if (request.getUsername() == null || request.getPassword() == null) return ResponseEntity.badRequest().body("Invalid");
        if (userService.existsByUsername(request.getUsername())) return ResponseEntity.badRequest().body("Username already exists");
        User u = new User();
        u.setUsername(request.getUsername());
        u.setPassword(request.getPassword()); // Will be hashed in UserService.save()
        // По ТЗ: после регистрации роль не назначается (null). Назначение делает только админ.
        u.setRole(null);
        userService.save(u);
        return ResponseEntity.ok("registered");
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest request) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));
        String token = jwtUtil.generateToken(request.getUsername());
        return ResponseEntity.ok(new AuthResponse(token));
    }
}
