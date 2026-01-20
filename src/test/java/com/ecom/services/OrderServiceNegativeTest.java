package com.ecom.services;

import com.ecom.models.Product;
import com.ecom.exceptions.DaoException;
import com.ecom.exceptions.InsufficientInventoryException;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

public class OrderServiceNegativeTest {
    @Test
    public void emptyCartThrows() {
        OrderService svc = new OrderService();
        assertThrows(DaoException.class, () -> svc.checkout(1, new HashMap<>()));
    }

    @Test
    public void invalidQuantityThrows() {
        OrderService svc = new OrderService();
        HashMap<Product,Integer> cart = new HashMap<>();
        Product p = new Product(); p.setProductId(1);
        cart.put(p, 0);
        assertThrows(DaoException.class, () -> svc.checkout(1, cart));
    }
}
