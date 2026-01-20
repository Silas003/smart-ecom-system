package com.ecom.models;

/**
 * Domain model representing an application user.
 * Contains authentication and contact information.
 */
public class User {
    private int userId;
    private String username;
    private String email;
    private String phone;
    private String password;
    private String role;
  
    public User() {}
  
    public User(int userId,String username,String phone ,String email, String role) {
      this.userId = userId;
      this.email = email;
      this.role = role;
      this.phone = phone;
      this.username = username;
    }
  
    public User(String email, String password, String role) {
      this.email = email;
      this.password = password;
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
  
    public String getPassword() {
      return password;
    }
  
    public void setPassword(String password) {
      this.password = password;
    }
  
    public String getRole() {
      return role;
    }
  
    public void setRole(String role) {
      this.role = role;
    }
    
    public String getPhone(){
      return this.phone;
    }

    public void setPhone(String phone){
      this.phone = phone;
    }
    public void setUsername(String username){
      this.username = username;
    }
    public String getUsername(){
      return this.username;
    }
    @Override
    public String toString() {
      return "User{id=" + userId + ", email='" + email + "', role='" + role + "'}";
    }
  }