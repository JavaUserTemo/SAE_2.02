package com.sae.moutonloup.model;

import javafx.scene.image.Image;

public class Cactus extends Vegetal {

    @Override
    public Image getImage() {
        return new Image(getClass().getResource("/images/cactus.png").toExternalForm());
    }

    @Override
    public boolean isAccessible() {
        return true;
    }
}
