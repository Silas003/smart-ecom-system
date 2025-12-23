package com.ecom.services;

import com.ecom.utils.DatabaseUtils;
import org.postgresql.util.PSQLException;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class CustomerService {
    public static void main(String[] args) {
        try(Connection connection = DatabaseUtils.getConnection()) {
            String query = "select * from customers";
            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery(query);

            while (rs.next()){
                String data = rs.getString("first_name");
                System.out.println(data);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
