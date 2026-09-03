package com.harsh.firstApp.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Token for password reset flow.
 * Generated when user requests "forgot password", valid for 30 minutes.
 */
@Entity
@Table(name = "password_reset_tokens")
public class PasswordResetToken {

    private static final int EXPIRATION_MINUTES = 30;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String token;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private LocalDateTime expiresAt;
    private boolean used = false;
    private LocalDateTime createdAt;

    public PasswordResetToken() {
        this.token = UUID.randomUUID().toString();
        this.createdAt = LocalDateTime.now();
        this.expiresAt = LocalDateTime.now().plusMinutes(EXPIRATION_MINUTES);
    }

    public PasswordResetToken(User user) {
        this();
        this.user = user;
    }

    /**
     * Check if this token is still valid (not expired and not used).
     */
    public boolean isValid() {
        return !used && LocalDateTime.now().isBefore(expiresAt);
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }

    public boolean isUsed() { return used; }
    public void setUsed(boolean used) { this.used = used; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
