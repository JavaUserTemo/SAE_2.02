package com.sae.moutonloup.model;

import javafx.scene.image.Image;

public class Loup extends Animal {

    public Loup(Position position) {
        super(position);
        this.vitesse = 3;
    }

    @Override
    public Image getImage() {
        return new Image(getClass().getResource("/images/loup.png").toExternalForm());
    }

    @Override
    public boolean isAccessible() {
        return false;
    }
}
