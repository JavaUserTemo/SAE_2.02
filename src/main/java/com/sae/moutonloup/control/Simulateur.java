package com.sae.moutonloup.control;

import com.sae.moutonloup.model.*;

public class Simulateur {
    private Labyrinthe labyrinthe;
    private Mouton mouton;
    private Loup loup;

    private int nbTours = 0;
    private int nbHerbe = 0, nbCactus = 0, nbMarguerite = 0;

    public Simulateur(Labyrinthe labyrinthe, Mouton mouton, Loup loup) {
        this.labyrinthe = labyrinthe;
        this.mouton = mouton;
        this.loup = loup;
    }

    public void tourMouton() {
        Position pos = mouton.getPosition();
        Element element = labyrinthe.getElement(pos.getX(), pos.getY());

        if (element instanceof Vegetal vegetal) {
            mouton.manger(vegetal);
            if (vegetal instanceof Herbe) nbHerbe++;
            else if (vegetal instanceof Marguerite) nbMarguerite++;
            else if (vegetal instanceof Cactus) nbCactus++;
        }


        labyrinthe.getCase(pos.getX(), pos.getY()).setElement(
                labyrinthe.genererVegetalAleatoire()
        );
    }

    public void tourLoup() {

    }

    public boolean estPartieFinie() {
        return mouton.getPosition().equals(labyrinthe.getSortie()) ||
                mouton.getPosition().equals(loup.getPosition());
    }

    public String getVainqueur() {
        if (mouton.getPosition().equals(loup.getPosition())) return "Loup";
        if (mouton.getPosition().equals(labyrinthe.getSortie())) return "Mouton";
        return null;
    }

    public void incrementerTour() {
        nbTours++;
    }

    public int getNbTours() {
        return nbTours;
    }

    public int getNbHerbe() {
        return nbHerbe;
    }

    public int getNbCactus() {
        return nbCactus;
    }

    public int getNbMarguerite() {
        return nbMarguerite;
    }
}
