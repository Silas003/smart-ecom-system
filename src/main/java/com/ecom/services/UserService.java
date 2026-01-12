package com.ecom.services;
import java.sql.*;

import com.ecom.dao.UsersDao;
import com.ecom.models.User;
import com.ecom.utils.PasswordUtils;

public class UserService {
    // Note: These fields are kept for potential future use with instance-based operations
    @SuppressWarnings("unused")
    private Connection connection = null;
    @SuppressWarnings("unused")
    private static UsersDao usersDao;
    
    public UserService(Connection conn,UsersDao usersDao){
        this.connection = conn;
        UserService.usersDao = usersDao;
    }

    public static User login(String email,String password){
        if(email == null || email.isBlank() || password == null || password.isEmpty()){
            return null;
        }
        
        if(!email.contains("@")){
            return null;
        }
        
        try {
            User user = UsersDao.getUserByEmail(email.toLowerCase());
            if(user == null){
                return null;
            }
            
            String hashedPassword = user.getPassword();
            if(hashedPassword == null || hashedPassword.isEmpty()){
                return null;
            }
            
            if(PasswordUtils.verifyPassword(password, hashedPassword)){
                return user; // Return user object on successful login
            }
            return null;
        } catch (Exception e) {
//            System.err.println("Error during login: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public static boolean signUp(User user){
        try {
            // Check if email already exists
            if(UsersDao.emailExists(user.getEmail())){
                return false;
            }
            
            // Hash the password before storing
            String hashedPassword = PasswordUtils.hashPassword(user.getPassword());
            user.setPassword(hashedPassword);
            
            // Set default role if not set
            if(user.getRole() == null || user.getRole().isEmpty()){
                user.setRole("customer");
            }
            
            UsersDao.createUser(user);
            return true;
        } catch (Exception e) {
            System.err.println("Error during signup: " + e.getMessage());
            return false;
        }
    }
}
