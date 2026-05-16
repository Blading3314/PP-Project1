package auth;

/**
 * User permission levels used by the login system.
 * Admins can delete records; employees can use the app without delete access.
 */
public enum Role {
    ADMIN,
    EMPLOYEE
}
