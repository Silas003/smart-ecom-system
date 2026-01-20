package com.ecom.dao;

import com.ecom.exceptions.InsufficientInventoryException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;

import static org.junit.jupiter.api.Assertions.*;

public class InventoryDaoIT {

    @BeforeAll
    public static void init() throws Exception {
        TestDb.setupDb();
    }

    @Test
    public void testDecreaseIncreaseStock() throws Exception {
        // insert product
        try (Connection conn = DriverManager.getConnection(System.getProperty("DB_URL"), System.getProperty("DB_USERNAME"), System.getProperty("DB_PASSWORD"))) {
            PreparedStatement ps = conn.prepareStatement("INSERT INTO products (category_id,name,price,stock_quantity) VALUES (?,?,?,?)", new String[]{"id"});
            ps.setInt(1, 0);
            ps.setString(2, "TestItem");
            ps.setDouble(3, 1.0);
            ps.setInt(4, 10);
            ps.executeUpdate();
        }

        // fetch product id
        int pid;
        try (Connection conn = DriverManager.getConnection(System.getProperty("DB_URL"), System.getProperty("DB_USERNAME"), System.getProperty("DB_PASSWORD"))) {
            var rs = conn.createStatement().executeQuery("SELECT id FROM products WHERE name='TestItem'");
            rs.next(); pid = rs.getInt(1);
        }

        InventoryDao dao = new InventoryDao();
        int before = dao.getStock(pid);
        assertEquals(10, before);

        // decrease by 3
        dao.decreaseStock(pid, 3);
        assertEquals(7, dao.getStock(pid));

        // increasing by 5
        dao.increaseStock(pid, 5);
        assertEquals(12, dao.getStock(pid));

        // decrease more than available -> exception
        assertThrows(InsufficientInventoryException.class, () -> dao.decreaseStock(pid, 100));

        // set absolute stock
        dao.setStock(pid, 4);
        assertEquals(4, dao.getStock(pid));
    }
}

