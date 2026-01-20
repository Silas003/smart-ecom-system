package com.ecom.services;

import com.ecom.dao.TestDb;
import com.ecom.dao.ProductDao;
import com.ecom.models.Product;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class InventoryServiceIT {

    @BeforeAll
    public static void init() throws Exception {
        TestDb.setupDb();
    }

    @Test
    public void testRestockLowInventory() throws Exception {
        try (Connection conn = DriverManager.getConnection(System.getProperty("DB_URL"), System.getProperty("DB_USERNAME"), System.getProperty("DB_PASSWORD"))) {
            PreparedStatement ps = conn.prepareStatement("INSERT INTO products (category_id,name,price,stock_quantity) VALUES (?,?,?,?)", new String[]{"id"});
            ps.setInt(1, 0); ps.setString(2, "LowItem"); ps.setDouble(3, 1.0); ps.setInt(4, 1); ps.executeUpdate();
            PreparedStatement ps2 = conn.prepareStatement("INSERT INTO products (category_id,name,price,stock_quantity) VALUES (?,?,?,?)", new String[]{"id"});
            ps2.setInt(1, 0); ps2.setString(2, "OkItem"); ps2.setDouble(3, 1.0); ps2.setInt(4, 20); ps2.executeUpdate();
        }

        InventoryService svc = new InventoryService();
        List<Product> restocked = svc.restockLowInventory(5, 10);
        assertFalse(restocked.isEmpty());
        boolean foundLow = restocked.stream().anyMatch(p -> p.getName().equals("LowItem") && p.getStockQuantity() >= 11);
        assertTrue(foundLow);
    }
}

