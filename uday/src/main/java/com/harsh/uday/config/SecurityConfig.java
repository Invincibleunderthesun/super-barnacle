package com.harsh.uday.config;

import com.harsh.uday.security.JwtFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtFilter jwtFilter;
    private final Environment environment;

    @Value("${cors.allowed-origins:http://localhost:3000,http://localhost:5173}")
    private String allowedOrigins;

    public SecurityConfig(JwtFilter jwtFilter, Environment environment) {
        this.jwtFilter = jwtFilter;
        this.environment = environment;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        boolean isDevProfile = Arrays.asList(environment.getActiveProfiles()).contains("dev");

        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> {
                    // Public endpoints
                    auth.requestMatchers("/api/v1/auth/**").permitAll()
                        .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                        .requestMatchers("/").permitAll();

                    // Payment webhook — no auth (Razorpay calls this)
                    auth.requestMatchers("/api/v1/payments/webhook").permitAll();

                    // Seller registration is public
                    auth.requestMatchers(HttpMethod.POST, "/api/v1/sellers/register").permitAll();
                    // Public seller profiles and product lists
                    auth.requestMatchers(HttpMethod.GET, "/api/v1/sellers/{id}").permitAll();
                    auth.requestMatchers(HttpMethod.GET, "/api/v1/sellers/{id}/products").permitAll();

                    // Swagger/OpenAPI — only permit in dev
                    if (isDevProfile) {
                        auth.requestMatchers("/swagger-ui/**", "/api-docs/**", "/swagger-ui.html").permitAll()
                            .requestMatchers("/h2-console/**").permitAll();
                    }

                    // Product endpoints - GET is public, POST/PUT/DELETE for ADMIN or SELLER
                    auth.requestMatchers(HttpMethod.GET, "/api/v1/products/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/products/**").hasAnyRole("ADMIN", "SELLER")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/products/**").hasAnyRole("ADMIN", "SELLER")
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/products/**").hasAnyRole("ADMIN", "SELLER")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/products/**").hasAnyRole("ADMIN", "SELLER")

                        // Order endpoints
                        .requestMatchers("/api/v1/orders/stats").hasRole("ADMIN")
                        .requestMatchers("/api/v1/orders/*/cancel/admin").hasRole("ADMIN")
                        .requestMatchers("/api/v1/orders/*/history/admin").hasRole("ADMIN")
                        .requestMatchers("/api/v1/orders/**").hasAnyRole("USER", "ADMIN", "SELLER")

                        // Cart endpoints
                        .requestMatchers("/api/v1/cart/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/v1/cart/**").hasAnyRole("USER", "ADMIN")

                        // Address endpoints
                        .requestMatchers("/api/v1/addresses/**").hasAnyRole("USER", "ADMIN")

                        // Payment endpoints (authenticated except webhook above)
                        .requestMatchers("/api/v1/payments/refund/**").hasRole("ADMIN")
                        .requestMatchers("/api/v1/payments/**").hasAnyRole("USER", "ADMIN")

                        // Seller own endpoints (my profile, my products, my stats)
                        .requestMatchers("/api/v1/sellers/me/**").hasRole("SELLER")
                        .requestMatchers("/api/v1/sellers/me").hasRole("SELLER")

                        // Seller admin endpoints (list all, verify, commission)
                        .requestMatchers(HttpMethod.GET, "/api/v1/sellers").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/sellers/*/verify").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/sellers/*/commission").hasRole("ADMIN")

                        // User management
                        .requestMatchers(HttpMethod.GET, "/api/v1/users").hasRole("ADMIN")
                        .requestMatchers("/api/v1/users/*/role").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/users/**").hasRole("ADMIN")
                        .requestMatchers("/api/v1/users/**").hasAnyRole("USER", "ADMIN", "SELLER")

                        .anyRequest().authenticated();

                })
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        if (isDevProfile) {
            http.headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()));
        }

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        List<String> origins = Arrays.asList(allowedOrigins.split(","));
        configuration.setAllowedOriginPatterns(origins);

        configuration.setAllowedMethods(Arrays.asList(
                "GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));

        configuration.setAllowedHeaders(Arrays.asList(
                "Authorization",
                "Content-Type",
                "Accept",
                "Origin",
                "X-Requested-With"));

        configuration.setExposedHeaders(Arrays.asList(
                "Authorization",
                "Content-Disposition"));

        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}