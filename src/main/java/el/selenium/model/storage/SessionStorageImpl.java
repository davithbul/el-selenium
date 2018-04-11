package el.selenium.model.storage;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.html5.SessionStorage;

import java.util.HashSet;
import java.util.Set;

public class SessionStorageImpl implements SessionStorage {

    private JavascriptExecutor js;

    public SessionStorageImpl(WebDriver driver) {
        this.js = (JavascriptExecutor) driver;
    }

    @Override
    public String getItem(String key) {
        return (String) js.executeScript(String.format("return window.sessionStorage.getItem('%s');", key));
    }

    @Override
    public Set<String> keySet() {
        Set<String> keySet = new HashSet<>();
        for (int i = 0; i < size(); i++) {
            String key = (String) js.executeScript(String.format("return window.sessionStorage.key('%s');", i));
            keySet.add(key);
        }
        return keySet;
    }

    @Override
    public void setItem(String key, String value) {
        js.executeScript(String.format(
                "window.sessionStorage.setItem('%s','%s');", key, value));
    }

    @Override
    public String removeItem(String key) {
        String item = getItem(key);
        js.executeScript(String.format("window.sessionStorage.removeItem('%s');", key));
        return item;
    }

    @Override
    public void clear() {
        js.executeScript("window.sessionStorage.clear();");
        for (String key : keySet()) {
            removeItem(key);
            removeItem(key);
        }
    }

    @Override
    public int size() {
        return ((Long) js.executeScript("return window.sessionStorage.length;")).intValue();
    }
}
