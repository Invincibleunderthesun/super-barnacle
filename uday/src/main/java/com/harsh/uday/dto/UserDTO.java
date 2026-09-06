package com.harsh.uday.dto;

import java.time.LocalDateTime;

/**
 * Safe response DTO for user data — no password, no internal fields.
 */
public class UserDTO {
    private Long id;
    private String username;
    private String email;
    private String role;
    private boolean active;
    private LocalDateTime createdAt;

    public UserDTO() {}

    public UserDTO(Long id, String username, String email, String role, boolean active, LocalDateTime createdAt) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.role = role;
        this.active = active;
        this.createdAt = createdAt;
    }

    // Static factory from User entity
    public static UserDTO from(com.harsh.uday.model.User user) {
        return new UserDTO(user.getId(), user.getUsername(), user.getEmail(),
                user.getRole(), user.isActive(), user.getCreatedAt());
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
