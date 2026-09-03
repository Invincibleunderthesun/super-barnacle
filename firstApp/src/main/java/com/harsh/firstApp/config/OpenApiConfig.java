package com.harsh.firstApp.config;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI/Swagger configuration for API documentation.
 */
@Configuration
@SecurityScheme(name = "bearerAuth", type = SecuritySchemeType.HTTP, scheme = "bearer", bearerFormat = "JWT", description = "Enter your JWT token")
public class OpenApiConfig {

    @Value("${spring.profiles.active:dev}")
    private String activeProfile;

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("E-Commerce API")
                        .version("1.0.0")
                        .description("""
                                A production-ready E-Commerce REST API built with Spring Boot.

                                ## Features
                                - User authentication with JWT
                                - Role-based access control (ADMIN/USER)
                                - Product management with categories and stock tracking
                                - Shopping cart with history
                                - Order management with status tracking
                                - Pagination and filtering

                                ## Authentication
                                1. Register a new user at `/api/v1/auth/register`
                                2. Login at `/api/v1/auth/login` to get a JWT token
                                3. Click 'Authorize' button and enter your token
                                4. All authenticated endpoints will now work
                                """)
                        .contact(new Contact()
                                .name("API Support")
                                .email("support@example.com"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(getServers());
    }

    private List<Server> getServers() {
        Server local = new Server()
                .url("http://localhost:8080")
                .description("Local Development Server");

        Server railway = new Server()
                .url("https://your-app.railway.app")
                .description("Railway Production Server");

        if ("prod".equals(activeProfile)) {
            return List.of(railway, local);
        }
        return List.of(local, railway);
    }
}
