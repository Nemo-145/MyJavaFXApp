package exceptions;

/**
 * Исключение, вызываемое при ошибке переключения сцены,
 * например, если FXML-файл не найден.
 */
public class SceneSwitchException extends Exception {

    public SceneSwitchException(String message) {
        super(message);
    }

    public SceneSwitchException(String message, Throwable cause) {
        super(message, cause);
    }
}
