package el.selenium.services;

import org.slf4j.Logger;import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class DriverLogFileProvider {

    private final static Logger log = LoggerFactory.getLogger(DriverLogFileProvider.class);

    private static Path logDir;
    private static Path screenShotDir;
    private static Path debugScreenShotDir;

    static {
        String logDirString = "/home/davit/Wise Robots/logs";
        logDir = initDirectory(logDirString);
        String screenShotDirString = logDir + File.separator+ "bets";
        screenShotDir = initDirectory(screenShotDirString);
        String debugScreenShotDirString = logDir + File.separator + "screenshots";
        debugScreenShotDir = initDirectory(debugScreenShotDirString);
    }

    private static Path initDirectory(String directory) {
        Path path = Paths.get(directory);
        if (!Files.isDirectory(path)) {
            try {
                Files.createDirectory(path);
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            }
        }
        return path;
    }

    public static String getLogDir() {
        return logDir.toAbsolutePath().toString();
    }

    public static String getDebugScreenShotDir() {
        return debugScreenShotDir.toAbsolutePath().toString();
    }

    public static String getScreenShotDir() {
        return screenShotDir.toAbsolutePath().toString();
    }
}
