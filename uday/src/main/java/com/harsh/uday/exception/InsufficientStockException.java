package com.harsh.uday.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception for insufficient stock scenarios.
 */
public class InsufficientStockException extends ApiException {

    private final String productName;
    private final int availableStock;
    private final int requestedQuantity;

    public InsufficientStockException(String productName, int availableStock, int requestedQuantity) {
        super(String.format("Insufficient stock for product '%s'. Available: %d, Requested: %d",
                productName, availableStock, requestedQuantity),
                HttpStatus.BAD_REQUEST, "INSUFFICIENT_STOCK");
        this.productName = productName;
        this.availableStock = availableStock;
        this.requestedQuantity = requestedQuantity;
    }

    public InsufficientStockException(String productName) {
        super(String.format("Product '%s' is out of stock", productName),
                HttpStatus.BAD_REQUEST, "OUT_OF_STOCK");
        this.productName = productName;
        this.availableStock = 0;
        this.requestedQuantity = 1;
    }

    public String getProductName() {
        return productName;
    }

    public int getAvailableStock() {
        return availableStock;
    }

    public int getRequestedQuantity() {
        return requestedQuantity;
    }
}
