package com.ecom.services;

import com.ecom.dao.InventoryDao;
import com.ecom.dao.ProductDao;
import com.ecom.models.Product;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class InventoryService {
    private final InventoryDao inventoryDao;
    private final ProductDao productDao;

    public InventoryService() {
        this.inventoryDao = new InventoryDao();
        this.productDao = new ProductDao();
    }

    // Visible for testing/injection
    public InventoryService(InventoryDao inventoryDao, ProductDao productDao) {
        this.inventoryDao = inventoryDao;
        this.productDao = productDao;
    }

    /**
     * Find products with stock_quantity <= threshold and increase their stock by reorderQty.
     * Returns the list of products that were restocked (after restock, with updated quantities).
     */
    public List<Product> restockLowInventory(int threshold, int reorderQty) throws SQLException {
        List<Product> restocked = new ArrayList<>();
        List<Product> all = productDao.findAll();
        for (Product p : all) {
            if (p.getStockQuantity() <= threshold) {
                inventoryDao.increaseStock(p.getProductId(), reorderQty);
                // fetch updated product
                Product updated = productDao.findById(p.getProductId());
                restocked.add(updated);
            }
        }
        return restocked;
    }
}

