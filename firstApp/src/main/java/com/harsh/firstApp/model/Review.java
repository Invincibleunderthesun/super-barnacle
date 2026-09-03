package com.harsh.firstApp.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;

/**
 * Product review with star rating, title, and comment.
 * One review per user per product.
 */
@Entity
@Table(name = "reviews", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "user_id", "product_id" })
})
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private User user;

    // Store username separately so it displays without loading User
    private String username;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    @JsonIgnore
    private Product product;

    @Min(value = 1, message = "Rating must be at least 1")
    @Max(value = 5, message = "Rating must be at most 5")
    private int rating;

    @NotBlank(message = "Review title is required")
    private String title;

    @Column(length = 2000)
    private String comment;

    private boolean verified = false;  // True if user purchased this product
    private LocalDateTime createdAt;

    public Review() {
        this.createdAt = LocalDateTime.now();
    }

    public Review(User user, Product product, int rating, String title, String comment) {
        this.user = user;
        this.username = user.getUsername();
        this.product = product;
        this.rating = rating;
        this.title = title;
        this.comment = comment;
        this.createdAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) {
        this.user = user;
        if (user != null) this.username = user.getUsername();
    }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }

    public int getRating() { return rating; }
    public void setRating(int rating) { this.rating = rating; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public boolean isVerified() { return verified; }
    public void setVerified(boolean verified) { this.verified = verified; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
