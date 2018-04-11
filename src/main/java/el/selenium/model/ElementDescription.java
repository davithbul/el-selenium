package el.selenium.model;

import org.apache.http.annotation.Immutable;
import org.openqa.selenium.By;

/**
 * Describes single element, might or might not be nested element.
 */
@Immutable
public class ElementDescription {
    private final By selector;

    public ElementDescription(By selector) {
        this.selector = selector;
    }

    public By getSelector() {
        return selector;
    }
}
