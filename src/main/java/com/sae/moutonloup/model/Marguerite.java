package com.sae.moutonloup.model;

import javafx.scene.image.Image;

public class Marguerite extends Vegetal {

    @Override
    public Image getImage() {
        return new Image(getClass().getResource("/images/marguerite.png").toExternalForm());
    }

    @Override
    public boolean isAccessible() {
        return true;
    }
}
