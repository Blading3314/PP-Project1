package util;

import java.sql.SQLException;

/**
 * App-level wrapper for database failures.
 * DAOs throw this so controllers can show useful alerts instead of silently printing SQL errors.
 */
public class DatabaseException extends RuntimeException {
    public enum Kind {
        CONNECTION,
        CONSTRAINT,
        NO_CHANGE,
        GENERAL
    }

    private final String operation;
    private final Kind kind;

    /**
     * Wraps a real SQL exception and classifies it for a friendly alert.
     */
    public DatabaseException(String operation, SQLException cause) {
        super(operation, cause);
        this.operation = operation;
        this.kind = classify(cause);
    }

    /**
     * Creates an app-level database error when there is no SQL exception, such as zero rows changed.
     */
    public DatabaseException(String operation, Kind kind) {
        super(operation);
        this.operation = operation;
        this.kind = kind;
    }

    /**
     * Describes what the app was trying to do when the database failed.
     */
    public String getOperation() {
        return operation;
    }

    /**
     * Chooses the translated message that best matches the failure type.
     */
    public String getMessageKey() {
        return switch (kind) {
            case CONNECTION -> "alert.database.connection";
            case CONSTRAINT -> "alert.database.constraint";
            case NO_CHANGE -> "alert.database.no.change";
            case GENERAL -> "alert.database.general";
        };
    }

    /**
     * Groups low-level SQLite messages into simpler categories for the UI.
     */
    private static Kind classify(SQLException cause) {
        String message = cause.getMessage() == null ? "" : cause.getMessage().toLowerCase();
        String state = cause.getSQLState() == null ? "" : cause.getSQLState();
        if (message.contains("constraint") || state.startsWith("23")) {
            return Kind.CONSTRAINT;
        }
        if (message.contains("no such table") || message.contains("unable to open database")
                || message.contains("database is locked")) {
            return Kind.CONNECTION;
        }
        return Kind.GENERAL;
    }
}
