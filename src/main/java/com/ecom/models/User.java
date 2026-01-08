package com.ecom.models;


public class User {
    private int userId;
    private String username;
    private String email;
    private String passwordHash;
    private String role;
  
    public User() {}
  
    public User(int userId,String username, String email, String role) {
      this.userId = userId;
      this.email = email;
      this.role = role;
    }
  
    public User(String email, String passwordHash, String role) {
      this.email = email;
      this.passwordHash = passwordHash;
      this.role = role;
    }
  
    public int getUserId() {
      return userId;
    }
  
    public void setUserId(int userId) {
      this.userId = userId;
    }
  
    public String getEmail() {
      return email;
    }
  
    public void setEmail(String email) {
      this.email = email;
    }
  
    public String getPasswordHash() {
      return passwordHash;
    }
  
    public void setPasswordHash(String passwordHash) {
      this.passwordHash = passwordHash;
    }
  
    public String getRole() {
      return role;
    }
  
    public void setRole(String role) {
      this.role = role;
    }
  
    @Override
    public String toString() {
      return "User{id=" + userId + ", email='" + email + "', role='" + role + "'}";
    }
  }