package com.ecom.services;

import com.ecom.dao.ProductDao;
import com.ecom.models.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

public class ProductServiceTest {

    private ProductDao mockDao;
    private ProductService service;

    @BeforeEach
    public void setup() {
        mockDao = Mockito.mock(ProductDao.class);
        service = new ProductService(mockDao);
    }

    @Test
    public void testSearchPassesCategoryThrough() throws SQLException {
        when(mockDao.search(anyString(), eq(5), anyInt(), anyInt(), anyString(), anyBoolean()))
                .thenReturn(List.of(new Product(1,5,"P1", 9.99, 10)));

        List<Product> results = service.search("", 5, 0, 10, "price", true, false);
        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals(5, results.get(0).getCategoryId());
    }

    @Test
    public void testCountPassesCategoryThrough() throws SQLException {
        when(mockDao.count(anyString(), eq(7))).thenReturn(42);
        int c = service.count("", 7);
        assertEquals(42, c);
    }
}

