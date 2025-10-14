package com.systemcontrol.backend.security;

import com.systemcontrol.backend.service.UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@SuppressWarnings("deprecation")
public class SecurityConfig {

    private final UserService userService;

    public SecurityConfig(UserService userService) {
        this.userService = userService;
    }

    @Bean
    public JwtUtil jwtUtil(org.springframework.core.env.Environment env) {
        String secret = env.getProperty("app.jwt.secret", "secret-key-should-change");
        long exp = Long.parseLong(env.getProperty("app.jwt.expiration-ms", "86400000"));
        return new JwtUtil(secret, exp);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userService);
        provider.setPasswordEncoder(passwordEncoder());
        return new ProviderManager(provider);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, JwtUtil jwtUtil) throws Exception {
        JwtAuthenticationFilter jwtFilter = new JwtAuthenticationFilter(jwtUtil, userService);
    http.cors().and()
        .csrf().disable()
        .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()
        .authorizeHttpRequests()
        .requestMatchers(org.springframework.http.HttpMethod.OPTIONS).permitAll()
        .requestMatchers("/api/auth/**", "/actuator/**").permitAll()
        // Users admin-only management
        .requestMatchers(org.springframework.http.HttpMethod.PUT, "/api/users/*/role").hasAuthority("ROLE_ADMIN")
        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/users/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_MANAGER")
        // Projects: create/update/delete — only MANAGER or ADMIN; read — all authenticated
        .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/projects/**").hasAnyAuthority("ROLE_MANAGER", "ROLE_ADMIN")
        .requestMatchers(org.springframework.http.HttpMethod.PUT, "/api/projects/**").hasAnyAuthority("ROLE_MANAGER", "ROLE_ADMIN")
        .requestMatchers(org.springframework.http.HttpMethod.DELETE, "/api/projects/**").hasAnyAuthority("ROLE_MANAGER", "ROLE_ADMIN")
        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/projects/**").authenticated()
        // Defects: creation — MANAGER or ADMIN; status updates — ENGINEER/MANAGER/ADMIN; read — all authenticated
        .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/defects/**").hasAnyAuthority("ROLE_MANAGER", "ROLE_ADMIN")
        .requestMatchers(org.springframework.http.HttpMethod.PUT, "/api/defects/**").hasAnyAuthority("ROLE_ENGINEER", "ROLE_MANAGER", "ROLE_ADMIN")
        .requestMatchers(org.springframework.http.HttpMethod.DELETE, "/api/defects/**").hasAnyAuthority("ROLE_MANAGER", "ROLE_ADMIN")
        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/defects/**").authenticated()
        // Comments & Attachments: create/delete — ENGINEER/MANAGER/ADMIN; read — all authenticated
        .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/comments/**").hasAnyAuthority("ROLE_ENGINEER", "ROLE_MANAGER", "ROLE_ADMIN")
        .requestMatchers(org.springframework.http.HttpMethod.DELETE, "/api/comments/**").hasAnyAuthority("ROLE_ENGINEER", "ROLE_MANAGER", "ROLE_ADMIN")
        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/comments/**").authenticated()
        .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/attachments/**").hasAnyAuthority("ROLE_ENGINEER", "ROLE_MANAGER", "ROLE_ADMIN")
        .requestMatchers(org.springframework.http.HttpMethod.DELETE, "/api/attachments/**").hasAnyAuthority("ROLE_ENGINEER", "ROLE_MANAGER", "ROLE_ADMIN")
        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/attachments/**").authenticated()
        // Reports/Analytics — просматривать всем аутентифицированным
        .requestMatchers("/api/reports/**").authenticated()
        // default
        .anyRequest().authenticated();

        http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public org.springframework.web.cors.CorsConfigurationSource corsConfigurationSource(org.springframework.core.env.Environment env) {
        org.springframework.web.cors.CorsConfiguration config = new org.springframework.web.cors.CorsConfiguration();
        String frontend = env.getProperty("app.frontend.url", "http://localhost:5173");
        config.setAllowedOrigins(java.util.List.of(frontend));
        config.setAllowedMethods(java.util.List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(java.util.List.of("*"));
        config.setExposedHeaders(java.util.List.of("Authorization"));
        config.setAllowCredentials(true);
        org.springframework.web.cors.UrlBasedCorsConfigurationSource source = new org.springframework.web.cors.UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
