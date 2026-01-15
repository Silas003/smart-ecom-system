package com.ecom.dao;

import com.ecom.models.Order;
import com.ecom.models.OrderItem;
import com.ecom.models.Product;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class OrderDaoIT {

    @BeforeAll
    public static void init() throws Exception {
        TestDb.setupDb();
    }

    @Test
    public void testFindByUserIdAndOrderItems() throws SQLException {
        // create user and product, order, order_item manually
        try (Connection conn = DriverManager.getConnection(System.getProperty("DB_URL"), System.getProperty("DB_USERNAME"), System.getProperty("DB_PASSWORD"))) {
            PreparedStatement ps1 = conn.prepareStatement("INSERT INTO users (username,email,phone,password,userrole) VALUES (?,?,?,?,?)", new String[]{"id"});
            ps1.setString(1, "orderuser"); ps1.setString(2, "u@example.com"); ps1.setString(3, ""); ps1.setString(4, "pwd"); ps1.setString(5, "customer");
            ps1.executeUpdate();

            PreparedStatement ps2 = conn.prepareStatement("INSERT INTO products (category_id,name,price,stock_quantity) VALUES (?,?,?,?)", new String[]{"product_id"});
            ps2.setInt(1, 0); ps2.setString(2, "Widget"); ps2.setDouble(3, 9.99); ps2.setInt(4, 100);
            ps2.executeUpdate();

            // get ids
            var rsUser = conn.createStatement().executeQuery("SELECT id FROM users WHERE email='u@example.com'");
            rsUser.next(); int userId = rsUser.getInt(1);
            var rsProduct = conn.createStatement().executeQuery("SELECT product_id FROM products WHERE name='Widget'");
            rsProduct.next(); int productId = rsProduct.getInt(1);

            PreparedStatement psOrder = conn.prepareStatement("INSERT INTO orders (user_id,status,total_amount) VALUES (?,?,?)", new String[]{"order_id"});
            psOrder.setInt(1, userId); psOrder.setString(2, "NEW"); psOrder.setDouble(3, 19.98);
            psOrder.executeUpdate();
            var rsOrder = conn.createStatement().executeQuery("SELECT order_id FROM orders WHERE user_id="+userId);
            rsOrder.next(); int orderId = rsOrder.getInt(1);

            PreparedStatement psItem = conn.prepareStatement("INSERT INTO order_items (order_id,product_id,quantity,unit_price) VALUES (?,?,?,?)");
            psItem.setInt(1, orderId); psItem.setInt(2, productId); psItem.setInt(3, 2); psItem.setDouble(4, 9.99);
            psItem.executeUpdate();
        }

        OrderDao dao = new OrderDao();
        List<Order> orders = dao.findByUserId(1); // user id likely 1
        assertFalse(orders.isEmpty());
        Order o = orders.get(0);
        List<OrderItem> items = dao.findOrderItemsByOrderId(o.getOrderId());
        assertFalse(items.isEmpty());
    }
}

