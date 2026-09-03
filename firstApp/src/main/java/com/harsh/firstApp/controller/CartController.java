package com.harsh.firstApp.controller;

import com.harsh.firstApp.dto.ApiResponse;
import com.harsh.firstApp.exception.ApiException;
import com.harsh.firstApp.model.Cart;
import com.harsh.firstApp.model.CartActionHistory;
import com.harsh.firstApp.security.JwtFilter;
import com.harsh.firstApp.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/cart")
@Tag(name = "Cart", description = "Shopping cart management APIs")
@SecurityRequirement(name = "bearerAuth")
public class CartController {

    private final CartService cartService;
    private final JwtFilter jwtFilter;

    public CartController(CartService cartService, JwtFilter jwtFilter) {
        this.cartService = cartService;
        this.jwtFilter = jwtFilter;
    }

    private void verifyOwnership(Long userId) {
        if (JwtFilter.isAdmin()) return;
        Long currentUserId = jwtFilter.getCurrentUserId();
        if (currentUserId == null || !currentUserId.equals(userId)) {
            throw new ApiException("Access denied: you can only access your own cart",
                    HttpStatus.FORBIDDEN, "ACCESS_DENIED");
        }
    }

    @Operation(summary = "Get cart by user ID")
    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<Cart>> getCartByUser(@PathVariable Long userId) {
        verifyOwnership(userId);
        Cart cart = cartService.getCartByUser(userId);
        if (cart == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(ApiResponse.success("Cart retrieved successfully", cart));
    }

    @Operation(summary = "Add product to cart with quantity")
    @PostMapping("/user/{userId}/add/{productId}")
    public ResponseEntity<ApiResponse<Cart>> addProductToCart(
            @PathVariable Long userId,
            @PathVariable Long productId,
            @RequestParam(defaultValue = "1") int quantity) {
        verifyOwnership(userId);
        Cart cart = cartService.addProductToCart(userId, productId, quantity);
        if (cart == null) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("User or product not found"));
        }
        return ResponseEntity.ok(ApiResponse.success("Product added to cart", cart));
    }

    @Operation(summary = "Update cart item quantity")
    @PatchMapping("/user/{userId}/item/{productId}")
    public ResponseEntity<ApiResponse<Cart>> updateCartItemQuantity(
            @PathVariable Long userId,
            @PathVariable Long productId,
            @RequestParam int quantity) {
        verifyOwnership(userId);
        Cart cart = cartService.updateCartItemQuantity(userId, productId, quantity);
        if (cart == null) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Cart or product not found"));
        }
        return ResponseEntity.ok(ApiResponse.success("Cart item updated", cart));
    }

    @Operation(summary = "Remove product from cart")
    @DeleteMapping("/user/{userId}/remove/{productId}")
    public ResponseEntity<ApiResponse<Cart>> removeProductFromCart(
            @PathVariable Long userId,
            @PathVariable Long productId) {
        verifyOwnership(userId);
        Cart cart = cartService.removeProductFromCart(userId, productId);
        if (cart == null) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Cart or product not found"));
        }
        return ResponseEntity.ok(ApiResponse.success("Product removed from cart", cart));
    }

    @Operation(summary = "Get cart action history")
    @GetMapping("/user/{userId}/history")
    public ResponseEntity<ApiResponse<List<CartActionHistory>>> getCartHistoryAsUser(
            @PathVariable Long userId) {
        verifyOwnership(userId);
        Long requesterId = jwtFilter.getCurrentUserId();
        List<CartActionHistory> history = cartService.getCartHistory(userId, false, requesterId);
        if (history == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(ApiResponse.success("Cart history retrieved", history));
    }

    @Operation(summary = "Get cart action history (admin)")
    @GetMapping("/admin/{userId}/history")
    public ResponseEntity<ApiResponse<List<CartActionHistory>>> getCartHistoryAsAdmin(
            @PathVariable Long userId) {
        List<CartActionHistory> history = cartService.getCartHistory(userId, true, null);
        if (history == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(ApiResponse.success("Cart history retrieved", history));
    }

    @Operation(summary = "Clear cart")
    @DeleteMapping("/user/{userId}/clear")
    public ResponseEntity<ApiResponse<Void>> clearCart(@PathVariable Long userId) {
        verifyOwnership(userId);
        boolean cleared = cartService.clearCart(userId);
        if (!cleared) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(ApiResponse.success("Cart cleared successfully", null));
    }
}