package com.sae.moutonloup.model;

import javafx.scene.image.Image;

public class Mouton extends Animal {

    private int vitesse = 2;

    public Mouton(Position position) {
        super(position);
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

    public int getVitesse() {
        return vitesse;
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
