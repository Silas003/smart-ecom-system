package com.ecom.services;

import com.ecom.models.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class SessionServiceTest {
    private SessionService session;

    @BeforeEach
    public void setup() {
        session = SessionService.getInstance();
        session.logout();
    }

    @Test
    public void loginLogoutBehavior() {
        assertFalse(session.isLoggedIn());
        User u = new User(); u.setUserId(5); u.setUsername("test");
        session.setCurrentUser(u);
        assertTrue(session.isLoggedIn());
        assertEquals(5, session.getCurrentUserId());
        session.logout();
        assertFalse(session.isLoggedIn());
    }
}
