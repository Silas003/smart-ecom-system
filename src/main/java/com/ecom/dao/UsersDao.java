package com.ecom.dao;

import com.ecom.utils.DatabaseUtils;
import com.ecom.models.User;
import org.postgresql.util.PSQLException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class UsersDao {
    public static void createUser(User user){
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
            throw new RuntimeException(e);
        }

    }

    public static User getUserById(int id){
        User user = null;
        String sql = "select id,username,email,phone,userrole from users where id=?";
        try(Connection conn = DatabaseUtils.getConnection()){
            PreparedStatement preparedStatement = conn.prepareStatement(sql);
            preparedStatement.setInt(1,id);
            ResultSet rs = preparedStatement.executeQuery();
            while (rs.next()){
                user = mapResultSetToUser(rs);
            }
            return user;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    public static User getUserByEmail(String email){
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
            throw new RuntimeException(e);
        }

    }
    
    public static boolean emailExists(String email){
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
            throw new RuntimeException(e);
        }
    }

    public static List<User> findAll(){
        List<User> users= new ArrayList<User>();
        String sql = "select id,username,email,phone,userrole from users ";
        try(Connection conn = DatabaseUtils.getConnection()){
            PreparedStatement preparedStatement = conn.prepareStatement(sql);
            ResultSet rs = preparedStatement.executeQuery();
            while (rs.next()){
                users.add(mapResultSetToUser(rs));
            }
            return users;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    public static void updateUser(String username,int id){
        String sql = "update users set username=? where id=?";
        try(Connection conn = DatabaseUtils.getConnection()){
            PreparedStatement preparedStatement = conn.prepareStatement(sql);
            if (username.length()>5)
            preparedStatement.setString(1,username);
            if (id>0)
            preparedStatement.setInt(2,id);
            int rd = preparedStatement.executeUpdate();
            if(rd > 0)
                System.out.println("User updated successfully.");

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    public static void deleteUser(int id){
        String sql = "delete from users where id=?";
        try(Connection conn = DatabaseUtils.getConnection()){
            PreparedStatement preparedStatement = conn.prepareStatement(sql);
            preparedStatement.setInt(1,id);
            int rd = preparedStatement.executeUpdate();
            if(rd > 0)
                System.out.println("User deleted successfully.");

        } catch (SQLException e) {
            throw new RuntimeException(e);
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
        user.setPassword(rs.getString("password"));
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return user;
        
}
}
