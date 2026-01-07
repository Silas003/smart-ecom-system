package com.ecom.services;
import com.ecom.dao.ProductDao;
import com.ecom.models.Product;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ProductService {
    private ProductDao productDAO;
    private Map<Integer, Product> productCache;

    public ProductService() {
        this.productDAO = new ProductDao();
        this.productCache = new HashMap<>();
    }

    // For testing injection
    public ProductService(ProductDao productDAO) {
        this.productDAO = productDAO;
        this.productCache = new HashMap<>();
    }

    public void createProduct(Product product) throws SQLException {
        productDAO.create(product);
        productCache.put(product.getProductId(), product);
    }

    public Product getProductById(int id) throws SQLException {
        // 1. Check Cache
        if (productCache.containsKey(id)) {
            System.out.println("Cache Hit for Product ID: " + id);
            return productCache.get(id);
        }


        System.out.println("Cache Miss for Product ID: " + id + ". Querying DB...");
        Product product = productDAO.findById(id);

        // 3. Put in Cache
        if (product != null) {
            productCache.put(id, product);
        }
        return product;
    }

    public List<Product> getAllProducts() throws SQLException {
        return productDAO.findAll();
    }

    public void updateProduct(Product product) throws SQLException {
        productDAO.update(product);
        // Invalidate/Update Cache
        productCache.put(product.getProductId(), product);
        System.out.println("Cache Updated for Product ID: " + product.getProductId());
    }

    public void deleteProduct(int id) throws SQLException {
        productDAO.delete(id);
        // Remove from Cache
        productCache.remove(id);
        System.out.println("Cache Cleared for Product ID: " + id);
    }

    // Sorting Algorithms
    public List<Product> sortProductsByPrice(List<Product> products, boolean ascending) {
        Comparator<Product> priceComparator = Comparator.comparingDouble(Product::getPrice);
        if (!ascending) {
            priceComparator = priceComparator.reversed();
        }
        return products.stream().sorted(priceComparator).collect(Collectors.toList());
    }

    public List<Product> searchProductsByName(String query) throws SQLException {
        // For now, we fetch all and filter in memory (or we could add a DAO method for LIKE query)
        // Instructions say: "Query uses SQL LIKE operator OR in-memory filtering."
        // Let's do in-memory for now as we have getAllProducts
        List<Product> allProducts = getAllProducts();
        return allProducts.stream()
                .filter(p -> p.getName().toLowerCase().contains(query.toLowerCase()))
                .collect(Collectors.toList());
    }

    public void clearCache() {
        productCache.clear();
    }
}