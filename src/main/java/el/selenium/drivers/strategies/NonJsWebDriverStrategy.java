package el.selenium.drivers.strategies;


import java.util.LinkedHashMap;
import java.util.Map;

public class NonJsWebDriverStrategy implements WebDriverStrategy {

    private final static Map<String, Object> PREFERENCES = new LinkedHashMap<>();

    static {
        PREFERENCES.put("javascript.enabled", false);
    }

    @Override
    public Map<String, Object> getPreferences() {
        return PREFERENCES;
    }
}
