package com.sae.moutonloup.model;

public abstract class Animal extends Element {
    protected Position position;
    protected int vitesse;

    public Animal(Position position) {
        this.position = position;
    }

    public Position getPosition() {
        return position;
    }

    public int getX() {
        return position.getX();
    }

    public int getY() {
        return position.getY();
    }

    public int getVitesse() {
        return vitesse;
    }

    public void setPosition(Position pos) {
        this.position = pos;
    }
}
