package com.harsh.uday.service;

import com.harsh.uday.exception.ApiException;
import com.harsh.uday.exception.InsufficientStockException;
import com.harsh.uday.model.*;
import com.harsh.uday.repository.CartItemRepository;
import com.harsh.uday.repository.CartRepository;
import com.harsh.uday.repository.ProductRepository;
import com.harsh.uday.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Cart service with proper quantity tracking via CartItem entity.
 */
@Service
@Transactional
public class CartService {

    private static final Logger logger = LoggerFactory.getLogger(CartService.class);

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public CartService(CartRepository cartRepository,
            CartItemRepository cartItemRepository,
            ProductRepository productRepository,
            UserRepository userRepository) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    /**
     * Get cart for a user, creating one if it doesn't exist
     */
    public Cart getCartByUser(Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) return null;

        return cartRepository.findByUser(user)
                .orElseGet(() -> {
                    Cart newCart = new Cart(user);
                    return cartRepository.save(newCart);
                });
    }

    /**
     * Add product to cart with specified quantity.
     * If product already in cart, increments the quantity.
     */
    public Cart addProductToCart(Long userId, Long productId, int quantity) {
        if (quantity < 1) {
            throw new ApiException("Quantity must be at least 1", HttpStatus.BAD_REQUEST);
        }

        User user = userRepository.findById(userId).orElse(null);
        Product product = productRepository.findById(productId).orElse(null);

        if (user == null || product == null) {
            logger.warn("Add to cart failed: user={}, product={}", userId, productId);
            return null;
        }

        if (product.getStock() < quantity) {
            throw new InsufficientStockException(product.getName(), product.getStock(), quantity);
        }

        Cart cart = cartRepository.findByUser(user).orElseGet(() -> cartRepository.save(new Cart(user)));

        // Check if product already in cart → increment quantity
        CartItem existingItem = cartItemRepository.findByCartAndProduct(cart, product).orElse(null);

        if (existingItem != null) {
            int newQty = existingItem.getQuantity() + quantity;
            if (product.getStock() < newQty) {
                throw new InsufficientStockException(product.getName(), product.getStock(), newQty);
            }
            existingItem.setQuantity(newQty);
            cartItemRepository.save(existingItem);
            logger.info("Product {} quantity updated to {} in cart for user {}", productId, newQty, userId);
        } else {
            CartItem newItem = new CartItem(cart, product, quantity);
            cart.getItems().add(newItem);
            logger.info("Product {} (qty={}) added to cart for user {}", productId, quantity, userId);
        }

        // Record action history
        CartActionHistory history = new CartActionHistory("ADD_PRODUCT", productId, cart);
        cart.getActionHistory().add(history);

        return cartRepository.save(cart);
    }

    /**
     * Update quantity of a specific cart item.
     */
    public Cart updateCartItemQuantity(Long userId, Long productId, int quantity) {
        if (quantity < 0) {
            throw new ApiException("Quantity cannot be negative", HttpStatus.BAD_REQUEST);
        }

        User user = userRepository.findById(userId).orElse(null);
        Product product = productRepository.findById(productId).orElse(null);
        if (user == null || product == null) return null;

        Cart cart = cartRepository.findByUser(user).orElse(null);
        if (cart == null) return null;

        CartItem item = cartItemRepository.findByCartAndProduct(cart, product).orElse(null);
        if (item == null) {
            throw new ApiException("Product not in cart", HttpStatus.NOT_FOUND);
        }

        if (quantity == 0) {
            // Remove item entirely
            cart.getItems().remove(item);
            cartItemRepository.delete(item);
            CartActionHistory history = new CartActionHistory("REMOVE_PRODUCT", productId, cart);
            cart.getActionHistory().add(history);
            logger.info("Product {} removed from cart for user {}", productId, userId);
        } else {
            if (product.getStock() < quantity) {
                throw new InsufficientStockException(product.getName(), product.getStock(), quantity);
            }
            item.setQuantity(quantity);
            cartItemRepository.save(item);
            CartActionHistory history = new CartActionHistory("UPDATE_QUANTITY", productId, cart);
            cart.getActionHistory().add(history);
            logger.info("Product {} quantity set to {} for user {}", productId, quantity, userId);
        }

        return cartRepository.save(cart);
    }

    /**
     * Remove product from cart entirely.
     */
    public Cart removeProductFromCart(Long userId, Long productId) {
        User user = userRepository.findById(userId).orElse(null);
        Product product = productRepository.findById(productId).orElse(null);
        if (user == null || product == null) return null;

        Cart cart = cartRepository.findByUser(user).orElse(null);
        if (cart == null) return null;

        CartItem item = cartItemRepository.findByCartAndProduct(cart, product).orElse(null);
        if (item != null) {
            cart.getItems().remove(item);
            cartItemRepository.delete(item);
            CartActionHistory history = new CartActionHistory("REMOVE_PRODUCT", productId, cart);
            cart.getActionHistory().add(history);
            logger.info("Product {} removed from cart for user {}", productId, userId);
            return cartRepository.save(cart);
        }
        return cart;
    }

    /**
     * Clear all items from cart.
     */
    public boolean clearCart(Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) return false;

        Cart cart = cartRepository.findByUser(user).orElse(null);
        if (cart != null) {
            cart.getItems().clear();
            CartActionHistory history = new CartActionHistory("CLEAR_CART", null, cart);
            cart.getActionHistory().add(history);
            cartRepository.save(cart);
            logger.info("Cart cleared for user {}", userId);
            return true;
        }
        return false;
    }

    /**
     * Get cart history with authorization check
     */
    public List<CartActionHistory> getCartHistory(Long userId, boolean isAdmin, Long requesterId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) return null;

        Cart cart = cartRepository.findByUser(user).orElse(null);
        if (cart == null) return null;

        if (!isAdmin && !user.getId().equals(requesterId)) {
            logger.warn("Unauthorized cart history access: requester={}, owner={}", requesterId, userId);
            return null;
        }

        return cart.getActionHistory();
    }

    /**
     * Get cart total item count (sum of quantities).
     */
    public int getCartItemCount(Long userId) {
        Cart cart = getCartByUser(userId);
        return cart != null ? cart.getTotalQuantity() : 0;
    }

    /**
     * Get cart total amount (sum of price × quantity).
     */
    public double getCartTotal(Long userId) {
        Cart cart = getCartByUser(userId);
        return cart != null ? cart.getTotalAmount() : 0.0;
    }
}
