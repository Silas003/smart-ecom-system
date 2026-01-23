package com.ecom.models;

public class Cart {
    private int id;
    private int userId;
    private String status;


    public Cart(int userId, String status) {
        this.userId = userId;
        this.status = status;
    }

    public Cart(int id,int userId, String status) {
        this.id = id;
        this.userId = userId;
        this.status = status;
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

    public void setTotalPrice(double totalPrice) {
    }
}
