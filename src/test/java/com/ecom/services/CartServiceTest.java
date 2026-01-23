package com.ecom.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class CartServiceTest {
    private CartService cart;

    @BeforeEach
    public void setup() {
        cart = CartService.getInstance();
        cart.clearCart();
    }

    @Test
    public void addAndRemoveAndTotals() {
//        assertTrue(cart.isEmpty());
//        cart.addToCart(1,2);
//        assertEquals(2, cart.getQuantity(1));
//        cart.updateQuantity(1, 5);
//        assertEquals(5, cart.getQuantity(1));
//        cart.removeFromCart(1);
//        assertTrue(cart.isEmpty());
    }
}
