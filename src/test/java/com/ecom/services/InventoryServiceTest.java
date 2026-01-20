package com.ecom.services;

import com.ecom.dao.InventoryDao;
import com.ecom.dao.ProductDao;
import com.ecom.models.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class InventoryServiceTest {
    private InventoryDao invDao;
    private ProductDao prodDao;
    private InventoryService service;

    @BeforeEach
    public void setup() {
        invDao = Mockito.mock(InventoryDao.class);
        prodDao = Mockito.mock(ProductDao.class);
        service = new InventoryService(invDao, prodDao);
    }

    @Test
    public void restockLowInventory_callsIncreaseAndReturnsUpdated() throws SQLException {
        Product p1 = new Product(1,1,"P1",10.0,2);
        Product p1Updated = new Product(1,1,"P1",10.0,12);
        when(prodDao.findAll()).thenReturn(List.of(p1));
        when(prodDao.findById(1)).thenReturn(p1Updated);

        List<Product> restocked = service.restockLowInventory(5, 10);
        assertEquals(1, restocked.size());
        assertEquals(12, restocked.get(0).getStockQuantity());
        verify(invDao, times(1)).increaseStock(1, 10);
    }
}
