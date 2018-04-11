package el.selenium.exceptions;

public class RuntimeExecutionException extends RuntimeException {

    public RuntimeExecutionException(String message, Throwable cause) {
        super(message, cause);
    }

    public RuntimeExecutionException(Throwable cause) {
        super(cause);
    }

    public RuntimeExecutionException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
