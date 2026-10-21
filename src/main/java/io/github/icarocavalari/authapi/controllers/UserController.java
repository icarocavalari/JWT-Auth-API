package io.github.icarocavalari.authapi.controllers;


import io.github.icarocavalari.authapi.dtos.UserDto;
import io.github.icarocavalari.authapi.entities.RoleEnum;
import io.github.icarocavalari.authapi.entities.User;
import io.github.icarocavalari.authapi.services.UserService;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.data.web.SortDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@SecurityRequirement(name = "bearerAuth")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User information successfully fetched"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid token")})
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/users/me")
    public ResponseEntity<UserDto> authenticatedUser(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(new UserDto(user.getId(), user.getUsername(), user.getFullName(), user.getCreatedAt()));
    }

    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "All users information successfully fetched"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid token"),
            @ApiResponse(responseCode = "403", description = "User doesn't have permission to access this endpoint")})
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @GetMapping("/users")
    public ResponseEntity<PagedModel<UserDto>> allUsers(
            @PageableDefault(page = 0, size = 10, sort = "id", direction = Sort.Direction.ASC)
            @ParameterObject Pageable pageable) {
        return ResponseEntity.ok(new PagedModel<>(userService.allUsers(pageable)));
    }

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Admin role successfully added to the user"),
            @ApiResponse(responseCode = "404", description = "User id was not found"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid token"),
            @ApiResponse(responseCode = "403", description = "User doesn't have permission to access this endpoint")
    })
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @PutMapping("/users/{id}/admin")
    public ResponseEntity<UserDto> createAdmin(@PathVariable Long id) {
        return ResponseEntity.ok(userService.createAdmin(id));
    }
}
