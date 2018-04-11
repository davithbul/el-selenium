package el.selenium.model;

import org.openqa.selenium.By;

public class Element {

    private String selector;

    private SelectorType selectorType;

    private ElementType elementType;

    private boolean multiple;

    public Element(String selector) {
        this.selector = selector;
        this.elementType = ElementType.input;
        if(selector.startsWith(".")) {
            this.selectorType = SelectorType.className;
        } else {
            this.selectorType = SelectorType.id;
        }
    }

    public Element(String selector, SelectorType selectorType) {
        this(selector, selectorType, ElementType.input);
    }

    public Element(String selector, SelectorType selectorType, ElementType elementType) {
        this(selector, selectorType, elementType, false);
    }

    public Element(String selector, SelectorType selectorType, ElementType elementType, boolean multiple) {
        this.selector = selector;
        this.selectorType = selectorType;
        this.elementType = elementType;
        this.multiple = multiple;
    }

    public String getSelector() {
        return selector;
    }

    public SelectorType getSelectorType() {
        return selectorType;
    }

    public ElementType getElementType() {
        return elementType;
    }

    public boolean isMultiple() {
        return multiple;
    }

    public By getBySelector() {
        return this.getSelectorType().getBySelector(selector);
    }
}
