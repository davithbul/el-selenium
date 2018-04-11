package el.selenium.model;

import org.apache.http.annotation.Immutable;
import org.openqa.selenium.By;

/**
 * ListElementDescription describe single element
 * from the list.
 */
@Immutable
public class ListElementDescription extends ElementDescription {

    private final int index;

    public ListElementDescription(By selector, int index) {
        super(selector);
        this.index = index;
    }

    public int getIndex() {
        return index;
    }
}
