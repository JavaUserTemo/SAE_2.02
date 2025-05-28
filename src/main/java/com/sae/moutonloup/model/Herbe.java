package com.sae.moutonloup.model;

import javafx.scene.image.Image;

public class Herbe extends Vegetal {

    @Override
    public Image getImage() {
        return new Image(getClass().getResource("/images/herbe.png").toExternalForm());
    }

    @Override
    public boolean isAccessible() {
        return true;
    }
}
