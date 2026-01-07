package com.ecom.services;

import com.ecom.utils.DatabaseUtils;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.ecom.dao.OrderDAO;
import com.ecom.models.Product;
import java.sql.SQLException;
import java.util.Map;

public class OrderService {
    private OrderDAO orderDAO;

    public OrderService() {
        this.orderDAO = new OrderDAO();
    }

    public boolean checkout(int userId, Map<Product, Integer> cart) {
        if (cart.isEmpty()) return false;
        try {
            return orderDAO.placeOrder(userId, cart);
        } catch (SQLException e) {
            System.err.println("Checkout failed: " + e.getMessage());
            return false;
        }
    }
}
