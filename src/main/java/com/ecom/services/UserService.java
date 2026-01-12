package com.ecom.services;
import java.sql.*;
import java.util.List;

import com.ecom.dao.UsersDao;
import com.ecom.models.User;
import com.ecom.utils.PasswordUtils;
import com.ecom.utils.ValidationUtils;
import com.ecom.exceptions.*;

public class UserService {
    // Note: These fields are kept for potential future use with instance-based operations
    @SuppressWarnings("unused")
    private Connection connection = null;
    @SuppressWarnings("unused")
    private static UsersDao usersDao;
    
    public UserService(Connection conn,UsersDao usersDao){
        this.connection = conn;
        UserService.usersDao = usersDao;
    }

    public UserService() {
        // default
    }

    public static User login(String email,String password) throws InvalidInputException, AuthenticationException, DaoException {
        // Basic validation
        ValidationUtils.requireNonEmpty(email, "email");
        ValidationUtils.requireNonEmpty(password, "password");
        if(!email.contains("@")){
            throw new InvalidInputException("Invalid email format", "email", email);
        }
        try {
            User user = UsersDao.getUserByEmail(email.toLowerCase());
            if(user == null){
                throw new AuthenticationException("Incorrect username or password");
            }
            String hashedPassword = user.getPassword();
            if(hashedPassword == null || hashedPassword.isEmpty()){
                throw new AuthenticationException("Incorrect username or password");
            }
            if(PasswordUtils.verifyPassword(password, hashedPassword)){
                return user; // Return user object on successful login
            }
            throw new AuthenticationException("Incorrect username or password");
        } catch (DaoException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            throw new DaoException("Unexpected error during login", e);
        }
    }

    public static void signUp(User user) throws ValidationException, DaoException {
        if (user == null) throw new InvalidInputException("User object is required");

        // Validate fields
        String email = ValidationUtils.trimToNull(user.getEmail());
        ValidationUtils.requireEmail(email, "email");
        ValidationUtils.requireNonEmpty(user.getUsername(), "username");
        ValidationUtils.requireStrongPassword(user.getPassword(), "password");

        try {
            // Check if email already exists
            if(UsersDao.emailExists(email)){
                throw new DuplicateEntityException("User", "email", email);
            }
            // Hash the password before storing
            String hashedPassword = PasswordUtils.hashPassword(user.getPassword());
            user.setPassword(hashedPassword);
            user.setEmail(email.toLowerCase());
            // Set default role if not set
            if(user.getRole() == null || user.getRole().isEmpty()){
                user.setRole("customer");
            }

            UsersDao.createUser(user);
        } catch (DaoException e) {
            throw e;
        } catch (ValidationException e) {
            // validation and duplicate exceptions should propagate
            throw e;
        } catch (Exception e) {
            System.err.println("Error during signup: " + e.getMessage());
            throw new DaoException("Unexpected error during signup", e);
        }
    }

    // New service methods for CRUD operations
    public static List<User> findAll() throws DaoException {
        try {
            return UsersDao.findAll();
        } catch (DaoException e) {
            throw e;
        } catch (Exception e) {
            throw new DaoException("Failed to retrieve users", e);
        }
    }

    public static User getUserById(int id) throws DaoException, InvalidInputException {
        if (id <= 0) throw new InvalidInputException("Invalid user id", "id", id);
        try {
            return UsersDao.getUserById(id);
        } catch (DaoException e) {
            throw e;
        } catch (Exception e) {
            throw new DaoException("Failed to get user by id", e);
        }
    }

    public static void createUser(User user) throws ValidationException, DaoException {
        if (user == null) throw new InvalidInputException("User is required");
        ValidationUtils.requireNonEmpty(user.getUsername(), "username");
        ValidationUtils.requireEmail(user.getEmail(), "email");
        // set default password if not provided
        if (user.getPassword() == null || user.getPassword().isEmpty()) {
            user.setPassword(PasswordUtils.hashPassword("changeme"));
        } else {
            // assume provided password is plaintext; hash it
            user.setPassword(PasswordUtils.hashPassword(user.getPassword()));
        }
        try {
            if (UsersDao.emailExists(user.getEmail())) {
                throw new DuplicateEntityException("User", "email", user.getEmail());
            }
            UsersDao.createUser(user);
        } catch (DaoException e) {
            throw e;
        }
    }

    public static void updateUser(User user) throws ValidationException, DaoException {
        if (user == null) throw new InvalidInputException("User is required");
        if (user.getUserId() <= 0) throw new InvalidInputException("Invalid user id", "id", user.getUserId());
        ValidationUtils.requireNonEmpty(user.getUsername(), "username");
        ValidationUtils.requireEmail(user.getEmail(), "email");
        try {
            // ensure email uniqueness if changed
            User existing = UsersDao.getUserById(user.getUserId());
            if (existing != null && !existing.getEmail().equalsIgnoreCase(user.getEmail())) {
                if (UsersDao.emailExists(user.getEmail())) {
                    throw new DuplicateEntityException("User", "email", user.getEmail());
                }
            }
            // if password looks like plaintext (not containing $ used by our hash), hash it
            if (user.getPassword() != null && !user.getPassword().isEmpty() && !user.getPassword().contains("$")) {
                user.setPassword(PasswordUtils.hashPassword(user.getPassword()));
            }
            UsersDao.updateUser(user);
        } catch (DaoException e) {
            throw e;
        }
    }

    public static void deleteUser(int id) throws DaoException, InvalidInputException {
        if (id <= 0) throw new InvalidInputException("Invalid user id", "id", id);
        try {
            UsersDao.deleteUser(id);
        } catch (DaoException e) {
            throw e;
        }
    }
}
