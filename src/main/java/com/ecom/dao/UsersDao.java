package com.ecom.dao;

import com.ecom.utils.DatabaseUtils;
import org.postgresql.util.PSQLException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UsersDao {
    public static void create(){
        String sql = "insert into users(username,phone,email,password) values(?,?,?,?)";
        try(Connection conn = DatabaseUtils.getConnection()){
            PreparedStatement preparedStatement = conn.prepareStatement(sql);
            preparedStatement.setString(1,"john_doe");
            preparedStatement.setString(2,"1234567890");
            preparedStatement.setString(3,"fs#mail.com");
            preparedStatement.setString(4,"password123");
            int rd = preparedStatement.executeUpdate();
            if(rd > 0)
                System.out.println("User created successfully.");

        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

    }

    public static void findById(int id){
        String sql = "select username,email,phone from users where id=?";
        try(Connection conn = DatabaseUtils.getConnection()){
            PreparedStatement preparedStatement = conn.prepareStatement(sql);
            preparedStatement.setInt(1,id);
            ResultSet rs = preparedStatement.executeQuery();
            while (rs.next()){
                System.out.println("User ID: " + rs.getInt("id"));
                System.out.println("Username: " + rs.getString("username"));
                System.out.println("Phone: " + rs.getString("phone"));
                System.out.println("Email: " + rs.getString("email"));
                System.out.println("---------------------------");
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

    }

    public static void findByEmail(String email){
        String sql = "select username,email,phone from users where email=?";
        try(Connection conn = DatabaseUtils.getConnection()){
            PreparedStatement preparedStatement = conn.prepareStatement(sql);
            preparedStatement.setString(1,email);
            ResultSet rs = preparedStatement.executeQuery();
            while (rs.next()){
                System.out.println("User ID: " + rs.getInt("id"));
                System.out.println("Username: " + rs.getString("username"));
                System.out.println("Phone: " + rs.getString("phone"));
                System.out.println("Email: " + rs.getString("email"));
                System.out.println("---------------------------");
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

    }

    public static void findAll(){
        String sql = "select username,email,phone from users ";
        try(Connection conn = DatabaseUtils.getConnection()){
            PreparedStatement preparedStatement = conn.prepareStatement(sql);
            ResultSet rs = preparedStatement.executeQuery();
            while (rs.next()){
                System.out.println("User ID: " + rs.getInt("id"));
                System.out.println("Username: " + rs.getString("username"));
                System.out.println("Phone: " + rs.getString("phone"));
                System.out.println("Email: " + rs.getString("email"));
                System.out.println("---------------------------");
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

    }

    public static void update(String username,int id){
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
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

    }

    public static void delete(int id){
        String sql = "delete from users where id=?";
        try(Connection conn = DatabaseUtils.getConnection()){
            PreparedStatement preparedStatement = conn.prepareStatement(sql);
            preparedStatement.setInt(1,id);
            int rd = preparedStatement.executeUpdate();
            if(rd > 0)
                System.out.println("User deleted successfully.");

        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

    }
}
