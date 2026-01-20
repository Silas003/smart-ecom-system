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

/**
 * Service layer for order-related business logic.
 *
 * Handles validation of cart contents and delegates persistence to {@code OrderDao}.
 * Converts lower-level exceptions into service-level exceptions for the UI layer.
 */
public class OrderService {
    private OrderDao orderDao;

    public OrderService() {
        this.orderDao = new OrderDao();
    }

    /**
     * Perform checkout for a user's cart.
     * Validates input and delegates to the DAO to persist the order transactionally.
     *
     * @param userId the id of the user placing the order
     * @param cart the cart contents (Product -> quantity)
     * @return true when checkout completed successfully
     * @throws DaoException when a database error or validation error occurs
     * @throws InsufficientInventoryException when inventory is insufficient
     */
    public boolean checkout(int userId, Map<Product, Integer> cart) throws DaoException, InsufficientInventoryException {
        if (cart == null || cart.isEmpty()) throw new DaoException("Cart is empty");
        for (Map.Entry<Product, Integer> e : cart.entrySet()){
            if (e.getKey() == null) throw new DaoException("Invalid product in cart");
            if (e.getValue() == null || e.getValue() <= 0) throw new DaoException("Invalid quantity for product " + e.getKey().getProductId());
        }
        try {
            // default checkout without address
            return orderDao.placeOrder(userId, cart);
        } catch (InsufficientInventoryException e) {
            e.getMessage();
            throw e;
        } catch (Exception e) {
            throw new DaoException("Checkout failed: " + e.getMessage(), e);
        }
    }

    /**
     * Overloaded checkout that accepts shipping address details.
     *
     * @param userId the id of the user placing the order
     * @param cart the cart contents (Product -> quantity)
     * @param city shipping city
     * @param region shipping region/state
     * @param zipCode shipping postal code
     * @param address shipping address line
     * @return true when checkout completed successfully
     * @throws DaoException when a DB or validation error occurs
     * @throws InsufficientInventoryException when inventory is insufficient
     */
    public boolean checkout(int userId, Map<Product, Integer> cart, String city, String region, String zipCode,String address) throws DaoException, InsufficientInventoryException {
        if (cart == null || cart.isEmpty()) throw new DaoException("Cart is empty");
        for (Map.Entry<Product, Integer> e : cart.entrySet()){
            if (e.getKey() == null) throw new DaoException("Invalid product in cart");
            if (e.getValue() == null || e.getValue() <= 0) throw new DaoException("Invalid quantity for product " + e.getKey().getProductId());
        }
        try {
            return orderDao.placeOrder(userId, cart, city, region, zipCode,address);
        } catch (InsufficientInventoryException e) {
            e.getMessage();
            throw e;
        } catch (Exception e) {
            throw new DaoException("Checkout failed: " + e.getMessage(), e);
        }
    }
}
