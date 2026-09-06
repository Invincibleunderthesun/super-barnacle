package com.harsh.uday.controller;

import com.harsh.uday.dto.ApiResponse;
import com.harsh.uday.exception.ResourceNotFoundException;
import com.harsh.uday.model.User;
import com.harsh.uday.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "Users", description = "User management APIs (Admin)")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Operation(summary = "Get all users (Admin only)")
    @GetMapping
    public ResponseEntity<ApiResponse<List<User>>> getAllUsers() {
        List<User> users = userRepository.findAll();
        // Password is already hidden via @JsonIgnore on User.password
        return ResponseEntity.ok(ApiResponse.success("Users retrieved", users));
    }

    @Operation(summary = "Get user by ID")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<User>> getUserById(@PathVariable Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
        return ResponseEntity.ok(ApiResponse.success("User retrieved", user));
    }

    @Operation(summary = "Get user profile by email")
    @GetMapping("/profile/{email}")
    public ResponseEntity<ApiResponse<User>> getUserByEmail(@PathVariable String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
        return ResponseEntity.ok(ApiResponse.success("User profile retrieved", user));
    }

    @Operation(summary = "Update user role (Admin only)")
    @PutMapping("/{id}/role")
    public ResponseEntity<ApiResponse<User>> updateUserRole(
            @PathVariable Long id,
            @RequestParam String role) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));

        // Validate role
        if (!role.equals("ROLE_USER") && !role.equals("ROLE_ADMIN")) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Invalid role. Use ROLE_USER or ROLE_ADMIN"));
        }

        user.setRole(role);
        User updatedUser = userRepository.save(user);

        return ResponseEntity.ok(ApiResponse.success("User role updated", updatedUser));
    }

    @Operation(summary = "Delete user (Admin only)")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long id) {
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("User", id);
        }
        userRepository.deleteById(id);
        return ResponseEntity.ok(ApiResponse.success("User deleted", null));
    }
}