package com.ecom.models;

public class OrderItem {
    private int id;
    private int orderId;
    private int productId;
    private int quantity;
    private double priceAtPurchase;
  
    public OrderItem(int productId, int quantity, double priceAtPurchase) {
      this.productId = productId;
      this.quantity = quantity;
      this.priceAtPurchase = priceAtPurchase;
    }
  
    public int getId() {
      return id;
    }
  
    public void setId(int id) {
      this.id = id;
    }
  
    public int getOrderId() {
      return orderId;
    }
  
    public void setOrderId(int orderId) {
      this.orderId = orderId;
    }
  
    public int getProductId() {
      return productId;
    }
  
    public void setProductId(int productId) {
      this.productId = productId;
    }
  
    public int getQuantity() {
      return quantity;
    }
  
    public void setQuantity(int quantity) {
      this.quantity = quantity;
    }
  
    public double getPriceAtPurchase() {
      return priceAtPurchase;
    }
  
    public void setPriceAtPurchase(double priceAtPurchase) {
      this.priceAtPurchase = priceAtPurchase;
    }
  }