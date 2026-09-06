package com.harsh.uday.controller;

import com.harsh.uday.dto.ApiResponse;
import com.harsh.uday.exception.ResourceNotFoundException;
import com.harsh.uday.model.Product;
import com.harsh.uday.service.ProductService;
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

import java.util.List;

@RestController
@RequestMapping("/api/v1/products")
@Tag(name = "Products", description = "Product management APIs")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @Operation(summary = "Get all products with pagination")
    @GetMapping
    public ResponseEntity<ApiResponse<Page<Product>>> getAllProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {

        // Cap page size to prevent abuse
        size = Math.min(size, 100);

        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Product> products = productService.getAllProducts(pageable);

        return ResponseEntity.ok(ApiResponse.success("Products retrieved", products));
    }

    @Operation(summary = "Get all products (no pagination)")
    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<Product>>> getAllProductsList() {
        List<Product> products = productService.getAllProductsList();
        return ResponseEntity.ok(ApiResponse.success("Products retrieved", products));
    }

    @Operation(summary = "Get product by ID")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Product>> getProductById(@PathVariable Long id) {
        Product product = productService.getProductById(id);
        if (product == null) {
            throw new ResourceNotFoundException("Product", id);
        }
        return ResponseEntity.ok(ApiResponse.success("Product retrieved", product));
    }

    @Operation(summary = "Search products by name (paginated)")
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Page<Product>>> searchProducts(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        size = Math.min(size, 100);
        Pageable pageable = PageRequest.of(page, size);
        Page<Product> products = productService.searchProducts(query, pageable);
        return ResponseEntity.ok(ApiResponse.success("Search results", products));
    }

    @Operation(summary = "Get products by price range (paginated)")
    @GetMapping("/filter")
    public ResponseEntity<ApiResponse<Page<Product>>> filterByPrice(
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        size = Math.min(size, 100);
        Pageable pageable = PageRequest.of(page, size);
        Page<Product> products = productService.filterByPrice(minPrice, maxPrice, pageable);
        return ResponseEntity.ok(ApiResponse.success("Filtered products", products));
    }

    @Operation(summary = "Add new product (Admin only)")
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping
    public ResponseEntity<ApiResponse<Product>> addProduct(@Valid @RequestBody Product product) {
        Product savedProduct = productService.addProduct(product);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Product created successfully", savedProduct));
    }

    @Operation(summary = "Update product (Admin only)")
    @SecurityRequirement(name = "bearerAuth")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Product>> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody Product product) {
        Product updatedProduct = productService.updateProduct(id, product);
        if (updatedProduct == null) {
            throw new ResourceNotFoundException("Product", id);
        }
        return ResponseEntity.ok(ApiResponse.success("Product updated successfully", updatedProduct));
    }

    @Operation(summary = "Update product stock (Admin only)")
    @SecurityRequirement(name = "bearerAuth")
    @PatchMapping("/{id}/stock")
    public ResponseEntity<ApiResponse<Product>> updateStock(
            @PathVariable Long id,
            @RequestParam int quantity) {
        Product updatedProduct = productService.updateStock(id, quantity);
        if (updatedProduct == null) {
            throw new ResourceNotFoundException("Product", id);
        }
        return ResponseEntity.ok(ApiResponse.success("Stock updated", updatedProduct));
    }

    @Operation(summary = "Delete product (Admin only)")
    @SecurityRequirement(name = "bearerAuth")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(@PathVariable Long id) {
        Product existing = productService.getProductById(id);
        if (existing == null) {
            throw new ResourceNotFoundException("Product", id);
        }
        productService.deleteProduct(id);
        return ResponseEntity.ok(ApiResponse.success("Product deleted successfully", null));
    }

    @Operation(summary = "Get low stock products (Admin only)")
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/low-stock")
    public ResponseEntity<ApiResponse<List<Product>>> getLowStockProducts(
            @RequestParam(defaultValue = "5") int threshold) {
        List<Product> products = productService.getLowStockProducts(threshold);
        return ResponseEntity.ok(ApiResponse.success("Low stock products", products));
    }
}