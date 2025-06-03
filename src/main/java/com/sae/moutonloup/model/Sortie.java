package com.sae.moutonloup.model;

import javafx.scene.image.Image;

public class Sortie extends Element {

    private int x;
    private int y;

    public Sortie(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    @Override
    public boolean isAccessible() {
        return true;
    }

    @Override
    public Image getImage() {
        return new Image(getClass().getResource("/images/sortie.png").toExternalForm());
    }
}
