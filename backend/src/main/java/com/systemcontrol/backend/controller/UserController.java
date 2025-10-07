package com.systemcontrol.backend.controller;

import com.systemcontrol.backend.repository.UserRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public static record UserDto(Long id, String username, String role) {}

    @GetMapping
    public List<UserDto> list() {
        return userRepository.findAll().stream()
                .map(u -> new UserDto(u.getId(), u.getUsername(), u.getRole() != null ? u.getRole().name() : null))
                .collect(Collectors.toList());
    }
}
