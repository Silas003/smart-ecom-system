package com.ecom.models;

import java.time.LocalDateTime;

public class Cart {
    private int id;
    private int userId;
    private String status;
    private Double amount;

    public Cart(int id, int userId, String status, double amount, LocalDateTime createdAt) {
    }

    public  int getId() {
        return id;
    }

    public  void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }
}
