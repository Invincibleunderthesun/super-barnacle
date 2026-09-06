package com.harsh.uday.controller;

import com.harsh.uday.dto.ApiResponse;
import com.harsh.uday.exception.ApiException;
import com.harsh.uday.model.Coupon;
import com.harsh.uday.repository.CouponRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/coupons")
@Tag(name = "Coupons", description = "Coupon management APIs")
@SecurityRequirement(name = "bearerAuth")
public class CouponController {

    private final CouponRepository couponRepository;

    public CouponController(CouponRepository couponRepository) {
        this.couponRepository = couponRepository;
    }

    @Operation(summary = "Get all coupons (Admin)")
    @GetMapping
    public ResponseEntity<ApiResponse<List<Coupon>>> getAllCoupons() {
        return ResponseEntity.ok(ApiResponse.success("Coupons retrieved", couponRepository.findAll()));
    }

    @Operation(summary = "Get active coupons")
    @GetMapping("/active")
    public ResponseEntity<ApiResponse<List<Coupon>>> getActiveCoupons() {
        return ResponseEntity.ok(ApiResponse.success("Active coupons", couponRepository.findByActiveTrue()));
    }

    @Operation(summary = "Create a new coupon (Admin)")
    @PostMapping
    public ResponseEntity<ApiResponse<Coupon>> createCoupon(@Valid @RequestBody Coupon coupon) {
        if (couponRepository.findByCode(coupon.getCode()).isPresent()) {
            throw new ApiException("Coupon code already exists", HttpStatus.CONFLICT);
        }
        Coupon saved = couponRepository.save(coupon);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Coupon created", saved));
    }

    @Operation(summary = "Update a coupon (Admin)")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Coupon>> updateCoupon(
            @PathVariable Long id, @Valid @RequestBody Coupon updated) {
        Coupon coupon = couponRepository.findById(id)
                .orElseThrow(() -> new ApiException("Coupon not found", HttpStatus.NOT_FOUND));

        coupon.setCode(updated.getCode());
        coupon.setDescription(updated.getDescription());
        coupon.setDiscountType(updated.getDiscountType());
        coupon.setDiscountValue(updated.getDiscountValue());
        coupon.setMinOrderAmount(updated.getMinOrderAmount());
        coupon.setMaxDiscount(updated.getMaxDiscount());
        coupon.setMaxUses(updated.getMaxUses());
        coupon.setActive(updated.isActive());
        coupon.setStartsAt(updated.getStartsAt());
        coupon.setExpiresAt(updated.getExpiresAt());

        return ResponseEntity.ok(ApiResponse.success("Coupon updated", couponRepository.save(coupon)));
    }

    @Operation(summary = "Delete a coupon (Admin)")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCoupon(@PathVariable Long id) {
        if (!couponRepository.existsById(id)) {
            throw new ApiException("Coupon not found", HttpStatus.NOT_FOUND);
        }
        couponRepository.deleteById(id);
        return ResponseEntity.ok(ApiResponse.success("Coupon deleted", null));
    }

    @Operation(summary = "Validate a coupon code and preview discount")
    @PostMapping("/validate")
    public ResponseEntity<ApiResponse<Map<String, Object>>> validateCoupon(
            @RequestParam String code,
            @RequestParam double subtotal) {
        Coupon coupon = couponRepository.findByCode(code.toUpperCase().trim())
                .orElseThrow(() -> new ApiException("Invalid coupon code", HttpStatus.NOT_FOUND));

        if (!coupon.isValid()) {
            throw new ApiException("This coupon has expired or reached its usage limit", HttpStatus.BAD_REQUEST);
        }

        if (subtotal < coupon.getMinOrderAmount()) {
            throw new ApiException(
                    String.format("Minimum order amount for this coupon is ₹%.2f", coupon.getMinOrderAmount()),
                    HttpStatus.BAD_REQUEST);
        }

        double discount = coupon.calculateDiscount(subtotal);

        Map<String, Object> result = Map.of(
                "code", coupon.getCode(),
                "description", coupon.getDescription() != null ? coupon.getDescription() : "",
                "discountType", coupon.getDiscountType(),
                "discountAmount", discount,
                "subtotalAfterDiscount", subtotal - discount
        );

        return ResponseEntity.ok(ApiResponse.success("Coupon is valid", result));
    }
}
