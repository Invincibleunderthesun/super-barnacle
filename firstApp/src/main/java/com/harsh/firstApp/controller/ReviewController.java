package com.harsh.firstApp.controller;

import com.harsh.firstApp.dto.ApiResponse;
import com.harsh.firstApp.exception.ApiException;
import com.harsh.firstApp.model.*;
import com.harsh.firstApp.repository.OrderRepository;
import com.harsh.firstApp.repository.ProductRepository;
import com.harsh.firstApp.repository.ReviewRepository;
import com.harsh.firstApp.repository.UserRepository;
import com.harsh.firstApp.security.JwtFilter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/reviews")
@Tag(name = "Reviews", description = "Product review and rating APIs")
public class ReviewController {

    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final JwtFilter jwtFilter;

    public ReviewController(ReviewRepository reviewRepository,
            ProductRepository productRepository,
            UserRepository userRepository,
            OrderRepository orderRepository,
            JwtFilter jwtFilter) {
        this.reviewRepository = reviewRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.orderRepository = orderRepository;
        this.jwtFilter = jwtFilter;
    }

    @Operation(summary = "Get reviews for a product (public)")
    @GetMapping("/product/{productId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getProductReviews(
            @PathVariable Long productId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ApiException("Product not found", HttpStatus.NOT_FOUND));

        Page<Review> reviews = reviewRepository.findByProduct(product,
                PageRequest.of(page, size, Sort.by("createdAt").descending()));

        Double avgRating = reviewRepository.getAverageRatingByProduct(product);
        long reviewCount = reviewRepository.countByProduct(product);

        Map<String, Object> result = new HashMap<>();
        result.put("reviews", reviews);
        result.put("averageRating", avgRating != null ? Math.round(avgRating * 10.0) / 10.0 : 0.0);
        result.put("totalReviews", reviewCount);

        return ResponseEntity.ok(ApiResponse.success("Reviews retrieved", result));
    }

    @Operation(summary = "Add a review for a product (authenticated)")
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/product/{productId}")
    public ResponseEntity<ApiResponse<Review>> addReview(
            @PathVariable Long productId,
            @Valid @RequestBody Review reviewRequest) {

        Long userId = jwtFilter.getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException("User not found", HttpStatus.NOT_FOUND));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ApiException("Product not found", HttpStatus.NOT_FOUND));

        // One review per user per product
        if (reviewRepository.existsByUserAndProduct(user, product)) {
            throw new ApiException("You have already reviewed this product", HttpStatus.CONFLICT);
        }

        Review review = new Review(user, product, reviewRequest.getRating(),
                reviewRequest.getTitle(), reviewRequest.getComment());

        // Check if user has purchased this product (verified purchase)
        List<Order> userOrders = orderRepository.findByUser(user);
        boolean hasPurchased = userOrders.stream()
                .filter(o -> o.getStatus() == Order.Status.DELIVERED || o.getStatus() == Order.Status.PAID)
                .flatMap(o -> o.getItems().stream())
                .anyMatch(item -> item.getProduct() != null && item.getProduct().getId().equals(productId));
        review.setVerified(hasPurchased);

        Review saved = reviewRepository.save(review);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Review added", saved));
    }

    @Operation(summary = "Update your review (authenticated)")
    @SecurityRequirement(name = "bearerAuth")
    @PutMapping("/{reviewId}")
    public ResponseEntity<ApiResponse<Review>> updateReview(
            @PathVariable Long reviewId,
            @Valid @RequestBody Review reviewRequest) {

        Long userId = jwtFilter.getCurrentUserId();
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ApiException("Review not found", HttpStatus.NOT_FOUND));

        if (!review.getUser().getId().equals(userId) && !JwtFilter.isAdmin()) {
            throw new ApiException("You can only update your own reviews", HttpStatus.FORBIDDEN);
        }

        review.setRating(reviewRequest.getRating());
        review.setTitle(reviewRequest.getTitle());
        review.setComment(reviewRequest.getComment());

        return ResponseEntity.ok(ApiResponse.success("Review updated", reviewRepository.save(review)));
    }

    @Operation(summary = "Delete a review (owner or admin)")
    @SecurityRequirement(name = "bearerAuth")
    @DeleteMapping("/{reviewId}")
    public ResponseEntity<ApiResponse<Void>> deleteReview(@PathVariable Long reviewId) {
        Long userId = jwtFilter.getCurrentUserId();
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ApiException("Review not found", HttpStatus.NOT_FOUND));

        if (!review.getUser().getId().equals(userId) && !JwtFilter.isAdmin()) {
            throw new ApiException("You can only delete your own reviews", HttpStatus.FORBIDDEN);
        }

        reviewRepository.delete(review);
        return ResponseEntity.ok(ApiResponse.success("Review deleted", null));
    }
}
