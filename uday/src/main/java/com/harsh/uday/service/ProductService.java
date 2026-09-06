package com.harsh.uday.service;

import com.harsh.uday.model.Product;
import com.harsh.uday.repository.ProductRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @CacheEvict(value = "products", allEntries = true)
    public Product addProduct(Product product) {
        return productRepository.save(product);
    }

    @Cacheable(value = "products", key = "#pageable.pageNumber + '-' + #pageable.pageSize")
    public Page<Product> getAllProducts(Pageable pageable) {
        return productRepository.findAll(pageable);
    }

    @Cacheable(value = "products-all")
    public List<Product> getAllProductsList() {
        return productRepository.findAll();
    }

    @Cacheable(value = "product", key = "#id")
    public Product getProductById(Long id) {
        return productRepository.findById(id).orElse(null);
    }

    @CacheEvict(value = { "products", "products-all", "product" }, allEntries = true)
    public Product updateProduct(Long id, Product updatedProduct) {
        Product product = productRepository.findById(id).orElse(null);
        if (product == null)
            return null;

        product.setName(updatedProduct.getName());
        product.setPrice(updatedProduct.getPrice());
        product.setStock(updatedProduct.getStock());
        if (updatedProduct.getDescription() != null) {
            product.setDescription(updatedProduct.getDescription());
        }
        if (updatedProduct.getCategory() != null) {
            product.setCategory(updatedProduct.getCategory());
        }
        if (updatedProduct.getImageUrl() != null) {
            product.setImageUrl(updatedProduct.getImageUrl());
        }
        return productRepository.save(product);
    }

    @CacheEvict(value = { "products", "products-all", "product" }, allEntries = true)
    public void deleteProduct(Long id) {
        productRepository.deleteById(id);
    }

    @CacheEvict(value = { "products", "products-all", "product" }, allEntries = true)
    public Product updateStock(Long id, int quantity) {
        Product product = productRepository.findById(id).orElse(null);
        if (product == null)
            return null;
        product.setStock(quantity);
        return productRepository.save(product);
    }

    /**
     * Search products with pagination
     */
    public Page<Product> searchProducts(String query, Pageable pageable) {
        return productRepository.searchProducts(query, pageable);
    }

    /**
     * Filter products by price range with pagination
     */
    public Page<Product> filterByPrice(Double minPrice, Double maxPrice, Pageable pageable) {
        if (minPrice == null)
            minPrice = 0.0;
        if (maxPrice == null)
            maxPrice = Double.MAX_VALUE;
        return productRepository.findByPriceBetween(minPrice, maxPrice, pageable);
    }

    public List<Product> getLowStockProducts(int threshold) {
        return productRepository.findByStockLessThanEqual(threshold);
    }

    public List<Product> getProductsByCategory(String category) {
        return productRepository.findByCategory(category);
    }
}