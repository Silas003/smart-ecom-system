package com.ecom.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ReviewsDao {
    private static Connection connection = null;

    public ReviewsDao(Connection conn) {
       connection = conn;
    }

    public static void create( int userId,int productId, String description,int stars){
        String sql = "insert into reviews(user_id,product_id,stars,description) values(?,?,?,?)";
        try(Connection conn = connection){
            PreparedStatement preparedStatement = conn.prepareStatement(sql);
            preparedStatement.setString(4,description);
            preparedStatement.setInt(1,userId);
            preparedStatement.setInt(2,productId);
            preparedStatement.setInt(3,stars);
            int rd = preparedStatement.executeUpdate();
            if(rd > 0)
                System.out.println("review created successfully.");

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    public static void read(){
        String sql = "select * from reviews";
        try(Connection conn = connection){
            PreparedStatement preparedStatement = conn.prepareStatement(sql);
            ResultSet rs = preparedStatement.executeQuery();
            while (rs.next()){
                System.out.println("User ID: " + rs.getInt("product_id"));
                System.out.println("Username: " + rs.getString("username"));
                System.out.println("Phone: " + rs.getString("phone"));
                System.out.println("Email: " + rs.getString("email"));
                System.out.println("---------------------------");
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    public static void update(int stars,int id){
        String sql = "update reviews set stars=? where id=?";
        try(Connection conn = connection){
            PreparedStatement preparedStatement = conn.prepareStatement(sql);

            preparedStatement.setInt(1,stars);
            preparedStatement.setInt(2,id);
            int rd = preparedStatement.executeUpdate();
            if(rd > 0)
                System.out.println("review updated successfully.");

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    public static void findByUserId(int id){
        String sql = "select * from reviews where user_id=?";
        try(Connection conn = connection){
            PreparedStatement preparedStatement = conn.prepareStatement(sql);
            preparedStatement.setInt(1,id);
            ResultSet rs = preparedStatement.executeQuery();
            while (rs.next()){
                System.out.println("User ID: " + rs.getInt("product_id"));
                System.out.println("Username: " + rs.getString("username"));
                System.out.println("Phone: " + rs.getString("phone"));
                System.out.println("Email: " + rs.getString("email"));
                System.out.println("---------------------------");
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    public static void delete(int id){
        String sql = "delete from reviews where id=?";
        try(Connection conn = connection){
            PreparedStatement preparedStatement = conn.prepareStatement(sql);
            preparedStatement.setInt(1,id);
            int rd = preparedStatement.executeUpdate();
            if(rd > 0)
                System.out.println("review deleted successfully.");

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }
}
