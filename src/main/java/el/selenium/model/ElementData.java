package el.selenium.model;

public class ElementData extends Element {

    private String value;

    public ElementData(String selector, String value) {
        this(selector, value, SelectorType.id);
    }

    public ElementData(String selector, String value, SelectorType type) {
        super(selector, type);
        this.value = value;
    }

    public ElementData(String selector, String value, SelectorType selectorType, ElementType elementType) {
        this(selector, value, selectorType, elementType, false);
    }

    public ElementData(String selector, String value, SelectorType selectorType, ElementType elementType, boolean multiple) {
        super(selector, selectorType, elementType, multiple);
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
