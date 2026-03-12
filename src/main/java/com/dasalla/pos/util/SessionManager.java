package com.dasalla.pos.util;

import com.dasalla.pos.model.User;

public class SessionManager {
    private static User currentUser;

    public static void setCurrentUser(User user) {
        currentUser = user;
    }

    public static User getCurrentUser() {
        return currentUser;
    }

    public static void logout() {
        currentUser = null;
    }

    public static boolean isLoggedIn() {
        return currentUser != null;
    }

    public static boolean isAdmin() {
        return currentUser != null && "ADMIN".equals(currentUser.getRole());
    }

    public static boolean isCashier() {
        return currentUser != null && ("CASHIER".equals(currentUser.getRole()) || "ADMIN".equals(currentUser.getRole()));
    }
}
