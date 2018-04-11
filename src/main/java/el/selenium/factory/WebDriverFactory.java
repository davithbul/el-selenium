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
        WebDriver driver = browser.getDriver(preferences);
//        driver.manage().timeouts().pageLoadTimeout(200, TimeUnit.SECONDS);
//        driver.manage().timeouts().setScriptTimeout(200, TimeUnit.SECONDS);
//        driver.manage().timeouts().implicitlyWait(200, TimeUnit.SECONDS);
        // See: http://kb.mozillazine.org/About:config_entries for a complete list of profile settings.
        //profile.setPreference("browser.link.open_newwindow.restriction", 1);
        //profile.setPreference("dom.disable_window_open_feature.minimizable", 1);
        // Run driver with this profile this profile:
        //driver.manage().window().setPosition(new Point(-2000, 0));
        //driver.manage().window().setSize(new Dimension(1024, 768));
        return driver;
    }

    public enum Browser {
        CHROME {
            @Override
            WebDriver getDriver(Map<String, Object> preferences) {
                System.setProperty("webdriver.chrome.driver", "/home/davit/Downloads/chromedriver_executable");
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
//                capabilities.setCapability("chrome.switches", Arrays.asList("--proxy-server=http://purevpn0s1169596:sw6ymvvk@au-sd1-ovpn-tcp.pointtoserver.com:80"));

                ChromeOptions chromeOptions = new ChromeOptions();
                chromeOptions.addArguments(Arrays.asList(
                        "--start-maximized",
                        "--test-type",
                        "--disable-impl-side-painting"
//                        "--proxy-server=http://203.91.121.74:3128"
                ));

                capabilities.setCapability(ChromeOptions.CAPABILITY, chromeOptions);
                return new ChromeDriver(capabilities);
            }
        },
        FIREFOX {
            @Override
            WebDriver getDriver(Map<String, Object> preferences) {
                FirefoxProfile profile = new ProfilesIni().getProfile("default");
//                profile.setPreference("general.useragent.override", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_9_3) AppleWebKit/537.75.14 (KHTML, like Gecko) Version/7.0.3 Safari/7046A194A");
                profile.setPreference("general.useragent.override", "Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/41.0.2228.0 Safari/537.36");
//                profile.setPreference("general.useragent.override", "Mozilla/5.0 (Windows NT 6.1) (KHTML, like Gecko) Firefox/40.0");
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
//                desiredCapabilities.setCapability(CapabilityType.SUPPORTS_WEB_STORAGE, false);
//                desiredCapabilities.setCapability("firefox_profile", profile.toString());
                return new FirefoxDriver(desiredCapabilities);
            }
        },
        HTML_UNITY {
            @Override
            WebDriver getDriver(Map<String, Object> preferences) {
                BrowserVersion browser = BrowserVersion.FIREFOX_38;
                HtmlUnitDriver htmlUnitDriver = new ExtendedHtmlUnitDriver(browser);
                htmlUnitDriver.manage().timeouts().pageLoadTimeout(150, TimeUnit.SECONDS);
                htmlUnitDriver.manage().timeouts().setScriptTimeout(150, TimeUnit.SECONDS);
//                htmlUnitDriver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
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
                        "/home/davit/Public/git/phantomjs/bin/phantomjs"
                );

                capabilities.setJavascriptEnabled(true);                //< not really needed: JS enabled by default
                capabilities.setCapability("takesScreenshot", true);    //< yeah, GhostDriver haz screenshots!
                capabilities.setCapability("phantomjs.page.settings.userAgent", "Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:25.0) Gecko/20100101 Firefox/25.0");

                // Launch driver (will take care and ownership of the phantomjs process)
                WebDriver driver = new PhantomJSDriver(capabilities);
                return driver;
            }
        };

        abstract WebDriver getDriver(Map<String, Object> preferences);

        public static Browser getDefault() {
            return FIREFOX;
        }
    }
}
