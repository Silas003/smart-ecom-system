package com.ecom.dao;

import com.ecom.models.Product;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ProductDaoIT {

    @BeforeAll
    public static void init() throws Exception {
        TestDb.setupDb();
    }

    @Test
    public void testCreateFindAndFindByCategory() throws SQLException {
        // insert category
        try (Connection conn = DriverManager.getConnection(System.getProperty("DB_URL"), System.getProperty("DB_USERNAME"), System.getProperty("DB_PASSWORD"))) {
            PreparedStatement ps = conn.prepareStatement("INSERT INTO categories (name) VALUES (?)", new String[]{"category_id"});
            ps.setString(1, "Electronics");
            ps.executeUpdate();
        }

        ProductDao dao = new ProductDao();
        Product p = new Product(1, "Phone", 199.99, 10);
        dao.create(p);
        assertTrue(p.getProductId() > 0);

        Product fetched = dao.findById(p.getProductId());
        assertNotNull(fetched);
        assertEquals("Phone", fetched.getName());

        List<Product> byCat = dao.findByCategoryId(1);
        assertFalse(byCat.isEmpty());

        dao.delete(p.getProductId());
        assertNull(dao.findById(p.getProductId()));
    }
}

