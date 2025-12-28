package com.harsh.firstApp.controller;

import com.harsh.firstApp.model.User;
import com.harsh.firstApp.repository.UserRepository;
import com.harsh.firstApp.security.JwtUtil;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AuthController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // Register new user
    @PostMapping("/register")
    public User register(@RequestBody User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    // Login returns JWT token
    @PostMapping("/login")
    public String login(@RequestBody User user) {
        User dbUser = userRepository.findByEmail(user.getEmail()).orElse(null);

        if (dbUser != null && passwordEncoder.matches(user.getPassword(), dbUser.getPassword())) {
            // Generate JWT token
            return JwtUtil.generateToken(dbUser.getEmail());
        }
        return "Invalid credentials";
    }

}