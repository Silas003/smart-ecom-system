package com.ecom;

import com.ecom.services.CustomerService;
import com.ecom.utils.DatabaseUtils;
import org.postgresql.util.PSQLException;

import java.sql.Connection;
import java.sql.SQLException;

public class Main {
    public static void main(String[] args) {
        CustomerService.main(args);
    }
}
