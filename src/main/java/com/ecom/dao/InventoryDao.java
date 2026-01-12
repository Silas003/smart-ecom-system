package com.ecom.dao;

import com.ecom.utils.DatabaseUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class InventoryDao {
    private static Connection connection = null;
    public InventoryDao(Connection conn){
        this.connection = conn;
    }

    public static void create(String proudct_id, String quantity_in_stock, String quantity_reserved, String stock_status){
        String sql = "insert into inventory(prodcut_id,quantity_in_stock,quantity_reserved,stock_status) values(?,?,?,?)";
        try(Connection conn = connection){
            PreparedStatement preparedStatement = conn.prepareStatement(sql);
            preparedStatement.setString(1,proudct_id);
            preparedStatement.setString(2,quantity_in_stock);
            preparedStatement.setString(3,quantity_reserved);
            preparedStatement.setString(4,stock_status);
            int rd = preparedStatement.executeUpdate();
            if(rd > 0)
                System.out.println("Inventory created successfully.");

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    public static void read(){
        String sql = "select inventory_id,product_id,quantity_in_stock,quantity_in_reserved,stock_status from inventory";
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

    public static void update(int quantity,int id){
        String sql = "update inventory set quantity_in_stock=? where id=?";
        try(Connection conn = connection){
            PreparedStatement preparedStatement = conn.prepareStatement(sql);

            preparedStatement.setInt(1,quantity);
                preparedStatement.setInt(2,id);
            int rd = preparedStatement.executeUpdate();
            if(rd > 0)
                System.out.println("inventory updated successfully.");

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    public static void delete(int id){
        String sql = "delete from inventory where id=?";
        try(Connection conn = connection){
            PreparedStatement preparedStatement = conn.prepareStatement(sql);
            preparedStatement.setInt(1,id);
            int rd = preparedStatement.executeUpdate();
            if(rd > 0)
                System.out.println("inventory deleted successfully.");

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }
}
