package el.selenium.utils;

import el.selenium.model.*;
import el.selenium.drivers.page.PageDriver;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class Forms {

    private final static Logger log = LoggerFactory.getLogger(Forms.class);

    private PageDriver pageDriver;

    public Forms(PageDriver pageDriver) {
        this.pageDriver = pageDriver;
    }

    public void fillData(String selector, String value) {
        fillData(new ElementData(selector, value));
    }

    public void fillData(String selector, String value, SelectorType selectorType) {
        fillData(new ElementData(selector, value, selectorType));
    }

    public void fillData(String selector, String value, SelectorType selectorType, ElementType elementType) {
        fillData(new ElementData(selector, value, selectorType, elementType));
    }

    public void fillData(ElementData elementData) {
        try {
            By bySelector = elementData.getSelectorType().getBySelector(elementData.getSelector());

            List<WebElementProxy> webElements;
            if (elementData.isMultiple()) {
                webElements = pageDriver.getWebElements(bySelector);
            } else {
                webElements = Collections.singletonList(pageDriver.getWebElement(bySelector));
            }

            for (WebElementProxy webElement : webElements) {
                pageDriver.makeVisible(webElement);
                elementData.getElementType().fillValue(webElement, elementData.getValue());
            }
        } catch (NoSuchElementException e) {
            log.error(e.getMessage());
            throw new RuntimeException("element " + elementData.getSelectorType() + " with name " + elementData.getSelector() + " hasn't been found");
        }
    }

    public void fillData(ArrayList<ElementData> elementDataList) {
        for (ElementData elementData : elementDataList) {
            fillData(elementData);
        }
    }

    public Map<String, String> getRequestAttributes() {
        Map<String, String> requestAttributes = new LinkedHashMap<>();
        String currentUrl = pageDriver.getDriver().getCurrentUrl();
        int cutIndex = currentUrl.indexOf("?");
        if (cutIndex != -1 && (cutIndex + 1) < currentUrl.length()) {
            String requestAttributesString = currentUrl.substring(cutIndex + 1);
            String[] parameters = StringUtils.split(requestAttributesString, '&');
            for (String parameter : parameters) {
                String name;
                String value = null;

                int index = parameter.indexOf('=');
                if (index != -1) {
                    name = parameter.substring(0, index);
                    if ((index + 1) < parameter.length()) {
                        value = parameter.substring(index + 1);
                    }
                } else {
                    name = parameter;
                }

                requestAttributes.put(name, value);
            }
        }
        return requestAttributes;
    }
}
