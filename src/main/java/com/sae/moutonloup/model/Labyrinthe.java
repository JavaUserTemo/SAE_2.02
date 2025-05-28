package com.sae.moutonloup.model;

public class Labyrinthe {
    private final int nbColonnes;
    private final int nbLignes;
    private final Case[][] grille;

    private Mouton mouton;
    private Loup loup;
    private Position sortie;

    public Labyrinthe(int colonnes, int lignes) {
        this.nbColonnes = colonnes;
        this.nbLignes = lignes;
        this.grille = new Case[colonnes][lignes];

        // Initialisation par défaut
        for (int x = 0; x < colonnes; x++) {
            for (int y = 0; y < lignes; y++) {
                if (x == 0 || y == 0 || x == colonnes - 1 || y == lignes - 1) {
                    grille[x][y] = new Case(new Rocher());
                } else {
                    grille[x][y] = new Case(new Herbe());
                }
            }
        }
    }
    public Vegetal genererVegetalAleatoire() {

        int r = new java.util.Random().nextInt(3);
        return switch (r) {
            case 0 -> new Herbe();
            case 1 -> new Marguerite();
            default -> new Cactus();
        };
    }

    public void placerMouton(int x, int y) {
        if (!grille[x][y].estRocher()) {
            mouton = new Mouton(new Position(x, y));
        }
    }

    public void placerLoup(int x, int y) {
        if (!grille[x][y].estRocher() && (sortie == null || !(sortie.getX() == x && sortie.getY() == y))) {
            loup = new Loup(new Position(x, y));
        }
    }

    public void placerVegetal(Vegetal vegetal, int x, int y) {
        if (!grille[x][y].estRocher()) {
            grille[x][y].setElement(vegetal);
        }
    }

    public void placerObstacle(int x, int y) {
        grille[x][y].setElement(new Rocher());
    }

    public void placerSortie(int x, int y) {
        if (x == 0 || y == 0 || x == nbColonnes - 1 || y == nbLignes - 1) {
            grille[x][y].setElement(new Herbe());
            sortie = new Position(x, y);
        }
    }

    public void supprimerElement(int x, int y) {
        if (mouton != null && mouton.getX() == x && mouton.getY() == y) mouton = null;
        else if (loup != null && loup.getX() == x && loup.getY() == y) loup = null;
        else if (!grille[x][y].estRocher()) grille[x][y].setElement(new Herbe());

        if (sortie != null && sortie.getX() == x && sortie.getY() == y) sortie = null;
    }

    public Case getCase(int x, int y) {
        return grille[x][y];
    }

    public Element getElement(int x, int y) {
        return grille[x][y].getElement();
    }

    public int getNbColonnes() {
        return nbColonnes;
    }

    public int getNbLignes() {
        return nbLignes;
    }

    public Mouton getMouton() {
        return mouton;
    }

    public Loup getLoup() {
        return loup;
    }

    public Position getSortie() {
        return sortie;
    }
}
