package com.ecom.services;

import com.ecom.models.User;
import com.ecom.dao.UsersDao;
import com.ecom.utils.PasswordUtils;
import com.ecom.exceptions.AuthenticationException;
import com.ecom.exceptions.DaoException;
import com.ecom.exceptions.DuplicateEntityException;
import com.ecom.exceptions.ValidationException;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;

public class UserServiceTest {

    @Test
    public void loginSuccess() throws Exception {
        String email = "jdoe@example.com";
        String plain = "password123";
        User u = new User();
        u.setEmail(email);
        u.setUsername("jdoe");
        u.setPassword(PasswordUtils.hashPassword(plain));

        try (MockedStatic<UsersDao> mocked = Mockito.mockStatic(UsersDao.class)) {
            mocked.when(() -> UsersDao.getUserByEmail(email)).thenReturn(u);
            var res = UserService.login(email, plain);
            assertNotNull(res);
            assertEquals("jdoe", res.getUsername());
        }
    }

    @Test
    public void loginWrongPasswordThrows() throws Exception {
        String email = "jdoe@example.com";
        User u = new User();
        u.setEmail(email);
        u.setUsername("jdoe");
        u.setPassword(PasswordUtils.hashPassword("otherpass"));

        try (MockedStatic<UsersDao> mocked = Mockito.mockStatic(UsersDao.class)) {
            mocked.when(() -> UsersDao.getUserByEmail(email)).thenReturn(u);
            assertThrows(AuthenticationException.class, () -> UserService.login(email, "password123"));
        }
    }

    @Test
    public void signUpDuplicateThrows() throws Exception {
        User u = new User();
        u.setEmail("new@example.com"); u.setUsername("new"); u.setPassword("Password1!");
        try (MockedStatic<UsersDao> mocked = Mockito.mockStatic(UsersDao.class)) {
            mocked.when(() -> UsersDao.emailExists(u.getEmail())).thenReturn(true);
            assertThrows(DuplicateEntityException.class, () -> UserService.signUp(u));
        }
    }
}
