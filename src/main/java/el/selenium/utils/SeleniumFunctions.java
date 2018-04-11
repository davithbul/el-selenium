package el.selenium.utils;

import org.openqa.selenium.WebElement;

public class SeleniumFunctions {

    public static String getAttribute(WebElement webElement, String attributeName) {
        return webElement.getAttribute(attributeName);
    }

    public static String getText(WebElement webElement) {
        return webElement.getText();
    }
}
