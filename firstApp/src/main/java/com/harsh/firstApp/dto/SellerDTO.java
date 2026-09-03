package com.harsh.firstApp.dto;

/**
 * Public-facing DTO for seller information.
 * Hides sensitive fields like bank details and internal IDs.
 */
public class SellerDTO {

    private Long id;
    private String storeName;
    private String storeDescription;
    private String logoUrl;
    private boolean verified;
    private String username;
    private String email;
    private long productCount;
    private double commissionRate;

    public SellerDTO() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getStoreName() { return storeName; }
    public void setStoreName(String storeName) { this.storeName = storeName; }

    public String getStoreDescription() { return storeDescription; }
    public void setStoreDescription(String storeDescription) { this.storeDescription = storeDescription; }

    public String getLogoUrl() { return logoUrl; }
    public void setLogoUrl(String logoUrl) { this.logoUrl = logoUrl; }

    public boolean isVerified() { return verified; }
    public void setVerified(boolean verified) { this.verified = verified; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public long getProductCount() { return productCount; }
    public void setProductCount(long productCount) { this.productCount = productCount; }

    public double getCommissionRate() { return commissionRate; }
    public void setCommissionRate(double commissionRate) { this.commissionRate = commissionRate; }
}
