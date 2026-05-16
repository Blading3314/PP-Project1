package auth;

import java.util.Optional;

/**
 * Login contract for checking user credentials.
 * Returning a role keeps the login screen separate from permission decisions.
 */
public interface UserAccountDAO {
    Optional<Role> authenticate(String username, String password);
}
