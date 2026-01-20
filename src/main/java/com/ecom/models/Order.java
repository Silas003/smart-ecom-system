package com.ecom.models;


import java.time.LocalDateTime;

/**
 * Domain model representing an order placed by a user.
 * Stores metadata such as order date, total amount and status.
 */
public class Order {
  private int orderId;
  private int userId;
  private LocalDateTime orderDate;
  private double totalAmount;
  private String status;

  public Order() {}

  public Order(int userId, double totalAmount) {
    this.userId = userId;
    this.totalAmount = totalAmount;
    this.orderDate = LocalDateTime.now();
  }

  public int getOrderId() {
    return orderId;
  }

  public void setOrderId(int orderId) {
    this.orderId = orderId;
  }

  public int getUserId() {
    return userId;
  }

  public void setUserId(int userId) {
    this.userId = userId;
  }

  public LocalDateTime getOrderDate() {
    return orderDate;
  }

  public void setOrderDate(LocalDateTime orderDate) {
    this.orderDate = orderDate;
  }

  public double getTotalAmount() {
    return totalAmount;
  }

  public void setTotalAmount(double totalAmount) {
    this.totalAmount = totalAmount;
  }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}