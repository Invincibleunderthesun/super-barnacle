package com.harsh.firstApp.controller;

import com.harsh.firstApp.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@Tag(name = "Health", description = "Health and status endpoints")
public class HealthController {

    @Value("${spring.application.name:firstApp}")
    private String appName;

    @Value("${spring.profiles.active:dev}")
    private String activeProfile;

    @Operation(summary = "Root endpoint - API info")
    @GetMapping("/")
    public ResponseEntity<ApiResponse<Map<String, Object>>> root() {
        Map<String, Object> info = new HashMap<>();
        info.put("name", "E-Commerce API");
        info.put("version", "1.0.0");
        info.put("description", "Production-ready E-Commerce REST API");
        info.put("documentation", "/swagger-ui.html");
        info.put("health", "/actuator/health");
        info.put("timestamp", LocalDateTime.now());

        return ResponseEntity.ok(ApiResponse.success("Welcome to the E-Commerce API", info));
    }

    @Operation(summary = "API status check")
    @GetMapping("/api/v1/status")
    public ResponseEntity<ApiResponse<Map<String, Object>>> status() {
        Map<String, Object> status = new HashMap<>();
        status.put("status", "UP");
        status.put("application", appName);
        status.put("profile", activeProfile);
        status.put("timestamp", LocalDateTime.now());
        status.put("java", System.getProperty("java.version"));

        return ResponseEntity.ok(ApiResponse.success("API is running", status));
    }
}
