package com.harsh.firstApp.config;

import com.harsh.firstApp.model.Product;
import com.harsh.firstApp.model.User;
import com.harsh.firstApp.repository.ProductRepository;
import com.harsh.firstApp.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Data seeder for development environment ONLY.
 * This class is NOT active in production.
 */
@Configuration
@Profile("dev")
public class DataLoader {

    private static final Logger logger = LoggerFactory.getLogger(DataLoader.class);

    @Bean
    CommandLineRunner initDatabase(UserRepository userRepository,
            ProductRepository productRepository,
            PasswordEncoder passwordEncoder) {
        return args -> {
            // Seed admin user
            if (userRepository.findByEmail("admin@example.com").isEmpty()) {
                User admin = new User();
                admin.setUsername("admin");
                admin.setEmail("admin@example.com");
                admin.setPassword(passwordEncoder.encode("adminpass"));
                admin.setRole("ROLE_ADMIN");
                userRepository.save(admin);
                logger.info("✅ Admin user created: admin@example.com / adminpass");
            }

            // Seed sample products
            if (productRepository.count() == 0) {
                // Electronics
                productRepository.save(new Product("MacBook Pro 14\"",
                        "Apple M3 Pro chip, 18GB RAM, 512GB SSD", 199999.0, 15, "Electronics"));
                productRepository.save(new Product("iPhone 15 Pro",
                        "256GB, Titanium, A17 Pro chip", 129900.0, 25, "Electronics"));
                productRepository.save(new Product("Samsung Galaxy S24 Ultra",
                        "256GB, AI Features, S Pen included", 124999.0, 20, "Electronics"));
                productRepository.save(new Product("Sony WH-1000XM5",
                        "Premium Noise Cancelling Headphones", 29990.0, 30, "Electronics"));
                productRepository.save(new Product("iPad Air",
                        "M1 chip, 64GB, 10.9-inch display", 59900.0, 18, "Electronics"));

                // Computers
                productRepository.save(new Product("Dell XPS 15",
                        "Intel i7, 16GB RAM, 512GB SSD", 145000.0, 10, "Computers"));
                productRepository.save(new Product("ASUS ROG Gaming Laptop",
                        "RTX 4070, AMD Ryzen 9, 32GB RAM", 175000.0, 8, "Computers"));
                productRepository.save(new Product("Mechanical Keyboard RGB",
                        "Cherry MX switches, customizable RGB", 8999.0, 50, "Computers"));
                productRepository.save(new Product("Logitech MX Master 3S",
                        "Wireless mouse, 8K DPI sensor", 9995.0, 40, "Computers"));

                // Accessories
                productRepository.save(new Product("Apple Watch Series 9",
                        "GPS + Cellular, 45mm, Always-on display", 49900.0, 22, "Accessories"));
                productRepository.save(new Product("AirPods Pro 2",
                        "Active Noise Cancellation, USB-C", 24900.0, 35, "Accessories"));
                productRepository.save(new Product("Samsung Galaxy Watch 6",
                        "44mm, Sleep tracking, Body composition", 32999.0, 28, "Accessories"));

                // Home & Office
                productRepository.save(new Product("LG 27\" 4K Monitor",
                        "IPS Panel, USB-C, HDR10", 35000.0, 15, "Home & Office"));
                productRepository.save(new Product("Ergonomic Office Chair",
                        "Lumbar support, adjustable armrests", 25999.0, 12, "Home & Office"));
                productRepository.save(new Product("Standing Desk Electric",
                        "Height adjustable, memory presets", 45000.0, 8, "Home & Office"));

                logger.info("✅ {} sample products seeded into database", productRepository.count());
            }
        };
    }
}