package el.selenium.factory;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import el.selenium.drivers.ExtendedHtmlUnitDriver;
import org.openqa.selenium.Platform;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.firefox.internal.ProfilesIni;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriverService;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class WebDriverFactory {

    public static WebDriver getDriver(Browser browser, Map<String, Object> preferences) {
        return browser.getDriver(preferences);
    }

    public enum Browser {
        CHROME {
            @Override
            WebDriver getDriver(Map<String, Object> preferences) {
                if (preferences.containsKey("webdriver.chrome.driver")) {
                    System.setProperty("webdriver.chrome.driver", String.valueOf(preferences.get("webdriver.chrome.driver")));
                }
                DesiredCapabilities capabilities = DesiredCapabilities.chrome();
                capabilities.setPlatform(Platform.WINDOWS);
                capabilities.setCapability(CapabilityType.ForSeleniumServer.ENSURING_CLEAN_SESSION, true);
                capabilities.setCapability(CapabilityType.SUPPORTS_APPLICATION_CACHE, false);
                capabilities.setCapability(CapabilityType.SUPPORTS_SQL_DATABASE, false);
                capabilities.setCapability("download.default_directory", "/var/data");

                for (Map.Entry<String, Object> preference : preferences.entrySet()) {
                    Object value = preference.getValue();
                    if (value instanceof Boolean) {
                        capabilities.setCapability(preference.getKey(), (boolean) value);
                    } else if (value instanceof Integer) {
                        capabilities.setCapability(preference.getKey(), value);
                    } else {
                        capabilities.setCapability(preference.getKey(), String.valueOf(value));
                    }
                }

                capabilities.setPlatform(Platform.WINDOWS);

                ChromeOptions chromeOptions = new ChromeOptions();
                chromeOptions.addArguments(Arrays.asList(
                        "--start-maximized",
                        "--test-type",
                        "--disable-impl-side-painting"
                ));

                capabilities.setCapability(ChromeOptions.CAPABILITY, chromeOptions);
                return new ChromeDriver(capabilities);
            }
        },
        FIREFOX {
            @Override
            WebDriver getDriver(Map<String, Object> preferences) {
                FirefoxProfile profile = new ProfilesIni().getProfile("default");
                profile.setPreference("intl.accept_languages", "en-US, en");
                profile.setPreference("font.language.group", "x-western");
                profile.setPreference("services.sync.prefs.sync.intl.accept_languages", true);

                profile.setPreference("browser.cache.disk.enable", false);
                profile.setPreference("browser.cache.memory.enable", false);
                profile.setPreference("browser.cache.offline.enable", false);
                profile.setPreference("network.http.use-cache", false);

                for (Map.Entry<String, Object> preference : preferences.entrySet()) {
                    Object value = preference.getValue();
                    if (value instanceof Boolean) {
                        profile.setPreference(preference.getKey(), (boolean) value);
                    } else if (value instanceof Integer) {
                        profile.setPreference(preference.getKey(), (int) value);
                    } else {
                        profile.setPreference(preference.getKey(), String.valueOf(value));
                    }
                }


                DesiredCapabilities desiredCapabilities = DesiredCapabilities.firefox();
                desiredCapabilities.setCapability(FirefoxDriver.PROFILE, profile);
                desiredCapabilities.setPlatform(Platform.WINDOWS);
                desiredCapabilities.setCapability(CapabilityType.ForSeleniumServer.ENSURING_CLEAN_SESSION, true);
                desiredCapabilities.setCapability(CapabilityType.SUPPORTS_APPLICATION_CACHE, false);
                desiredCapabilities.setCapability(CapabilityType.SUPPORTS_SQL_DATABASE, false);
                return new FirefoxDriver(desiredCapabilities);
            }
        },
        HTML_UNIT {
            @Override
            WebDriver getDriver(Map<String, Object> preferences) {
                BrowserVersion browser = BrowserVersion.FIREFOX_45;
                HtmlUnitDriver htmlUnitDriver = new ExtendedHtmlUnitDriver(browser);
                htmlUnitDriver.manage().timeouts().pageLoadTimeout(150, TimeUnit.SECONDS);
                htmlUnitDriver.manage().timeouts().setScriptTimeout(150, TimeUnit.SECONDS);
                if (preferences.containsKey("javascript.enabled") && !(boolean) preferences.get("javascript.enabled")) {
                    htmlUnitDriver.setJavascriptEnabled(false);
                } else {
                    htmlUnitDriver.setJavascriptEnabled(true);
                }

                return htmlUnitDriver;
            }
        },
        PHANTOM_JS {
            @Override
            WebDriver getDriver(Map<String, Object> preferences) {
                // prepare capabilities
                DesiredCapabilities capabilities = new DesiredCapabilities();
                capabilities.setCapability(
                        PhantomJSDriverService.PHANTOMJS_EXECUTABLE_PATH_PROPERTY,
                        preferences.getOrDefault(PhantomJSDriverService.PHANTOMJS_EXECUTABLE_PATH_PROPERTY, "/bin/phantomjs")
                );

                capabilities.setJavascriptEnabled(true);                //< not really needed: JS enabled by default
                capabilities.setCapability("takesScreenshot", true);    //< yeah, GhostDriver haz screenshots!
                capabilities.setCapability("phantomjs.page.settings.userAgent", "Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:25.0) Gecko/20100101 Firefox/25.0");

                // Launch driver (will take care and ownership of the phantomjs process)
                return new PhantomJSDriver(capabilities);
            }
        };

        abstract WebDriver getDriver(Map<String, Object> preferences);

        public static Browser getDefault() {
            return FIREFOX;
        }
    }
}
