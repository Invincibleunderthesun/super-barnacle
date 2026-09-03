package com.harsh.firstApp.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import java.time.LocalDateTime;

/**
 * Coupon/discount code for orders.
 * Supports percentage and fixed-amount discounts with usage limits and expiry.
 */
@Entity
@Table(name = "coupons")
public class Coupon {

    public enum DiscountType {
        PERCENTAGE,
        FIXED_AMOUNT
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Coupon code is required")
    @Column(unique = true, nullable = false)
    private String code;

    private String description;

    @Enumerated(EnumType.STRING)
    private DiscountType discountType = DiscountType.PERCENTAGE;

    @Positive(message = "Discount value must be positive")
    private double discountValue;

    private double minOrderAmount = 0;   // Minimum order subtotal to apply
    private double maxDiscount = 0;      // Max discount cap (for percentage type, 0 = no cap)

    private int maxUses = 0;             // 0 = unlimited
    private int currentUses = 0;

    private boolean active = true;

    private LocalDateTime startsAt;
    private LocalDateTime expiresAt;
    private LocalDateTime createdAt;

    public Coupon() {
        this.createdAt = LocalDateTime.now();
    }

    /**
     * Check if this coupon is currently valid for use.
     */
    public boolean isValid() {
        if (!active) return false;
        if (maxUses > 0 && currentUses >= maxUses) return false;

        LocalDateTime now = LocalDateTime.now();
        if (startsAt != null && now.isBefore(startsAt)) return false;
        if (expiresAt != null && now.isAfter(expiresAt)) return false;

        return true;
    }

    /**
     * Calculate discount amount for a given subtotal.
     */
    public double calculateDiscount(double subtotal) {
        if (subtotal < minOrderAmount) return 0;

        double discount;
        if (discountType == DiscountType.PERCENTAGE) {
            discount = subtotal * (discountValue / 100.0);
            if (maxDiscount > 0) {
                discount = Math.min(discount, maxDiscount);
            }
        } else {
            discount = Math.min(discountValue, subtotal); // Don't exceed subtotal
        }

        return Math.round(discount * 100.0) / 100.0;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code != null ? code.toUpperCase().trim() : null; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public DiscountType getDiscountType() { return discountType; }
    public void setDiscountType(DiscountType discountType) { this.discountType = discountType; }

    public double getDiscountValue() { return discountValue; }
    public void setDiscountValue(double discountValue) { this.discountValue = discountValue; }

    public double getMinOrderAmount() { return minOrderAmount; }
    public void setMinOrderAmount(double minOrderAmount) { this.minOrderAmount = minOrderAmount; }

    public double getMaxDiscount() { return maxDiscount; }
    public void setMaxDiscount(double maxDiscount) { this.maxDiscount = maxDiscount; }

    public int getMaxUses() { return maxUses; }
    public void setMaxUses(int maxUses) { this.maxUses = maxUses; }

    public int getCurrentUses() { return currentUses; }
    public void setCurrentUses(int currentUses) { this.currentUses = currentUses; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public LocalDateTime getStartsAt() { return startsAt; }
    public void setStartsAt(LocalDateTime startsAt) { this.startsAt = startsAt; }

    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
