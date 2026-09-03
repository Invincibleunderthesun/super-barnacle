package com.harsh.firstApp.service;

import com.harsh.firstApp.dto.*;
import com.harsh.firstApp.exception.ApiException;
import com.harsh.firstApp.model.PasswordResetToken;
import com.harsh.firstApp.model.User;
import com.harsh.firstApp.repository.PasswordResetTokenRepository;
import com.harsh.firstApp.repository.UserRepository;
import com.harsh.firstApp.security.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final PasswordResetTokenRepository tokenRepository;
    private final EmailService emailService;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder,
                       JwtUtil jwtUtil, PasswordResetTokenRepository tokenRepository,
                       EmailService emailService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.tokenRepository = tokenRepository;
        this.emailService = emailService;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new ApiException("Email already registered", HttpStatus.CONFLICT, "EMAIL_EXISTS");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole("ROLE_USER");
        User savedUser = userRepository.save(user);

        String token = jwtUtil.generateToken(savedUser.getEmail(), savedUser.getRole());
        logger.info("New user registered: {}", savedUser.getEmail());

        return AuthResponse.builder()
                .token(token)
                .expiresIn(jwtUtil.getExpirationTime())
                .userId(savedUser.getId())
                .username(savedUser.getUsername())
                .email(savedUser.getEmail())
                .role(savedUser.getRole())
                .build();
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ApiException("Invalid email or password",
                        HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new ApiException("Invalid email or password",
                    HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS");
        }

        String token = jwtUtil.generateToken(user.getEmail(), user.getRole());
        logger.info("User logged in: {}", user.getEmail());

        return AuthResponse.builder()
                .token(token)
                .expiresIn(jwtUtil.getExpirationTime())
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }

    public AuthResponse refreshToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new ApiException("Invalid token", HttpStatus.UNAUTHORIZED, "INVALID_TOKEN");
        }

        String token = authHeader.substring(7);
        String email = jwtUtil.validateToken(token);
        if (email == null) {
            throw new ApiException("Token expired or invalid", HttpStatus.UNAUTHORIZED, "TOKEN_EXPIRED");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ApiException("User not found", HttpStatus.NOT_FOUND, "USER_NOT_FOUND"));

        String newToken = jwtUtil.generateToken(user.getEmail(), user.getRole());

        return AuthResponse.builder()
                .token(newToken)
                .expiresIn(jwtUtil.getExpirationTime())
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }

    public void forgotPassword(ForgotPasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail()).orElse(null);
        if (user != null) {
            PasswordResetToken resetToken = new PasswordResetToken(user);
            tokenRepository.save(resetToken);
            emailService.sendPasswordResetEmail(user.getEmail(), resetToken.getToken());
            logger.info("Password reset token generated for: {}", user.getEmail());
        } else {
            logger.info("Password reset requested for non-existent email: {}", request.getEmail());
        }
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        PasswordResetToken resetToken = tokenRepository.findByToken(request.getToken())
                .orElseThrow(() -> new ApiException("Invalid or expired reset token",
                        HttpStatus.BAD_REQUEST, "INVALID_TOKEN"));

        if (!resetToken.isValid()) {
            throw new ApiException("Reset token has expired. Please request a new one.",
                    HttpStatus.BAD_REQUEST, "TOKEN_EXPIRED");
        }

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        resetToken.setUsed(true);
        tokenRepository.save(resetToken);
        logger.info("Password reset successful for user: {}", user.getEmail());
    }
}
