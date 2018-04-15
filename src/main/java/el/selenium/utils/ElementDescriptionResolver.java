package el.selenium.utils;

import com.google.common.base.Preconditions;
import el.selenium.model.ElementDescription;
import el.selenium.model.ElementDescriptorTree;
import el.selenium.model.ListElementDescription;
import el.selenium.model.WebElementProxy;
import el.selenium.drivers.page.PageDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ElementDescriptionResolver {

    public static WebElementProxy getWebElement(WebDriver driver, ElementDescription elementDescription) {
        if (elementDescription instanceof ListElementDescription) {
            return getWebElement(driver, (ListElementDescription) elementDescription);
        }
        WebElement webElement = driver.findElement(elementDescription.getSelector());
        return new WebElementProxy(webElement, elementDescription);
    }

    public static WebElementProxy getWebElement(WebDriver driver, ListElementDescription elementDescription) {
        List<WebElement> elements = driver.findElements(elementDescription.getSelector());
        if (elements.size() < elementDescription.getIndex()) {
            waitElement(driver, elementDescription.getSelector(), PageDriver.DEFAULT_TIMEOUT);
            elements = driver.findElements(elementDescription.getSelector());
        }
        WebElement webElement = elements.get(elementDescription.getIndex());
        return new WebElementProxy(webElement, elementDescription);
    }

    public static WebElementProxy getWebElement(WebDriver driver, ElementDescriptorTree elementDescriptorTree) {
        return getWebElement(driver, null, elementDescriptorTree);
    }

    public static WebElementProxy getWebElement(WebDriver driver, WebElementProxy parentElement, ElementDescriptorTree elementDescriptorTree) {
        WebElementProxy webElement = parentElement;
        for (ElementDescription elementDescription : elementDescriptorTree.getElementDescriptions()) {
            webElement = ElementDescriptionResolver.getWebElement(driver, webElement, elementDescription);
        }
        return webElement;
    }

    public static WebElementProxy getWebElement(WebDriver driver, WebElementProxy parentElement, ElementDescription elementDescription) {
        if (parentElement == null) {
            return getWebElement(driver, elementDescription);
        }

        WebElement webElement;
        if (elementDescription instanceof ListElementDescription) {
            List<WebElement> elements = parentElement.findElements(elementDescription.getSelector());
            if(elements.size() < ((ListElementDescription) elementDescription).getIndex()) {
                //unique case waiting few seconds
                waitElement(driver, elementDescription.getSelector(), PageDriver.DEFAULT_TIMEOUT);
                elements = parentElement.findElements(elementDescription.getSelector());
            }
            webElement = elements.get(((ListElementDescription) elementDescription).getIndex());
        } else {
            webElement = parentElement.findElement(elementDescription.getSelector());
        }
        ElementDescriptorTree elementDescriptorTree = new ElementDescriptorTree(parentElement, elementDescription);
        return new WebElementProxy(webElement, elementDescriptorTree);
    }

    public static List<WebElementProxy> getWebElements(WebDriver driver, ElementDescriptorTree elementDescriptorTree) {
        List<WebElementProxy> webElements = null;
        WebElementProxy parentElement = null;
        Iterator<ElementDescription> iterator = elementDescriptorTree.getElementDescriptions().iterator();
        while (iterator.hasNext()) {
            ElementDescription elementDescription = iterator.next();
            if (!iterator.hasNext()) {
                //last element
                webElements = getWebElements(driver, parentElement, elementDescription);
            } else {
                parentElement = getWebElement(driver, parentElement, elementDescription);
            }
        }

        return webElements;
    }

    public static List<WebElementProxy> getWebElements(WebDriver driver, WebElementProxy parentElement, ElementDescription elementDescription) {
        Preconditions.checkArgument(!(elementDescription instanceof ListElementDescription));
        List<WebElement> webElements = parentElement != null ?
                parentElement.findElements(elementDescription.getSelector()) :
                driver.findElements(elementDescription.getSelector());

        return IntStream.range(0, webElements.size())
                .mapToObj(i -> {
                    WebElement webElement = webElements.get(i);
                    ListElementDescription listElementDescription = new ListElementDescription(elementDescription.getSelector(), i);
                    ElementDescriptorTree elementDescriptorTree = parentElement != null ?
                            new ElementDescriptorTree(parentElement, listElementDescription) :
                            new ElementDescriptorTree(listElementDescription);
                    return new WebElementProxy(webElement, elementDescriptorTree);
                }).collect(Collectors.toList());
    }

    public static void waitElement(WebDriver driver, By bySelector, long timeout) {
        (new WebDriverWait(driver, timeout))
                .until(ExpectedConditions.visibilityOfElementLocated(bySelector));
    }
}
