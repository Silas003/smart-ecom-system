package com.ecom.services;

import com.ecom.models.Product;
import com.ecom.exceptions.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ProductServiceNegativeTest {
    private ProductService service;

    @BeforeEach
    public void setup() {
        service = new ProductService();
    }

    @Test
    public void createProductNullThrows() {
        assertThrows(ValidationException.class, () -> service.createProduct(null));
    }

    @Test
    public void createProductNegativePriceThrows() {
        Product p = new Product(1, "Bad", -5.0, 10);
        assertThrows(ValidationException.class, () -> service.createProduct(p));
    }

    @Test
    public void updateProductNegativeStockThrows() {
        Product p = new Product(1, 1, "X", 10.0, -2);
        assertThrows(ValidationException.class, () -> service.updateProduct(p));
    }
}
