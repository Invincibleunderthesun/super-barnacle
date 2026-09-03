package com.harsh.firstApp.dto;

/**
 * Safe response DTO for product data including seller info.
 */
public class ProductDTO {
    private Long id;
    private String name;
    private String description;
    private double price;
    private int stock;
    private String category;
    private String imageUrl;
    private boolean active;
    private Long sellerId;
    private String sellerName;

    public ProductDTO() {}

    public static ProductDTO from(com.harsh.firstApp.model.Product product) {
        ProductDTO dto = new ProductDTO();
        dto.id = product.getId();
        dto.name = product.getName();
        dto.description = product.getDescription();
        dto.price = product.getPrice();
        dto.stock = product.getStock();
        dto.category = product.getCategory();
        dto.imageUrl = product.getImageUrl();
        dto.active = product.isActive();
        if (product.getSeller() != null) {
            dto.sellerId = product.getSeller().getId();
            dto.sellerName = product.getSeller().getStoreName();
        }
        return dto;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public Long getSellerId() { return sellerId; }
    public void setSellerId(Long sellerId) { this.sellerId = sellerId; }
    public String getSellerName() { return sellerName; }
    public void setSellerName(String sellerName) { this.sellerName = sellerName; }
}
