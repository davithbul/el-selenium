package el.selenium.drivers.strategies;


import java.util.Collections;
import java.util.Map;

public class WebDriverDefaultStrategy implements WebDriverStrategy {

    @Override
    public Map<String, Object> getPreferences() {
        return Collections.emptyMap();
    }
}
