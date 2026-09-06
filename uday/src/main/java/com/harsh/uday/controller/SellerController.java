package com.harsh.uday.controller;

import com.harsh.uday.dto.ApiResponse;
import com.harsh.uday.dto.SellerDTO;
import com.harsh.uday.dto.SellerRegisterRequest;
import com.harsh.uday.dto.AuthResponse;
import com.harsh.uday.model.Product;
import com.harsh.uday.model.Seller;
import com.harsh.uday.security.JwtFilter;
import com.harsh.uday.service.SellerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/sellers")
@Tag(name = "Sellers", description = "Seller registration, profile, and product management APIs")
public class SellerController {

    private final SellerService sellerService;

    public SellerController(SellerService sellerService) {
        this.sellerService = sellerService;
    }

    // ======================== PUBLIC ENDPOINTS ========================

    @Operation(summary = "Register as a new seller")
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> registerSeller(
            @Valid @RequestBody SellerRegisterRequest request) {
        AuthResponse auth = sellerService.registerSeller(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Seller registration successful", auth));
    }

    @Operation(summary = "Get public seller profile")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SellerDTO>> getSellerProfile(@PathVariable Long id) {
        SellerDTO seller = sellerService.getSellerPublicProfile(id);
        return ResponseEntity.ok(ApiResponse.success("Seller profile retrieved", seller));
    }

    @Operation(summary = "Get seller's products (public)")
    @GetMapping("/{id}/products")
    public ResponseEntity<ApiResponse<Page<Product>>> getSellerProducts(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        // For public access, we redirect to a paginated product search by seller ID
        Pageable pageable = PageRequest.of(page, Math.min(size, 100));
        // Use seller email from profile lookup
        SellerDTO seller = sellerService.getSellerPublicProfile(id);
        Page<Product> products = sellerService.getSellerProducts(seller.getEmail(), pageable);
        return ResponseEntity.ok(ApiResponse.success("Seller products retrieved", products));
    }

    // ======================== SELLER OWN ENDPOINTS ========================

    @Operation(summary = "Get own seller profile")
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<SellerDTO>> getOwnProfile() {
        String email = JwtFilter.getCurrentUserEmail();
        Seller seller = sellerService.getSellerByEmail(email);
        SellerDTO dto = sellerService.mapToDTO(seller);
        return ResponseEntity.ok(ApiResponse.success("Seller profile", dto));
    }

    @Operation(summary = "Update own seller store info")
    @SecurityRequirement(name = "bearerAuth")
    @PutMapping("/me")
    public ResponseEntity<ApiResponse<SellerDTO>> updateOwnProfile(
            @RequestBody Map<String, String> updates) {
        String email = JwtFilter.getCurrentUserEmail();
        Seller updated = sellerService.updateSellerProfile(
                email,
                updates.get("storeName"),
                updates.get("storeDescription"),
                updates.get("logoUrl"),
                updates.get("gstNumber"));
        return ResponseEntity.ok(ApiResponse.success("Profile updated", sellerService.mapToDTO(updated)));
    }

    @Operation(summary = "Get own products (paginated)")
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/me/products")
    public ResponseEntity<ApiResponse<Page<Product>>> getOwnProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        String email = JwtFilter.getCurrentUserEmail();
        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, Math.min(size, 100), sort);
        Page<Product> products = sellerService.getSellerProducts(email, pageable);
        return ResponseEntity.ok(ApiResponse.success("Your products", products));
    }

    @Operation(summary = "Add a new product to your store")
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/me/products")
    public ResponseEntity<ApiResponse<Product>> addProduct(
            @Valid @RequestBody Product product) {
        String email = JwtFilter.getCurrentUserEmail();
        Product saved = sellerService.addProductForSeller(email, product);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Product added to your store", saved));
    }

    @Operation(summary = "Update your product")
    @SecurityRequirement(name = "bearerAuth")
    @PutMapping("/me/products/{productId}")
    public ResponseEntity<ApiResponse<Product>> updateProduct(
            @PathVariable Long productId,
            @Valid @RequestBody Product product) {
        String email = JwtFilter.getCurrentUserEmail();
        Product updated = sellerService.updateSellerProduct(email, productId, product);
        return ResponseEntity.ok(ApiResponse.success("Product updated", updated));
    }

    @Operation(summary = "Delete your product")
    @SecurityRequirement(name = "bearerAuth")
    @DeleteMapping("/me/products/{productId}")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(@PathVariable Long productId) {
        String email = JwtFilter.getCurrentUserEmail();
        sellerService.deleteSellerProduct(email, productId);
        return ResponseEntity.ok(ApiResponse.success("Product deleted", null));
    }

    @Operation(summary = "Get your seller dashboard statistics")
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/me/stats")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getDashboard() {
        String email = JwtFilter.getCurrentUserEmail();
        Map<String, Object> stats = sellerService.getSellerDashboard(email);
        return ResponseEntity.ok(ApiResponse.success("Seller dashboard", stats));
    }

    // ======================== ADMIN ENDPOINTS ========================

    @Operation(summary = "Get all sellers (Admin only)")
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping
    public ResponseEntity<ApiResponse<Page<Seller>>> getAllSellers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, Math.min(size, 100));
        Page<Seller> sellers = sellerService.getAllSellers(pageable);
        return ResponseEntity.ok(ApiResponse.success("All sellers", sellers));
    }

    @Operation(summary = "Verify or reject a seller (Admin only)")
    @SecurityRequirement(name = "bearerAuth")
    @PutMapping("/{id}/verify")
    public ResponseEntity<ApiResponse<SellerDTO>> verifySeller(
            @PathVariable Long id,
            @RequestParam boolean verified) {
        Seller seller = sellerService.verifySeller(id, verified);
        return ResponseEntity.ok(ApiResponse.success(
                verified ? "Seller verified" : "Seller rejected",
                sellerService.mapToDTO(seller)));
    }

    @Operation(summary = "Set seller commission rate (Admin only)")
    @SecurityRequirement(name = "bearerAuth")
    @PutMapping("/{id}/commission")
    public ResponseEntity<ApiResponse<SellerDTO>> setCommission(
            @PathVariable Long id,
            @RequestParam double rate) {
        Seller seller = sellerService.setCommissionRate(id, rate);
        return ResponseEntity.ok(ApiResponse.success("Commission rate updated", sellerService.mapToDTO(seller)));
    }
}
