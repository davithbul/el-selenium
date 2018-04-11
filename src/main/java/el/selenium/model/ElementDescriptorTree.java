package el.selenium.model;

import org.openqa.selenium.By;
import java.util.LinkedList;

public class ElementDescriptorTree {

    /**
     * Represents all the path till final element.
     * Might be single element descriptions.
     * The last element, is the final description
     */
    private LinkedList<ElementDescription> elementDescriptions;

    public ElementDescriptorTree(By selector) {
        this(new ElementDescription(selector));
    }

    public ElementDescriptorTree(ElementDescription elementDescription) {
        elementDescriptions = new LinkedList<>();
        elementDescriptions.add(elementDescription);
    }

    public ElementDescriptorTree(ElementDescriptorTree elementDescriptorTree) {
        this.elementDescriptions = new LinkedList<>(elementDescriptorTree.getElementDescriptions());
    }

    public ElementDescriptorTree(ElementDescriptorTree elementDescriptorTree, By selector) {
        this(elementDescriptorTree, new ElementDescription(selector));
    }

    public ElementDescriptorTree(ElementDescriptorTree elementDescriptorTree, ElementDescription elementDescription) {
        this.elementDescriptions = new LinkedList<>(elementDescriptorTree.getElementDescriptions());
        this.elementDescriptions.add(elementDescription);
    }

    public ElementDescriptorTree(ElementDescriptorTree parentElementDescriptor, ElementDescriptorTree childElementDescriptor) {
        this.elementDescriptions = new LinkedList<>(parentElementDescriptor.getElementDescriptions());
        this.elementDescriptions.addAll(childElementDescriptor.getElementDescriptions());
    }

    public LinkedList<ElementDescription> getElementDescriptions() {
        return elementDescriptions;
    }

    /**
     * Returns final element's description
     */
    public ElementDescription getElementDescription() {
        return elementDescriptions.getLast();
    }

    public ElementDescription getTopElementDescription() {
        return elementDescriptions.getFirst();
    }

    public void addTopElement(By selector) {
        addTopElement(new ElementDescription(selector));
    }

    public void addTopElement(ElementDescription elementDescription) {
        elementDescriptions.add(0, elementDescription);
    }
}
