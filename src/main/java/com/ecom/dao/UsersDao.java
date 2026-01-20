package com.ecom.dao;

import com.ecom.utils.DatabaseUtils;
import com.ecom.models.User;
import com.ecom.exceptions.DaoException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class UsersDao {
    public static void createUser(User user) throws DaoException{
        if(user == null) throw new DaoException("User is null");
        String sql = "insert into users(username,phone,email,password,userrole) values(?,?,?,?,?)";
        try(Connection conn = DatabaseUtils.getConnection()){
            PreparedStatement preparedStatement = conn.prepareStatement(sql);
            preparedStatement.setString(1,user.getUsername());
            preparedStatement.setString(2,user.getPhone());
            preparedStatement.setString(3,user.getEmail());
            preparedStatement.setString(4,user.getPassword());
            preparedStatement.setString(5,user.getRole());
            int rd = preparedStatement.executeUpdate();
            if(rd > 0)
                System.out.println("User created successfully.");

        } catch (SQLException e) {
            throw new DaoException("Failed to create user", e);
        }

    }

    public static User getUserById(int id) throws DaoException{
        if (id <= 0) throw new DaoException("Invalid user id: " + id);
        User user = null;
        String sql = "select id,username,email,phone,password,userrole from users where id=?";
        try(Connection conn = DatabaseUtils.getConnection()){
            PreparedStatement preparedStatement = conn.prepareStatement(sql);
            preparedStatement.setInt(1,id);
            ResultSet rs = preparedStatement.executeQuery();
            while (rs.next()){
                user = mapResultSetToUser(rs);
            }
            return user;
        } catch (SQLException e) {
            throw new DaoException("Failed to get user by id", e);
        }

    }

    public static User getUserByEmail(String email) throws DaoException{
        if (email == null || email.isBlank()) throw new DaoException("Email is required");
        User user = null;
        String sql = "select id,username,email,phone,password,userrole from users where email=?";
        try(Connection conn = DatabaseUtils.getConnection()){
            PreparedStatement preparedStatement = conn.prepareStatement(sql);
            preparedStatement.setString(1,email);
            ResultSet rs = preparedStatement.executeQuery();
            while (rs.next()){
                user = mapResultSetToUser(rs);
            }
            return user;
        } catch (SQLException e) {
            throw new DaoException("Failed to get user by email", e);
        }

    }
    
    public static boolean emailExists(String email) throws DaoException{
        if (email == null || email.isBlank()) return false;
        String sql = "select count(*) from users where email=?";
        try(Connection conn = DatabaseUtils.getConnection()){
            PreparedStatement preparedStatement = conn.prepareStatement(sql);
            preparedStatement.setString(1,email);
            ResultSet rs = preparedStatement.executeQuery();
            if (rs.next()){
                return rs.getInt(1) > 0;
            }
            return false;
        } catch (SQLException e) {
            throw new DaoException("Failed to check email existence", e);
        }
    }

    public static List<User> findAll() throws DaoException{
        List<User> users= new ArrayList<>();
        String sql = "select id,username,email,phone,password,userrole from users ";
        try(Connection conn = DatabaseUtils.getConnection()){
            PreparedStatement preparedStatement = conn.prepareStatement(sql);
            ResultSet rs = preparedStatement.executeQuery();
            while (rs.next()){
                users.add(mapResultSetToUser(rs));
            }
            return users;

        } catch (SQLException e) {
            throw new DaoException("Failed to retrieve users", e);
        }

    }

    public static void updateUser(User user) throws DaoException {
        if (user == null) throw new DaoException("User object cannot be null.");
        if (user.getUserId() <= 0) throw new DaoException("Invalid user ID: " + user.getUserId());

        String sql = "UPDATE users SET username = ?, phone = ?, email = ?, userrole = ?";
        boolean updatePassword = user.getPassword() != null && !user.getPassword().isEmpty();
        if (updatePassword) {
            sql += ", password = ?";
        }
        sql += " WHERE id = ?";

        try (Connection conn = DatabaseUtils.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getPhone());
            stmt.setString(3, user.getEmail());
            stmt.setString(4, user.getRole());

            int paramIndex = 5;
            if (updatePassword) {
                stmt.setString(paramIndex++, user.getPassword());
            }
            stmt.setInt(paramIndex, user.getUserId());

            int rowsUpdated = stmt.executeUpdate();
            if (rowsUpdated == 0) {
                throw new DaoException("No user found with ID: " + user.getUserId());
            }
        } catch (SQLException e) {
            throw new DaoException("Failed to update user: " + e.getMessage(), e);
        }
    }

    public static void deleteUser(int id) throws DaoException{
        if (id <= 0) throw new DaoException("Invalid user id: " + id);
        String sql = "delete from users where id=?";
        try(Connection conn = DatabaseUtils.getConnection()){
            PreparedStatement preparedStatement = conn.prepareStatement(sql);
            preparedStatement.setInt(1,id);
            int rd = preparedStatement.executeUpdate();
            if(rd > 0)
                System.out.println("User deleted successfully.");

        } catch (SQLException e) {
            throw new DaoException("Failed to delete user", e);
        }

    }

    private static User mapResultSetToUser(ResultSet rs) {
        User user = new User();
        try {
            user.setEmail(rs.getString("email"));
            user.setRole(rs.getString("userrole"));
            user.setPhone(rs.getString("phone"));
            user.setUserId(rs.getInt("id"));
            user.setUsername(rs.getString("username"));
            try { user.setPassword(rs.getString("password")); } catch (SQLException ignored) {}
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return user;
        
}
}
