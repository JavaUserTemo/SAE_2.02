package com.sae.moutonloup.model;

import javafx.scene.image.Image;

public class Sortie extends Element {
    @Override
    public boolean isAccessible() {
        return true;
    }

    @Override
    public Image getImage() {
        return new Image(getClass().getResource("/images/sortie.png").toExternalForm());
    }
}
