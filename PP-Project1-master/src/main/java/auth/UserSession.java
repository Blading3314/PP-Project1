package auth;

/**
 * Holds the currently logged-in user for this running app session.
 * It is intentionally simple because this is a desktop app with one active user at a time.
 */
public final class UserSession {
    private static String username;
    private static Role role;

    private UserSession() {
    }

    /**
     * Starts a session after the login screen authenticates the user.
     */
    public static void login(String loggedInUsername, Role loggedInRole) {
        username = loggedInUsername;
        role = loggedInRole;
    }

    /**
     * Clears the active user when the Logout button is clicked.
     */
    public static void logout() {
        username = null;
        role = null;
    }

    public static String getUsername() {
        return username;
    }

    public static Role getRole() {
        return role;
    }

    /**
     * Tells screens whether a user is currently signed in.
     */
    public static boolean isLoggedIn() {
        return role != null;
    }

    /**
     * Checks whether the current user has administrator privileges.
     */
    public static boolean isAdmin() {
        return role == Role.ADMIN;
    }

    /**
     * Central place for the delete permission rule.
     */
    public static boolean canDelete() {
        return isAdmin();
    }
}
