package com.harsh.firstApp.service;

import com.harsh.firstApp.dto.AuthResponse;
import com.harsh.firstApp.dto.SellerDTO;
import com.harsh.firstApp.dto.SellerRegisterRequest;
import com.harsh.firstApp.exception.ApiException;
import com.harsh.firstApp.model.Product;
import com.harsh.firstApp.model.Seller;
import com.harsh.firstApp.model.User;
import com.harsh.firstApp.repository.ProductRepository;
import com.harsh.firstApp.repository.SellerRepository;
import com.harsh.firstApp.repository.UserRepository;
import com.harsh.firstApp.security.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class SellerService {

    private static final Logger logger = LoggerFactory.getLogger(SellerService.class);

    private final SellerRepository sellerRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public SellerService(SellerRepository sellerRepository,
                         UserRepository userRepository,
                         ProductRepository productRepository,
                         PasswordEncoder passwordEncoder,
                         JwtUtil jwtUtil) {
        this.sellerRepository = sellerRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    /**
     * Register a new seller — creates both a User (ROLE_SELLER) and Seller profile.
     */
    public AuthResponse registerSeller(SellerRegisterRequest request) {
        // Check email uniqueness
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new ApiException("Email already registered", HttpStatus.CONFLICT, "EMAIL_EXISTS");
        }

        // Check store name uniqueness
        if (sellerRepository.findByStoreName(request.getStoreName()).isPresent()) {
            throw new ApiException("Store name already taken", HttpStatus.CONFLICT, "STORE_NAME_EXISTS");
        }

        // Create User with ROLE_SELLER
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole("ROLE_SELLER");
        User savedUser = userRepository.save(user);

        // Create Seller profile
        Seller seller = new Seller(savedUser, request.getStoreName());
        seller.setStoreDescription(request.getStoreDescription());
        seller.setGstNumber(request.getGstNumber());
        sellerRepository.save(seller);

        // Generate JWT
        String token = jwtUtil.generateToken(savedUser.getEmail(), savedUser.getRole());

        logger.info("New seller registered: {} (store: {})", savedUser.getEmail(), seller.getStoreName());

        return AuthResponse.builder()
                .token(token)
                .expiresIn(jwtUtil.getExpirationTime())
                .userId(savedUser.getId())
                .username(savedUser.getUsername())
                .email(savedUser.getEmail())
                .role(savedUser.getRole())
                .build();
    }

    /**
     * Get seller profile by user email (from JWT).
     */
    public Seller getSellerByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ApiException("User not found", HttpStatus.NOT_FOUND));
        return sellerRepository.findByUser(user)
                .orElseThrow(() -> new ApiException("Seller profile not found", HttpStatus.NOT_FOUND));
    }

    /**
     * Get public seller profile by ID.
     */
    public SellerDTO getSellerPublicProfile(Long sellerId) {
        Seller seller = sellerRepository.findById(sellerId)
                .orElseThrow(() -> new ApiException("Seller not found", HttpStatus.NOT_FOUND));
        return mapToDTO(seller);
    }

    /**
     * Update seller store info.
     */
    public Seller updateSellerProfile(String email, String storeName, String storeDescription, String logoUrl, String gstNumber) {
        Seller seller = getSellerByEmail(email);

        if (storeName != null && !storeName.isBlank()) {
            // Check uniqueness if name changed
            if (!storeName.equals(seller.getStoreName())) {
                sellerRepository.findByStoreName(storeName).ifPresent(existing -> {
                    throw new ApiException("Store name already taken", HttpStatus.CONFLICT, "STORE_NAME_EXISTS");
                });
            }
            seller.setStoreName(storeName);
        }
        if (storeDescription != null) seller.setStoreDescription(storeDescription);
        if (logoUrl != null) seller.setLogoUrl(logoUrl);
        if (gstNumber != null) seller.setGstNumber(gstNumber);
        seller.setUpdatedAt(LocalDateTime.now());

        return sellerRepository.save(seller);
    }

    /**
     * Add a product for this seller.
     */
    public Product addProductForSeller(String email, Product product) {
        Seller seller = getSellerByEmail(email);
        product.setSeller(seller);
        Product saved = productRepository.save(product);
        logger.info("Seller {} added product: {} (id={})", seller.getStoreName(), saved.getName(), saved.getId());
        return saved;
    }

    /**
     * Get all products belonging to this seller (paginated).
     */
    public Page<Product> getSellerProducts(String email, Pageable pageable) {
        Seller seller = getSellerByEmail(email);
        return productRepository.findBySellerId(seller.getId(), pageable);
    }

    /**
     * Update a product — verifies seller ownership.
     */
    public Product updateSellerProduct(String email, Long productId, Product updatedProduct) {
        Seller seller = getSellerByEmail(email);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ApiException("Product not found", HttpStatus.NOT_FOUND));

        if (product.getSeller() == null || !product.getSeller().getId().equals(seller.getId())) {
            throw new ApiException("You can only edit your own products", HttpStatus.FORBIDDEN, "ACCESS_DENIED");
        }

        product.setName(updatedProduct.getName());
        product.setPrice(updatedProduct.getPrice());
        product.setStock(updatedProduct.getStock());
        if (updatedProduct.getDescription() != null) product.setDescription(updatedProduct.getDescription());
        if (updatedProduct.getCategory() != null) product.setCategory(updatedProduct.getCategory());
        if (updatedProduct.getImageUrl() != null) product.setImageUrl(updatedProduct.getImageUrl());

        return productRepository.save(product);
    }

    /**
     * Delete a product — verifies seller ownership.
     */
    public void deleteSellerProduct(String email, Long productId) {
        Seller seller = getSellerByEmail(email);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ApiException("Product not found", HttpStatus.NOT_FOUND));

        if (product.getSeller() == null || !product.getSeller().getId().equals(seller.getId())) {
            throw new ApiException("You can only delete your own products", HttpStatus.FORBIDDEN, "ACCESS_DENIED");
        }

        productRepository.delete(product);
        logger.info("Seller {} deleted product: {}", seller.getStoreName(), product.getName());
    }

    /**
     * Get seller dashboard statistics.
     */
    public Map<String, Object> getSellerDashboard(String email) {
        Seller seller = getSellerByEmail(email);

        Map<String, Object> stats = new HashMap<>();
        stats.put("sellerId", seller.getId());
        stats.put("storeName", seller.getStoreName());
        stats.put("verified", seller.isVerified());
        stats.put("commissionRate", seller.getCommissionRate());
        stats.put("totalProducts", productRepository.countBySellerId(seller.getId()));

        return stats;
    }

    /**
     * Admin: get all sellers (paginated)
     */
    public Page<Seller> getAllSellers(Pageable pageable) {
        return sellerRepository.findAll(pageable);
    }

    /**
     * Admin: verify or reject a seller
     */
    public Seller verifySeller(Long sellerId, boolean verified) {
        Seller seller = sellerRepository.findById(sellerId)
                .orElseThrow(() -> new ApiException("Seller not found", HttpStatus.NOT_FOUND));
        seller.setVerified(verified);
        seller.setUpdatedAt(LocalDateTime.now());
        logger.info("Seller {} (id={}) verification set to: {}", seller.getStoreName(), sellerId, verified);
        return sellerRepository.save(seller);
    }

    /**
     * Admin: set commission rate for a seller
     */
    public Seller setCommissionRate(Long sellerId, double rate) {
        if (rate < 0 || rate > 100) {
            throw new ApiException("Commission rate must be between 0 and 100", HttpStatus.BAD_REQUEST);
        }
        Seller seller = sellerRepository.findById(sellerId)
                .orElseThrow(() -> new ApiException("Seller not found", HttpStatus.NOT_FOUND));
        seller.setCommissionRate(rate);
        seller.setUpdatedAt(LocalDateTime.now());
        logger.info("Seller {} commission rate set to {}%", seller.getStoreName(), rate);
        return sellerRepository.save(seller);
    }

    /**
     * Map Seller entity to public DTO.
     */
    public SellerDTO mapToDTO(Seller seller) {
        SellerDTO dto = new SellerDTO();
        dto.setId(seller.getId());
        dto.setStoreName(seller.getStoreName());
        dto.setStoreDescription(seller.getStoreDescription());
        dto.setLogoUrl(seller.getLogoUrl());
        dto.setVerified(seller.isVerified());
        dto.setProductCount(productRepository.countBySellerId(seller.getId()));
        if (seller.getUser() != null) {
            dto.setUsername(seller.getUser().getUsername());
            dto.setEmail(seller.getUser().getEmail());
        }
        dto.setCommissionRate(seller.getCommissionRate());
        return dto;
    }
}
