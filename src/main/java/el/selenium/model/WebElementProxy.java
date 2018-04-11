package el.selenium.model;

import org.openqa.selenium.*;

import java.util.List;
import java.util.stream.Collectors;

public class WebElementProxy extends ElementDescriptorTree implements WebElement {

    /** This is the real web element, which wrappers WebElementProxy **/
    private WebElement webElementHolder;

    public WebElementProxy(WebElement webElementHolder, By selector) {
        super(selector);
        this. webElementHolder = webElementHolder;
    }

    public WebElementProxy(WebElement webElementHolder, ElementDescription elementDescription) {
        super(elementDescription);
        this. webElementHolder = webElementHolder;
    }

    public WebElementProxy(WebElement webElementHolder, ElementDescriptorTree elementDescriptorTree) {
        super(elementDescriptorTree);
        this.webElementHolder = webElementHolder;
    }

    public void setWebElementHolder(WebElement webElement) {
        webElementHolder = webElement;
    }

    public WebElement getWebElementHolder() {
        return webElementHolder;
    }

    public WebElement getWebElement() {
        WebElement webElement = webElementHolder;
        while (webElement instanceof WebElementProxy) {
            webElement = ((WebElementProxy) webElement).getWebElementHolder();
        }
        return webElement;
    }

    @Override
    public void click() {
        webElementHolder.click();
    }

    @Override
    public void submit() {
        webElementHolder.submit();
    }

    @Override
    public void sendKeys(CharSequence... charSequences) {
        webElementHolder.sendKeys(charSequences);
    }

    @Override
    public void clear() {
        webElementHolder.clear();
    }

    @Override
    public String getTagName() {
        return webElementHolder.getTagName();
    }

    @Override
    public String getAttribute(String s) {
        return webElementHolder.getAttribute(s);
    }

    @Override
    public boolean isSelected() {
        return webElementHolder.isSelected();
    }

    @Override
    public boolean isEnabled() {
        return webElementHolder.isEnabled();
    }

    @Override
    public String getText() {
        return webElementHolder.getText();
    }

    @Override
    public List<WebElement> findElements(By by) {
        return webElementHolder.findElements(by)
                .stream()
                .map(webElement -> new WebElementProxy(webElement, by))
                .collect(Collectors.toList());
    }

    @Override
    public WebElementProxy findElement(By by) {
        WebElement webElement = webElementHolder.findElement(by);
        return new WebElementProxy(webElement, by);
    }

    @Override
    public boolean isDisplayed() {
        return webElementHolder.isDisplayed();
    }

    @Override
    public Point getLocation() {
        return webElementHolder.getLocation();
    }

    @Override
    public Dimension getSize() {
        return webElementHolder.getSize();
    }

    @Override
    public Rectangle getRect() {
        return webElementHolder.getRect();
    }

    @Override
    public String getCssValue(String s) {
        return webElementHolder.getCssValue(s);
    }

    @Override
    public <X> X getScreenshotAs(OutputType<X> target) throws WebDriverException {
        return webElementHolder.getScreenshotAs(target);
    }
}
