package el.selenium.utils;

import el.selenium.exceptions.RuntimeExecutionException;

import java.util.Arrays;
import java.util.Optional;

public class CommandExecutor {

    public static void executeOnIsolation(Command command) {
        try {
            command.execute();
        } catch (Throwable e) {
            //skip error
        }
    }

    public static void execute(Command command, int retryCount) throws RuntimeExecutionException {
        Optional<Exception> lastException = Optional.empty();
        for (int i = 0; i < retryCount; i++) {
            try {
                command.execute();
                return;
            } catch (Exception e) {
                lastException = Optional.of(e);
            }
        }

        if(lastException.isPresent()) {
            throw new RuntimeExecutionException(lastException.get());
        }
    }

    public static void execute(Command command, int retryCount, Throwable... exceptions) throws RuntimeExecutionException {
        Optional<Throwable> lastException = Optional.empty();
        for (int i = 0; i < retryCount; i++) {
            try {
                command.execute();
                return;
            } catch (Throwable e) {
                boolean exceptionMatched = Arrays.stream(exceptions)
                        .anyMatch(exception -> exception.getClass().isAssignableFrom(e.getClass()));
                if(exceptionMatched) {
                    lastException = Optional.of(e);
                } else {
                    throw e;
                }
            }
        }

        if(lastException.isPresent()) {
            throw new RuntimeExecutionException(lastException.get());
        }
    }

    public interface Command {
        void execute();
    }
}
