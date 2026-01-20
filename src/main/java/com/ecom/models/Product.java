package com.ecom.models;


/**
 * Domain model representing a product in the catalog.
 * Contains basic product attributes used by DAOs and UI.
 */
public class Product {
    private int productId;
    private int categoryId;
    private String name;
    private double price;
    private int stockQuantity;
  
    public Product() {}
  
    public Product(int productId, int categoryId, String name, double price, int stockQuantity) {
      this.productId = productId;
      this.categoryId = categoryId;
      this.name = name;
      this.price = price;
      this.stockQuantity = stockQuantity;
    }
  
    public Product(int categoryId, String name, double price, int stockQuantity) {
      this.categoryId = categoryId;
      this.name = name;
      this.price = price;
      this.stockQuantity = stockQuantity;
    }
  
    public int getProductId() {
      return productId;
    }
  
    public void setProductId(int productId) {
      this.productId = productId;
    }
  
    public int getCategoryId() {
      return categoryId;
    }
  
    public void setCategoryId(int categoryId) {
      this.categoryId = categoryId;
    }
  
    public String getName() {
      return name;
    }
  
    public void setName(String name) {
      this.name = name;
    }
  
    public double getPrice() {
      return price;
    }
  
    public void setPrice(double price) {
      this.price = price;
    }
  
    public int getStockQuantity() {
      return stockQuantity;
    }
  
    public void setStockQuantity(int stockQuantity) {
      this.stockQuantity = stockQuantity;
    }
  
    @Override
    public String toString() {
      return "Product{id="
          + productId
          + ", name='"
          + name
          + "', price="
          + price
          + ", stock="
          + stockQuantity
          + "}";
    }
  }