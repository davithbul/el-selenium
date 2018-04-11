package el.selenium.model;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

public enum ElementType {
    input {
        @Override
        public void fillValue(WebElement webElement, String value) {
            webElement.sendKeys(value);
        }
    },
    select {
        @Override
        public void fillValue(WebElement webElement, String value) {
            Select select = new Select(webElement);
            if(value.startsWith("index:")) {
                value = value.substring(6);
                select.selectByIndex(Integer.parseInt(value));
            } else {
                select.selectByValue(value);
            }
        }
    },
    radio {
        @Override
        public void fillValue(WebElement webElement, String value) {
            webElement.click();
        }
    };

    public abstract void fillValue(WebElement webElement, String value);
}
