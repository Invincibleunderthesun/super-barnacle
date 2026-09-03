package com.harsh.firstApp.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Authentication response containing JWT token and user information.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuthResponse {

    private String token;
    private String tokenType = "Bearer";
    private Long expiresIn;
    private Long userId;
    private String username;
    private String email;
    private String role;

    public AuthResponse() {
    }

    public AuthResponse(String token, Long expiresIn, Long userId, String username, String email, String role) {
        this.token = token;
        this.expiresIn = expiresIn;
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.role = role;
    }

    // Builder pattern for convenience
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final AuthResponse response = new AuthResponse();

        public Builder token(String token) {
            response.token = token;
            return this;
        }

        public Builder expiresIn(Long expiresIn) {
            response.expiresIn = expiresIn;
            return this;
        }

        public Builder userId(Long userId) {
            response.userId = userId;
            return this;
        }

        public Builder username(String username) {
            response.username = username;
            return this;
        }

        public Builder email(String email) {
            response.email = email;
            return this;
        }

        public Builder role(String role) {
            response.role = role;
            return this;
        }

        public AuthResponse build() {
            return response;
        }
    }

    // Getters and Setters
    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public Long getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(Long expiresIn) {
        this.expiresIn = expiresIn;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
