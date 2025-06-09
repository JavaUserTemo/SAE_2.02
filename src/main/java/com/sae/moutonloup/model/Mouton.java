package com.sae.moutonloup.model;

import javafx.scene.image.Image;

public class Mouton extends Animal {

    public Mouton(Position position) {
        super(position);
        this.vitesse = 2; // Initialiser la vitesse héritée de Animal
    }

    public void manger(Vegetal vegetal) {
        if (vegetal instanceof Herbe) {
            this.vitesse = 2;
        } else if (vegetal instanceof Cactus) {
            this.vitesse = 1;
        } else if (vegetal instanceof Marguerite) {
            this.vitesse = 4;
        }
    }

    @Override
    public Image getImage() {
        return new Image(getClass().getResource("/images/mouton.png").toExternalForm());
    }

    @Override
    public boolean isAccessible() {
        return false;
    }
}