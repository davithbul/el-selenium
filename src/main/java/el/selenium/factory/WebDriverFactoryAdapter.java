package el.selenium.factory;

import el.selenium.drivers.strategies.WebDriverStrategy;
import el.selenium.drivers.strategies.WebDriverStrategyProvider;
import el.selenium.model.ProfileStrategy;
import org.openqa.selenium.WebDriver;

import static el.selenium.factory.WebDriverFactory.Browser;

public class WebDriverFactoryAdapter {

    public static WebDriver getWebDriver(Browser browser, ProfileStrategy profileStrategy) {
        WebDriverStrategy strategy = WebDriverStrategyProvider.getStrategy(profileStrategy);
        return WebDriverFactory.getDriver(browser, strategy.getPreferences());
    }
}
