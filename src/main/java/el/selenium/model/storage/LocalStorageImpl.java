package el.selenium.model.storage;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.html5.LocalStorage;

import java.util.HashSet;
import java.util.Set;

public class LocalStorageImpl implements LocalStorage {

    private JavascriptExecutor js;

    public LocalStorageImpl(WebDriver webDriver) {
        this.js = (JavascriptExecutor) webDriver;
    }

    @Override
    public String getItem(String key) {
        return (String) js.executeScript(String.format("return window.localStorage.getItem('%s');", key));
    }

    @Override
    public Set<String> keySet() {
        Set<String> keySet = new HashSet<>();
        for (int i = 0; i < size(); i++) {
            String key = (String) js.executeScript(String.format("return window.localStorage.key('%s');", i));
            keySet.add(key);
        }
        return keySet;
    }

    @Override
    public void setItem(String key, String value) {
        js.executeScript(String.format(
                "window.localStorage.setItem('%s','%s');", key, value));
    }

    @Override
    public String removeItem(String key) {
        String item = getItem(key);
        js.executeScript(String.format("window.localStorage.removeItem('%s');", key));
        return item;
    }

    @Override
    public void clear() {
        js.executeScript("window.localStorage.clear();");
        for (String key : keySet()) {
            removeItem(key);
            removeItem(key);
        }
    }

    @Override
    public int size() {
        return ((Long) js.executeScript("return window.localStorage.length;")).intValue();
    }
}
