package el.selenium.drivers.strategies;


import el.selenium.model.ProfileStrategy;

public class WebDriverStrategyProvider {

    private static WebDriverDefaultStrategy webDriverDefaultStrategy = new WebDriverDefaultStrategy();
    private static NonJsWebDriverStrategy nonJsWebDriverStrategy = new NonJsWebDriverStrategy();

    public static WebDriverStrategy getStrategy(ProfileStrategy profileStrategy) {
        WebDriverStrategy webDriverStrategy = null;
        switch (profileStrategy) {
            case DEFAULT:
                webDriverStrategy = webDriverDefaultStrategy;
                break;
            case NON_JS:
                webDriverStrategy = nonJsWebDriverStrategy;
                break;
            default:
                new RuntimeException("WebDriver strategy hasn't been found for profile: " + profileStrategy);
        }

        return webDriverStrategy;
    }
}
