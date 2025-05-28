package com.sae.moutonloup.model;

public class Case {
    private Element element;

    public Case(Element element) {
        this.element = element;
    }

    public Element getElement() {
        return element;
    }

    public void setElement(Element element) {
        this.element = element;
    }

    public boolean estAccessible() {
        return element == null || element.isAccessible();
    }

    public boolean estRocher() {
        return element instanceof Rocher;
    }

    public boolean estSortie() {
        return element instanceof Herbe;
    }

    public boolean estVide() {
        return element == null;
    }
}
