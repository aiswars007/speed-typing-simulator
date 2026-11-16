package com.speedtyping.service;

import com.speedtyping.dao.UserDAO;
import com.speedtyping.model.User;

public class AuthService {

    private final UserDAO userDAO;
    private static User currentUser;

    public AuthService() {
        this.userDAO = new UserDAO();
    }

    public static User getCurrentUser() {
        return currentUser;
    }

    public static void setCurrentUser(User user) {
        currentUser = user;
    }

    public static boolean isLoggedIn() {
        return currentUser != null;
    }

    public static void logout() {
        currentUser = null;
    }

    public User login(String username, String password) throws IllegalArgumentException {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be empty.");
        }
        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("Password cannot be empty.");
        }

        User user = userDAO.getUserByUsername(username.trim());
        if (user == null) {
            throw new IllegalArgumentException("Invalid username or password.");
        }

        if (!SecurityUtils.verifyPassword(password, user.getPassword())) {
            throw new IllegalArgumentException("Invalid username or password.");
        }

        // Seamlessly upgrade legacy plaintext passwords to PBKDF2 hash on successful login
        if (!SecurityUtils.isHashed(user.getPassword())) {
            String newHashedPassword = SecurityUtils.hashPassword(password);
            userDAO.updatePassword(user.getId(), newHashedPassword);
            user.setPassword(newHashedPassword);
        }

        currentUser = user;
        return user;
    }

    public User register(String username, String password, String confirmPassword, String fullName) throws IllegalArgumentException {
        if (username == null || username.trim().length() < 3) {
            throw new IllegalArgumentException("Username must be at least 3 characters long.");
        }
        if (username.contains(" ")) {
            throw new IllegalArgumentException("Username cannot contain spaces.");
        }
        if (password == null || password.length() < 4) {
            throw new IllegalArgumentException("Password must be at least 4 characters long.");
        }
        if (!password.equals(confirmPassword)) {
            throw new IllegalArgumentException("Passwords do not match.");
        }
        if (userDAO.usernameExists(username.trim())) {
            throw new IllegalArgumentException("Username '" + username.trim() + "' is already taken.");
        }

        try {
            String hashedPassword = SecurityUtils.hashPassword(password);
            User user = userDAO.registerUser(username.trim(), hashedPassword, fullName == null ? "" : fullName.trim());
            if (user != null) {
                currentUser = user;
                return user;
            } else {
                throw new IllegalArgumentException("Failed to register user. Please try again.");
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Database error during registration: " + e.getMessage());
        }
    }
}
