package com.systemcontrol.backend.config;

import com.systemcontrol.backend.model.Role;
import com.systemcontrol.backend.model.User;
import com.systemcontrol.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

    @Value("${app.bootstrap.admin.username:admin}")
    private String adminUsername;

    @Value("${app.bootstrap.admin.password:admin}")
    private String adminPassword;

    @Bean
    public CommandLineRunner bootstrapAdmin(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            if (adminUsername == null || adminUsername.isBlank() || adminPassword == null || adminPassword.isBlank()) {
                return;
            }
            userRepository.findByUsername(adminUsername).ifPresentOrElse(
                u -> {
                    // ensure admin has ROLE_ADMIN; if ранее был без роли — назначим
                    if (u.getRole() == null || u.getRole() != Role.ROLE_ADMIN) {
                        u.setRole(Role.ROLE_ADMIN);
                        userRepository.save(u);
                    }
                },
                () -> {
                    User admin = new User();
                    admin.setUsername(adminUsername);
                    admin.setPassword(passwordEncoder.encode(adminPassword));
                    admin.setRole(Role.ROLE_ADMIN);
                    userRepository.save(admin);
                }
            );
        };
    }
}


