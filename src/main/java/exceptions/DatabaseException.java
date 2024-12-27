package exceptions;

/**
 * Исключение, выбрасываемое при ошибках в работе с БД.
 */
public class DatabaseException extends Exception {

    public DatabaseException(String message) {
        super(message);
    }

    public DatabaseException(String message, Throwable cause) {
        super(message, cause);
    }
}
