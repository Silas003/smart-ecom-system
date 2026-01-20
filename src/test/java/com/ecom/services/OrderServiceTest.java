package com.ecom.services;

import com.ecom.dao.OrderDao;
import com.ecom.models.Product;
import com.ecom.exceptions.DaoException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class OrderServiceTest {
    private OrderDao mockDao;
    private OrderService service;

    @BeforeEach
    public void setup() {
        mockDao = Mockito.mock(OrderDao.class);
        service = new OrderService();
        try {
            var f = OrderService.class.getDeclaredField("orderDao");
            f.setAccessible(true);
            f.set(service, mockDao);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testCheckoutDelegatesToDao() throws Exception {
        Map<Product,Integer> cart = new HashMap<>();
        Product p = new Product(); p.setProductId(1); p.setPrice(10);
        cart.put(p,1);
        when(mockDao.placeOrder(1, cart)).thenReturn(true);

        boolean ok = service.checkout(1, cart);
        assertTrue(ok);
        verify(mockDao, times(1)).placeOrder(1, cart);
    }

    @Test
    public void testCheckoutWithAddressDelegatesToDao() throws Exception {
        Map<Product,Integer> cart = new HashMap<>();
        Product p = new Product(); p.setProductId(1); p.setPrice(10);
        cart.put(p,1);
        when(mockDao.placeOrder(1, cart, "City", "Region", "Zip","address")).thenReturn(true);

        boolean ok = service.checkout(1, cart, "City", "Region", "Zip","address");
        assertTrue(ok);
        verify(mockDao, times(1)).placeOrder(1, cart, "City", "Region", "Zip","address");
    }

    @Test
    public void testCheckoutEmptyCartThrows() {
        assertThrows(DaoException.class, () -> service.checkout(1, new HashMap<>()));
    }
}
