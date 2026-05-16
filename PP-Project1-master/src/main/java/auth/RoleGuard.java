package auth;

import I18n.I18nManager;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;

import java.util.Optional;

/**
 * Small helper for permission checks shared by the controllers.
 * It keeps delete-button visibility and delete warnings consistent across all screens.
 */
public final class RoleGuard {
    private RoleGuard() {
    }

    /**
     * Hides and disables delete buttons for users who are not admins.
     */
    public static void applyDeletePermission(Button deleteButton) {
        boolean canDelete = UserSession.canDelete();
        deleteButton.setDisable(!canDelete);
        deleteButton.setVisible(canDelete);
        deleteButton.setManaged(canDelete);
    }

    /**
     * Blocks delete actions that somehow get triggered without admin permission.
     */
    public static boolean confirmDeleteAllowed(I18nManager i18n) {
        if (UserSession.canDelete()) {
            return true;
        }

        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(i18n.getString("permission.denied.title"));
        alert.setHeaderText(null);
        alert.setContentText(i18n.getString("permission.denied.delete"));
        alert.showAndWait();
        return false;
    }

    /**
     * Shows the final "are you sure?" warning before a real delete happens.
     */
    public static boolean confirmDelete(I18nManager i18n, String itemName) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(i18n.getString("delete.confirm.title"));
        alert.setHeaderText(null);
        alert.setContentText(i18n.getString("delete.confirm.message", itemName));

        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }
}
