package com.ecom.services;

import com.ecom.models.Product;
import java.util.HashMap;
import java.util.Map;

/**
 * Singleton service for managing the user's shopping cart in-memory.
 */
public class CartService {
    private static CartService instance;
    private Map<Integer, Integer> cart; // productId -> quantity

    private CartService() {
        this.cart = new HashMap<>();
    }

    public static CartService getInstance() {
        if (instance == null) {
            instance = new CartService();
        }
        return instance;
    }

    public void addToCart(int productId, int quantity) {
        cart.put(productId, cart.getOrDefault(productId, 0) + quantity);
    }

    public void removeFromCart(int productId) {
        cart.remove(productId);
    }

    public void updateQuantity(int productId, int quantity) {
        if (quantity <= 0) {
            cart.remove(productId);
        } else {
            cart.put(productId, quantity);
        }
    }

    public int getQuantity(int productId) {
        return cart.getOrDefault(productId, 0);
    }

    public Map<Integer, Integer> getCart() {
        return new HashMap<>(cart);
    }

    public void clearCart() {
        cart.clear();
    }

    public int getTotalItems() {
        return cart.values().stream().mapToInt(Integer::intValue).sum();
    }

    public boolean isEmpty() {
        return cart.isEmpty();
    }
}
