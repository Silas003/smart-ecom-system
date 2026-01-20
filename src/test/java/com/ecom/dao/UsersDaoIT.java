package com.ecom.dao;

import com.ecom.models.User;
import com.ecom.exceptions.DaoException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

public class UsersDaoIT {

    @BeforeAll
    public static void init() throws Exception {
        TestDb.setupDb();
    }

    @Test
    public void testCreateAndFindById() throws DaoException, SQLException {
        // Create a user via UsersDao
        User user = new User();
        user.setUsername("bob");
        user.setEmail("bob@example.com");
        user.setPhone("1234567890");
        user.setPassword("secret");
        user.setRole("customer");

        UsersDao.createUser(user);

        // fetch by email
        User fetched = UsersDao.getUserByEmail("bob@example.com");
        assertNotNull(fetched);
        assertEquals("bob", fetched.getUsername());

        // fetch by id
        User byId = UsersDao.getUserById(fetched.getUserId());
        assertNotNull(byId);
        assertEquals(fetched.getEmail(), byId.getEmail());

        // cleanup
        UsersDao.deleteUser(byId.getUserId());
        assertNull(UsersDao.getUserByEmail("bob@example.com"));
    }
}

