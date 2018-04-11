package el.selenium.model;

import org.openqa.selenium.By;

public enum SelectorType {
    className {
        @Override
        public By getBySelector(String selector) {
            if(selector.startsWith(".")) {
                selector = selector.substring(1);
            }
            return By.className(selector);
        }
    },
    cssSelector {
        @Override
        public By getBySelector(String selector) {
            return By.cssSelector(selector);
        }
    },
    id {
        @Override
        public By getBySelector(String selector) {
            if(selector.startsWith("#")) {
                selector = selector.substring(1);
            }
            return By.id(selector);
        }
    },
    linkText {
        @Override
        public By getBySelector(String selector) {
            return By.linkText(selector);
        }
    },
    name {
        @Override
        public By getBySelector(String selector) {
            return By.name(selector);
        }
    },
    partialLinkText {
        @Override
        public By getBySelector(String selector) {
            return By.partialLinkText(selector);
        }
    },
    tagName {
        @Override
        public By getBySelector(String selector) {
            return By.tagName(selector);
        }
    },
    xPath {
        @Override
        public By getBySelector(String selector) {
            return By.xpath(selector);
        }
    };
    
    public abstract By getBySelector(String selector);
}
