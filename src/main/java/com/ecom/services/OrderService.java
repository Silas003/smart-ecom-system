package com.ecom.services;

import com.ecom.utils.DatabaseUtils;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.ecom.dao.OrderDao;
import com.ecom.models.Product;
import com.ecom.exceptions.DaoException;
import com.ecom.exceptions.InsufficientInventoryException;
import com.ecom.utils.ValidationUtils;
import java.util.Map;

public class OrderService {
    private OrderDao orderDao;

    public OrderService() {
        this.orderDao = new OrderDao();
    }

    public boolean checkout(int userId, Map<Product, Integer> cart) throws DaoException, InsufficientInventoryException {
        if (cart == null || cart.isEmpty()) throw new DaoException("Cart is empty");
        // validate quantities
        for (Map.Entry<Product, Integer> e : cart.entrySet()){
            if (e.getKey() == null) throw new DaoException("Invalid product in cart");
            if (e.getValue() == null || e.getValue() <= 0) throw new DaoException("Invalid quantity for product " + e.getKey().getProductId());
        }
        try {
            return orderDao.placeOrder(userId, cart);
        } catch (InsufficientInventoryException e) {
            throw e;
        } catch (Exception e) {
            throw new DaoException("Checkout failed: " + e.getMessage(), e);
        }
    }
}
