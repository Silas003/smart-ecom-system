package com.ecom.services;
import com.ecom.dao.ProductDao;
import com.ecom.models.Product;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import com.ecom.utils.ValidationUtils;
import com.ecom.exceptions.DaoException;
import com.ecom.exceptions.ValidationException;

public class ProductService {
    private final ProductDao productDAO;
    private final Map<Integer, Product> productCache;
    private final Map<String, List<Product>> listCache; // key: q|category|page|pageSize|sort|asc

    private static final ProductService INSTANCE = new ProductService(new ProductDao());
    public static ProductService getInstance() { return INSTANCE; }

    public ProductService() {
        this.productDAO = new ProductDao();
        this.productCache = new ConcurrentHashMap<>();
        this.listCache = new ConcurrentHashMap<>();
    }

    // constructor for injection/testing
    public ProductService(ProductDao productDAO) {
        this.productDAO = productDAO;
        this.productCache = new ConcurrentHashMap<>();
        this.listCache = new ConcurrentHashMap<>();
    }

    private String cacheKey(String q, Integer categoryId, int page, int pageSize, String sortBy, boolean asc) {
        return (q == null ? "" : q.toLowerCase()) + "|" + (categoryId == null ? "" : categoryId) + "|" + page + "|" + pageSize + "|" + sortBy + "|" + asc;
    }

    public List<Product> search(String q, int page, int pageSize, String sortBy, boolean asc, boolean useCache) throws SQLException {
        return search(q, null, page, pageSize, sortBy, asc, useCache);
    }

    public List<Product> search(String q, Integer categoryId, int page, int pageSize, String sortBy, boolean asc, boolean useCache) throws SQLException {
        String key = cacheKey(q, categoryId, page, pageSize, sortBy, asc);
        if (useCache && listCache.containsKey(key)) {
            recordCacheHit();
            return listCache.get(key);
        }
        recordCacheMiss();
        int offset = page * pageSize;
        List<Product> results = productDAO.search(q, categoryId, offset, pageSize, sortBy, asc);
        listCache.put(key, results);
        results.forEach(p -> productCache.put(p.getProductId(), p));
        return results;
    }

    // Simple cache metrics
    private long cacheHits = 0;
    private long cacheMisses = 0;

    private synchronized void recordCacheHit() { cacheHits++; }
    private synchronized void recordCacheMiss() { cacheMisses++; }

    public long getCacheHits() { return cacheHits; }
    public long getCacheMisses() { return cacheMisses; }

    public int getListCacheSize() { return listCache.size(); }
    public int getProductCacheSize() { return productCache.size(); }

    public int count(String q) throws SQLException {
        return count(q, null);
    }

    public int count(String q, Integer categoryId) throws SQLException {
        return productDAO.count(q, categoryId);
    }

    public Product getProductById(int id) throws SQLException {
        if (productCache.containsKey(id)) return productCache.get(id);
        Product p = productDAO.findById(id);
        if (p != null) productCache.put(id, p);
        return p;
    }

    public void createProduct(Product product) throws ValidationException, DaoException {
        if (product == null) throw new ValidationException("Product is required");
        String name = product.getName();
        ValidationUtils.requireNonEmpty(name, "product name");
        if (product.getPrice() < 0) throw new ValidationException("Price must be zero or positive", "price", product.getPrice());
        if (product.getStockQuantity() < 0) throw new ValidationException("Stock quantity must be non-negative", "stockQuantity", product.getStockQuantity());
        try {
            Product existing = productDAO.findByName(name);
            if (existing != null) {
                throw new com.ecom.exceptions.DuplicateEntityException("Product with name '" + name + "' already exists");
            }
            productDAO.create(product);
            invalidateAll();
        } catch (com.ecom.exceptions.DuplicateEntityException de) {
            throw de;
        } catch (SQLException e) {
            throw new DaoException("Failed to create product: " + e.getMessage(), e);
        }
    }

    public void updateProduct(Product product) throws ValidationException, DaoException {
        if (product == null) throw new ValidationException("Product is required");
        ValidationUtils.requireNonEmpty(product.getName(), "product name");
        if (product.getPrice() < 0) throw new ValidationException("Price must be zero or positive", "price", product.getPrice());
        if (product.getStockQuantity() < 0) throw new ValidationException("Stock quantity must be non-negative", "stockQuantity", product.getStockQuantity());
        try {
            // check duplicate name excluding self
            Product existing = productDAO.findByName(product.getName());
            if (existing != null && existing.getProductId() != product.getProductId()) {
                throw new com.ecom.exceptions.DuplicateEntityException("Another product with name '" + product.getName() + "' already exists");
            }
            productDAO.update(product);
            productCache.put(product.getProductId(), product);
            invalidateAll();
        } catch (com.ecom.exceptions.DuplicateEntityException de) {
            throw de;
        } catch (SQLException e) {
            throw new DaoException("Failed to update product: " + e.getMessage(), e);
        }
    }

    public void deleteProduct(int id) throws DaoException {
        try {
            productDAO.delete(id);
            productCache.remove(id);
            invalidateAll();
        } catch (SQLException e) {
            throw new DaoException("Failed to delete product", e);
        }
    }

    public List<Product> getAllProducts() throws DaoException {
        try {
            return productDAO.findAll();
        } catch (SQLException e) {
            throw new DaoException("Failed to fetch all products", e);
        }
    }

    public List<Product> sortProductsByPrice(List<Product> products, boolean ascending) {
        Comparator<Product> priceComparator = Comparator.comparingDouble(Product::getPrice);
        if (!ascending) {
            priceComparator = priceComparator.reversed();
        }
        return products.stream().sorted(priceComparator).collect(Collectors.toList());
    }

    public List<Product> searchProductsByName(String query) throws DaoException {
        // keep behaviour: fetch all and filter in-memory (backwards compatible)
        try {
            List<Product> allProducts = getAllProducts();
            return allProducts.stream()
                    .filter(p -> p.getName().toLowerCase().contains(query.toLowerCase()))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new DaoException("Failed to search products", e);
        }
    }

    public void clearCache() {
        productCache.clear();
        listCache.clear();
    }

    private void invalidateAll() {
        clearCache();
    }

    public List<Product> getProductsByCategory(int categoryId) throws SQLException {
        return productDAO.findByCategoryId(categoryId);
    }
}
