package el.selenium.services;

import com.google.common.base.Predicate;
import el.selenium.factory.WebDriverFactory;
import el.selenium.factory.WebDriverFactoryAdapter;
import el.selenium.model.*;
import el.selenium.model.storage.LocalStorageImpl;
import el.selenium.model.storage.SessionStorageImpl;
import el.selenium.utils.ElementDescriptionResolver;
import el.selenium.utils.Forms;
import el.selenium.utils.SeleniumFunctions;
import el.selenium.utils.CommandExecutor;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.html5.LocalStorage;
import org.openqa.selenium.html5.SessionStorage;
import org.openqa.selenium.interactions.Actions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.openqa.selenium.*;
import org.openqa.selenium.remote.UnreachableBrowserException;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.IntStream;

public class PageDriver implements AutoCloseable {

    private final static Logger log = LoggerFactory.getLogger(PageDriver.class);

    private final String id;

    public final static long DEFAULT_TIMEOUT = 25;

    private WebDriver driver;
    private WebDriverFactory.Browser browser;
    private ProfileStrategy profileStrategy;

    private SessionStorage sessionStorage;
    private LocalStorage localStorage;

    public PageDriver(WebDriverFactory.Browser browser, ProfileStrategy profileStrategy) {
        this(UUID.randomUUID().toString(), browser, profileStrategy);
    }

    public PageDriver(String id, WebDriverFactory.Browser browser, ProfileStrategy profileStrategy) {
        this.id = id;
        this.browser = browser;
        this.profileStrategy = profileStrategy;
        this.driver = WebDriverFactoryAdapter.getWebDriver(browser, profileStrategy);
    }

    public String getId() {
        return id;
    }

    public WebDriver getDriver() {
        return driver;
    }

    /**
     * closes current browser, and opens new one instead of it,
     * with the same strategy.
     */
    public void restartBrowser() {
        try {
            close();
        } catch (Throwable e1) {
            log.error(e1.getMessage(), e1);
        } finally {
            this.driver = WebDriverFactoryAdapter.getWebDriver(browser, profileStrategy);
            log.debug("Initializing new web driver "  + id);
        }
    }

    public ProcessedRequest goToURL(String url) {
        long startTime = System.currentTimeMillis();
        ProcessedRequest processedRequest = new ProcessedRequest(url);
        try {
            driver.get(url);
        } catch (UnreachableBrowserException e) {
            log.error(e.getMessage(), e);
            restartBrowser();
            goToURL(url);
        } catch (WebDriverException e) {
            if (e.getMessage().contains("java.io.FileNotFoundException")) {
                log.error(e.getMessage(), e);
                restartBrowser();
                goToURL(url);
            } else if (e.getMessage().contains("SyntaxError: An invalid or illegal string was specified.")) {
                log.error("SyntaxError: An Invalid initial url is: " + url);
                log.error(e.getMessage(), e);
            } else {
                log.error(e.getMessage(), e);
                restartBrowser();
                goToURL(url);
            }
        }
        long difference = System.currentTimeMillis() - startTime;
        long differenceInSeconds = TimeUnit.MILLISECONDS.convert(difference, TimeUnit.SECONDS);
        log.debug("Go to URL: {} took {} seconds", url, differenceInSeconds);
        processedRequest.setLatencyBySeconds(differenceInSeconds);
        processedRequest.setFinalURL(driver.getCurrentUrl());
        return processedRequest;
    }

    public ProcessedRequest goToURL(String url, BiFunction<PageDriver, ProcessedRequest, Boolean>... postFunction) {
        ProcessedRequest processedRequest = goToURL(url);
        Arrays.stream(postFunction)
                .filter(predicate -> predicate.apply(this, processedRequest))
                .findFirst();
        return processedRequest;
    }

    public WebElementProxy getWebElement(By cssSelector) {
        return getWebElement(new ElementDescriptorTree(cssSelector));
    }

    public WebElementProxy getWebElement(ElementDescriptorTree elementDescriptorTree) {
        try {
            return ElementDescriptionResolver.getWebElement(driver, elementDescriptorTree);
        } catch (StaleElementReferenceException e) {
            return getWebElement(elementDescriptorTree);
        } catch (InvalidSelectorException e) {
            //chrome issue
            if (e.getMessage().contains("TypeError: Failed to execute 'createNSResolver' on 'Document': parameter 1 is not of type 'Node'")) {
                return getWebElement(elementDescriptorTree);
            } else {
                throw e;
            }
        }
    }

    public List<WebElementProxy> getWebElements(By selector) {
        return ElementDescriptionResolver.getWebElements(driver, new ElementDescriptorTree(selector));
    }

    public List<WebElementProxy> getWebElements(ElementDescriptorTree elementDescriptorTree) {
        return ElementDescriptionResolver.getWebElements(driver, elementDescriptorTree);
    }

    public <R> R getElement(ElementDescriptorTree elementDescriptorTree, Function<WebElement, R> function) {
        try {
            return getElement(getWebElement(elementDescriptorTree), function);
        } catch (StaleElementReferenceException e) {
            return getElement(getWebElement(elementDescriptorTree), function);
        }
    }

    public <R> R getElement(WebElementProxy webElementProxy, Function<WebElement, R> function) {
        try {
            return function.apply(webElementProxy);
        } catch (StaleElementReferenceException e) {
            webElementProxy.setWebElementHolder(getWebElement(webElementProxy).getWebElementHolder());
            return getElement(webElementProxy, function);
        }
    }

    public WebElementProxy getWebElement(WebElementProxy parentElement, By selector) {
        try {
            return ElementDescriptionResolver.getWebElement(driver, parentElement, new ElementDescription(selector));
        } catch (StaleElementReferenceException e) {
            parentElement.setWebElementHolder(getWebElement(parentElement).getWebElementHolder());
            return getWebElement(parentElement, selector);
        }
    }

    public WebElementProxy waitWebElement(ElementDescriptorTree elementDescriptorTree, long timeout) {
        waitElement(elementDescriptorTree.getTopElementDescription().getSelector(), timeout);
        return getWebElement(elementDescriptorTree);
    }

    public WebElementProxy waitWebElement(WebElementProxy parentElement, By selector, long timeout) {
        try {
            (new WebDriverWait(driver, timeout))
                    .until((Predicate<WebDriver>) driver1 -> !parentElement.findElements(selector).isEmpty());
            return ElementDescriptionResolver.getWebElement(driver, parentElement, new ElementDescription(selector));
        } catch (StaleElementReferenceException e) {
            parentElement.setWebElementHolder(waitWebElement(parentElement, timeout).getWebElementHolder());
            return waitWebElement(parentElement, selector, timeout);
        }
    }

    public List<WebElementProxy> getWebElements(WebElementProxy parentElement, By selector) {
        try {
            return ElementDescriptionResolver.getWebElements(driver, parentElement, new ElementDescription(selector));
        } catch (StaleElementReferenceException e) {
            parentElement.setWebElementHolder(getWebElement(parentElement).getWebElementHolder());
            return getWebElements(parentElement, selector);
        }
    }

    public <R> R getAttribute(ElementDescriptorTree elementDescriptorTree, BiFunction<WebElement, String, R> function, String attributeName) {
        try {
            WebElementProxy webElement = getWebElement(elementDescriptorTree);
            return getAttribute(webElement, function, attributeName);
        } catch (StaleElementReferenceException e) {
            return getAttribute(elementDescriptorTree, function, attributeName);
        }
    }

    public <R> R getAttribute(By selector, Function<WebElement, R> function) {
        try {
            WebElementProxy webElement = ElementDescriptionResolver.getWebElement(driver, new ElementDescription(selector));
            return function.apply(webElement);
        } catch (StaleElementReferenceException e) {
            return getAttribute(selector, function);
        }
    }

    public <R> R getAttribute(WebElementProxy webElementProxy, Function<WebElement, R> function) {
        try {
            return function.apply(webElementProxy);
        } catch (StaleElementReferenceException e) {
            webElementProxy.setWebElementHolder(getWebElement(webElementProxy).getWebElementHolder());
            return getAttribute(webElementProxy, function);
        }
    }

    public <R> R getAttribute(WebElementProxy webElementProxy, BiFunction<WebElement, String, R> function, String attributeName) {
        try {
            return function.apply(webElementProxy, attributeName);
        } catch (StaleElementReferenceException e) {
            webElementProxy.setWebElementHolder(getWebElement(webElementProxy).getWebElementHolder());
            return getAttribute(webElementProxy, function, attributeName);
        }
    }

    public <R> R getAttribute(WebElementProxy parentElement, By selector, BiFunction<WebElement, String, R> function, String attributeName) {
        return getAttribute(parentElement, new ElementDescriptorTree(selector), function, attributeName);
    }

    public <R> R getAttribute(WebElementProxy parentElement, ElementDescriptorTree elementDescriptorTree, BiFunction<WebElement, String, R> function, String attributeName) {
        try {
            WebElementProxy webElement = ElementDescriptionResolver.getWebElement(driver, parentElement, elementDescriptorTree);
            return function.apply(webElement, attributeName);
        } catch (StaleElementReferenceException e) {
            parentElement.setWebElementHolder(getWebElement(parentElement).getWebElementHolder());
            return getAttribute(parentElement, elementDescriptorTree, function, attributeName);
        }
    }

    public <R> R getAttribute(WebElementProxy parentElement, By selector, Function<WebElement, R> function) {
        return getAttribute(parentElement, new ElementDescriptorTree(selector), function);
    }

    public <R> R getAttribute(WebElementProxy parentElement, ElementDescriptorTree elementDescriptorTree, Function<WebElement, R> function) {
        try {
            WebElementProxy webElement = ElementDescriptionResolver.getWebElement(driver, parentElement, elementDescriptorTree);
            return function.apply(webElement);
        } catch (StaleElementReferenceException e) {
            parentElement.setWebElementHolder(getWebElement(parentElement).getWebElementHolder());
            return getAttribute(parentElement, elementDescriptorTree, function);
        }
    }


    public String getText(By cssSelector) {
        return getElement(new ElementDescriptorTree(cssSelector), SeleniumFunctions::getText);
    }

    public void mouseHover(WebElement webElement) {
        Actions builder = new Actions(driver);
        WebElement cleanWebElement = webElement;
        if (webElement instanceof WebElementProxy) {
            cleanWebElement = ((WebElementProxy) webElement).getWebElement();
        }
        Actions hoverOverRegistrar = builder.moveToElement(cleanWebElement);
        hoverOverRegistrar.perform();
    }

    public WebDriver switchToFrame(String iframeID) {
        return switchToFrame(iframeID, DEFAULT_TIMEOUT);
    }

    public WebDriver switchToFrame(String iframeID, long timeOut) {
        return waitCondition(ExpectedConditions.frameToBeAvailableAndSwitchToIt(iframeID), timeOut);
    }

    public WebDriver switchToFrame(String iframeID, long timeOut, boolean switchDefaultBefore) {
        if(switchDefaultBefore) {
            switchToDefaultContent();
        }
        return waitCondition(ExpectedConditions.frameToBeAvailableAndSwitchToIt(iframeID), timeOut);
    }

    public WebDriver switchToDefaultContent() {
        return driver.switchTo().defaultContent();
    }

    public WebElementProxy getElementFromIframe(String iframeID, Element element) {
        switchToFrame(iframeID);
        WebElement foundElement = driver.findElement(element.getBySelector());
        driver.switchTo().defaultContent();
        return new WebElementProxy(foundElement, element.getBySelector());
    }

    public boolean isElementDisplayed(By selector) {
        try {
            List<WebElement> elements = driver.findElements(selector);
            return !elements.isEmpty() && elements.get(0).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isElementDisplayedWithTimeout(By cssElement) {
        return isElementDisplayedWithTimeout(cssElement, DEFAULT_TIMEOUT);
    }

    public boolean isElementDisplayedWithTimeout(By element, long timeout) {
        try {
            WebElement webElement = waitElement(element, timeout);
            return webElement.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isElementDisplayed(WebElementProxy parentElement, By by) {
        try {
            WebElement webElement = parentElement.findElement(by);
            return webElement.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    public WebElementProxy getParent(WebElementProxy webElement) {
        return getWebElement(webElement, By.xpath(".."));
    }

    public void makeVisible(WebElement webElement) {
        String js = "arguments[0].style.display='block'; arguments[0].style.visibility='visible'; arguments[0].style.height = '1px'; arguments[0].style.width = '1px'; arguments[0].style.opacity = 1;";
        executeScript(webElement, js);
    }

    public void makeVisible(String cssSelector) {
        JavascriptExecutor executor = (JavascriptExecutor) driver;
        executor.executeScript("document.querySelector('" + cssSelector + "').style.display='block';");
        executor.executeScript("document.querySelector('" + cssSelector + "').style.visibility='visible';");
    }

    public void executeScript(String js) {
        ((JavascriptExecutor) driver).executeScript(js);
    }

    public void executeScript(WebElement webElement, String js) {
        if (webElement instanceof WebElementProxy) {
            ((JavascriptExecutor) driver).executeScript(js, (WebElement) ((WebElementProxy) webElement).getWebElementHolder());
        } else {
            ((JavascriptExecutor) driver).executeScript(js, webElement);
        }
    }

    public WebElementProxy waitElement(By bySelector) {
        WebElement webElement = this.waitCondition(ExpectedConditions.visibilityOfElementLocated(bySelector));
        return new WebElementProxy(webElement, bySelector);
    }

    public WebElementProxy waitElement(By bySelector, long timeout) {
        WebElement webElement = this.waitCondition(ExpectedConditions.visibilityOfElementLocated(bySelector), timeout);
        return new WebElementProxy(webElement, bySelector);
    }

    public List<WebElementProxy> waitElements(By bySelector) {
        this.waitCondition(ExpectedConditions.visibilityOfElementLocated(bySelector));
        return this.getWebElements(new ElementDescriptorTree(bySelector));
    }

    public String waitText(By bySelector) {
        return waitText(bySelector, DEFAULT_TIMEOUT);
    }

    public String waitText(By selector, long timeout) {
        try {
            return this.waitCondition(ExpectedConditions.visibilityOfElementLocated(selector), timeout).getText();
        } catch (StaleElementReferenceException e) {
            log.warn("Attempting to recover from StaleElementReferenceException ..." + selector);
            return waitText(selector, timeout);
        } catch (InvalidSelectorException e) {
            //chrome issue
            if (e.getMessage().contains("TypeError: Failed to execute 'createNSResolver' on 'Document': parameter 1 is not of type 'Node'")) {
                return waitText(selector);
            } else {
                throw e;
            }
        }
    }

    public <T> T waitCondition(ExpectedCondition<T> expectedCondition) {
        return waitCondition(expectedCondition, DEFAULT_TIMEOUT);
    }

    public <T> T waitCondition(ExpectedCondition<T> expectedCondition, long timeout) {
        try {
            return (new WebDriverWait(driver, timeout)).until(expectedCondition);
        } catch (StaleElementReferenceException e) {
            log.warn("Attempting to recover from StaleElementReferenceException ..." + expectedCondition);
            return waitCondition(expectedCondition, timeout);
        } catch (InvalidSelectorException e) {
            //chrome issue
            if (e.getMessage().contains("TypeError: Failed to execute 'createNSResolver' on 'Document': parameter 1 is not of type 'Node'")) {
                return waitCondition(expectedCondition, timeout);
            } else {
                throw e;
            }
        }
    }


    public <R> R waitElement(By selector, BiFunction<WebElement, String, R> function, String attributeName) {
        try {
            WebElementProxy webElementProxy = waitElement(selector);
            return function.apply(webElementProxy, attributeName);
        } catch (StaleElementReferenceException e) {
            log.warn("Attempting to recover from StaleElementReferenceException for selector {} ... ", selector);
            return waitElement(selector, function, attributeName);
        } catch (InvalidSelectorException e) {
            //chrome issue
            if (e.getMessage().contains("TypeError: Failed to execute 'createNSResolver' on 'Document': parameter 1 is not of type 'Node'")) {
                return waitElement(selector, function, attributeName);
            } else {
                throw e;
            }
        }
    }

    public void click(By cssSelector) {
        WebElementProxy webElement = getWebElement(cssSelector);
        click((WebElement) webElement);
    }

    public void click(ElementDescriptorTree elementDescriptorTree) {
        click((WebElement) getWebElement(elementDescriptorTree));
    }

    public void clickIfPresent(By selector) {
        if (isElementDisplayed(selector)) {
            click(selector);
        }
    }

    public void click(WebElement webElement) {
        try {
            webElement.click();
        } catch (Exception e) {
            try {
                Actions actions = new Actions(driver);
                WebElement cleanWebElement = webElement;
                if (webElement instanceof WebElementProxy) {
                    cleanWebElement = ((WebElementProxy) webElement).getWebElement();
                }
                actions.moveToElement(cleanWebElement).click().perform();
            } catch (StaleElementReferenceException e1) {
                if (webElement instanceof WebElementProxy) {
                    click((ElementDescriptorTree) webElement);
                } else {
                    throw e1;
                }
            }
        }
    }

    public void takeScreenShot(String dirName, String fileName) {
        try {
//            fileName = StringUtils.remove(fileName, "/");
            fileName = StringUtils.replace(fileName, "/", "\\");
            if (fileName.length() > 220) {
                fileName = fileName.substring(0, 220);
            }
            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd.hh.mm");
            String dateTmeFormat = LocalDateTime.now().format(dateTimeFormatter);
            fileName += ".date." + dateTmeFormat;
            FileOutputStream out = new FileOutputStream(dirName + "/" + fileName + "_d.png");
            byte[] screenshotAs = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
            out.write(screenshotAs);
            out.close();

            //and now save the page
            savePage(new File(dirName + "/" + fileName + ".html"));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            // No need to crash the tests if the screenshot fails
        }
    }

    public void stopLoading() {
        try {
            log.debug("Stopping page loading in page " + driver.getCurrentUrl());
            driver.findElement(By.tagName("body")).sendKeys(Keys.ESCAPE);
        } catch (StaleElementReferenceException e) {
            stopLoading();
        } catch (Exception e) {
            //ignore
        }
        executeScript("return window.stop");
    }

    public void savePage(File file) {
        FileWriter writer = null;
        try {
            writer = new FileWriter(file, true);
            writer.write(driver.getPageSource());
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    log.error(e.getMessage(), e);
                }
            }
        }
    }

    public Forms formHelper() {
        return new Forms(this);
    }

    public SessionStorage getSessionStorage() {
        if (sessionStorage == null) {
            sessionStorage = new SessionStorageImpl(driver);
        }
        return sessionStorage;
    }

    public LocalStorage getLocalStorage() {
        if (localStorage == null) {
            localStorage = new LocalStorageImpl(driver);
        }
        return localStorage;
    }

    public void cleanAllCookies() {
        driver.manage().deleteAllCookies();
    }

    public void cleanFullStorage() {
        IntStream.range(0, 2).forEach(i -> {
                    CommandExecutor.executeOnIsolation(this::cleanAllCookies);
                    CommandExecutor.executeOnIsolation(() -> getLocalStorage().clear());
                    CommandExecutor.executeOnIsolation(() -> getSessionStorage().clear());
                }
        );
    }

    public String getPageInfo() {
        return "PageInfo {" +
                "URL = " + driver.getCurrentUrl() +
                ", Title = title" + driver.getTitle();
    }

    @Override
    public void close() {
        driver.close();
        driver.quit();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PageDriver that = (PageDriver) o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
