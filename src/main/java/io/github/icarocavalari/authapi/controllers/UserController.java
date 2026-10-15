package io.github.icarocavalari.authapi.controllers;


import io.github.icarocavalari.authapi.dtos.UserDto;
import io.github.icarocavalari.authapi.entities.RoleEnum;
import io.github.icarocavalari.authapi.entities.User;
import io.github.icarocavalari.authapi.services.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/users/me")
    public ResponseEntity<UserDto> authenticatedUser(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(new UserDto(user.getId(), user.getUsername(), user.getFullName(), user.getCreatedAt()));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @GetMapping("/users")
    public ResponseEntity<List<UserDto>> allUsers() {
        return ResponseEntity.ok(userService.allUsers());
    }

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @PutMapping("/users/{id}/admin")
    public ResponseEntity<UserDto> createAdmin(@PathVariable Long id) {
        return ResponseEntity.ok(userService.createAdmin(id));
    }
}
