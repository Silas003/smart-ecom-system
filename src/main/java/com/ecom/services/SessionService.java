package com.ecom.services;

import com.ecom.models.User;

/**
 * Singleton service to manage user session
 */
public class SessionService {
    private static SessionService instance;
    private User currentUser;
    private String pendingFxml; // new: store desired navigation after login

    private SessionService() {
        this.currentUser = null;
    }

    public static SessionService getInstance() {
        if (instance == null) {
            instance = new SessionService();
        }
        return instance;
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public int getCurrentUserId() {
        return currentUser != null ? currentUser.getUserId() : 0;
    }

    public boolean isLoggedIn() {
        return currentUser != null;
    }

    public void logout() {
        this.currentUser = null;
    }

    // Pending navigation helpers
    public void setPendingFxml(String fxml) {
        this.pendingFxml = fxml;
    }

    public String getPendingFxml() {
        return this.pendingFxml;
    }

    public void clearPendingFxml() {
        this.pendingFxml = null;
    }
}
