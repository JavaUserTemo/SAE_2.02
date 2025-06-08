package com.sae.moutonloup.model;

public class Labyrinthe {
    private final int nbColonnes;
    private final int nbLignes;
    private final Element[][] grille;

    private Mouton mouton;
    private Loup loup;
    private Position sortie;

    public Labyrinthe(int colonnes, int lignes) {
        this.nbColonnes = colonnes;
        this.nbLignes = lignes;
        this.grille = new Element[colonnes][lignes];

        // Initialisation par défaut avec bordure de rochers
        initialiserGrilleParDefaut();
    }


    private void initialiserGrilleParDefaut() {
        for (int x = 0; x < nbColonnes; x++) {
            for (int y = 0; y < nbLignes; y++) {
                if (x == 0 || y == 0 || x == nbColonnes - 1 || y == nbLignes - 1) {
                    grille[x][y] = new Rocher();
                } else {
                    grille[x][y] = new Herbe();
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


    public boolean placerMouton(int x, int y) {
        if (!estPositionValide(x, y)) {
            return false;
        }

        // Ne peut pas placer le mouton sur un rocher
        if (grille[x][y] instanceof Rocher) {
            return false;
        }

        mouton = new Mouton(new Position(x, y));

        // Si la case ne contient pas déjà un végétal, mettre de l'herbe
        if (!(grille[x][y] instanceof Vegetal)) {
            grille[x][y] = new Herbe();
        }

        return true;
    }


    public boolean placerLoup(int x, int y) {
        if (!estPositionValide(x, y)) {
            return false;
        }

        // Ne peut pas placer le loup sur un rocher ou sur la sortie
        if (grille[x][y] instanceof Rocher) {
            return false;
        }

        if (sortie != null && sortie.getX() == x && sortie.getY() == y) {
            return false;
        }

        loup = new Loup(new Position(x, y));

        // Si la case ne contient pas déjà un végétal, mettre de l'herbe
        if (!(grille[x][y] instanceof Vegetal)) {
            grille[x][y] = new Herbe();
        }

        return true;
    }


    public boolean placerVegetal(Vegetal vegetal, int x, int y) {
        if (!estPositionValide(x, y)) {
            return false;
        }

        // Ne peut pas placer un végétal sur un rocher
        if (grille[x][y] instanceof Rocher) {
            return false;
        }

        grille[x][y] = vegetal;
        return true;
    }


    public boolean placerObstacle(int x, int y) {
        if (!estPositionValide(x, y)) {
            return false;
        }

        // Ne peut pas placer un rocher sur les bords (ils sont déjà des rochers)
        // Mais on peut le faire à l'intérieur
        grille[x][y] = new Rocher();

        // Si des animaux étaient sur cette case, les supprimer
        if (mouton != null && mouton.getX() == x && mouton.getY() == y) {
            mouton = null;
        }
        if (loup != null && loup.getX() == x && loup.getY() == y) {
            loup = null;
        }

        return true;
    }


    public boolean placerSortie(int x, int y) {
        if (!estPositionValide(x, y)) {
            return false;
        }

        // La sortie doit être sur un bord
        if (!(x == 0 || y == 0 || x == nbColonnes - 1 || y == nbLignes - 1)) {
            return false;
        }

        // Remplacer le rocher du bord par la sortie
        Sortie nouvelleSortie = new Sortie(x, y);
        grille[x][y] = nouvelleSortie;
        sortie = new Position(x, y);

        return true;
    }


    public void supprimerElement(int x, int y) {
        if (!estPositionValide(x, y)) {
            return;
        }

        // Supprimer les animaux s'ils sont sur cette case
        if (mouton != null && mouton.getX() == x && mouton.getY() == y) {
            mouton = null;
        }
        if (loup != null && loup.getX() == x && loup.getY() == y) {
            loup = null;
        }

        // Si c'est la sortie, la supprimer et remettre un rocher
        if (sortie != null && sortie.getX() == x && sortie.getY() == y) {
            sortie = null;
            grille[x][y] = new Rocher();
        } else {
            // Si ce n'est pas un bord, remettre de l'herbe
            if (!(x == 0 || y == 0 || x == nbColonnes - 1 || y == nbLignes - 1)) {
                grille[x][y] = new Herbe();
            }
        }
    }


    private boolean estPositionValide(int x, int y) {
        return x >= 0 && x < nbColonnes && y >= 0 && y < nbLignes;
    }


    public boolean estAccessible(int x, int y) {
        if (!estPositionValide(x, y)) {
            return false;
        }
        return !(grille[x][y] instanceof Rocher);
    }


    public boolean estRocher(int x, int y) {
        if (!estPositionValide(x, y)) {
            return false;
        }
        return grille[x][y] instanceof Rocher;
    }


    public boolean estSortie(int x, int y) {
        if (!estPositionValide(x, y)) {
            return false;
        }
        return grille[x][y] instanceof Sortie;
    }


    public boolean estVegetal(int x, int y) {
        if (!estPositionValide(x, y)) {
            return false;
        }
        return grille[x][y] instanceof Vegetal;
    }


    public boolean deplacerMouton(int newX, int newY) {
        if (mouton == null || !estPositionValide(newX, newY)) {
            return false;
        }

        if (grille[newX][newY] instanceof Rocher) {
            return false;
        }

        mouton.setPosition(new Position(newX, newY));
        return true;
    }


    public boolean deplacerLoup(int newX, int newY) {
        if (loup == null || !estPositionValide(newX, newY)) {
            return false;
        }

        if (grille[newX][newY] instanceof Rocher) {
            return false;
        }


        if (grille[newX][newY] instanceof Sortie) {
            if (mouton == null || mouton.getX() != newX || mouton.getY() != newY) {
                return false;
            }
        }

        loup.setPosition(new Position(newX, newY));
        return true;
    }


    public void moutonMange(int x, int y) {
        if (!estPositionValide(x, y) || mouton == null) {
            return;
        }

        Element element = grille[x][y];
        if (element instanceof Vegetal vegetal) {
            mouton.manger(vegetal);

            grille[x][y] = genererVegetalAleatoire();
        }
    }


    public Element getElement(int x, int y) {
        if (!estPositionValide(x, y)) {
            return null;
        }
        return grille[x][y];
    }

    public Element[][] getGrille() {
        return grille;
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

    /**
     * Remet la grille à son état initial
     */
    public void reinitialiser() {
        mouton = null;
        loup = null;
        sortie = null;
        initialiserGrilleParDefaut();
    }

    /**
     * Vérifie si le labyrinthe est connexe (tous les points accessibles peuvent atteindre la sortie)
     */
    public boolean estConnexeVersSortie() {
        if (sortie == null) {
            return false;
        }

        boolean[][] visite = new boolean[nbColonnes][nbLignes];
        explorerDepuisSortie(sortie.getX(), sortie.getY(), visite);

        // Vérifier que toutes les cases accessibles ont été visitées
        for (int x = 0; x < nbColonnes; x++) {
            for (int y = 0; y < nbLignes; y++) {
                if (estAccessible(x, y) && !visite[x][y]) {
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * Explore récursivement depuis la sortie pour vérifier la connectivité
     */
    private void explorerDepuisSortie(int x, int y, boolean[][] visite) {
        if (!estPositionValide(x, y) || visite[x][y] || grille[x][y] instanceof Rocher) {
            return;
        }

        visite[x][y] = true;

        // Explorer les 4 directions
        explorerDepuisSortie(x + 1, y, visite);
        explorerDepuisSortie(x - 1, y, visite);
        explorerDepuisSortie(x, y + 1, visite);
        explorerDepuisSortie(x, y - 1, visite);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int y = 0; y < nbLignes; y++) {
            for (int x = 0; x < nbColonnes; x++) {
                if (mouton != null && mouton.getX() == x && mouton.getY() == y) {
                    sb.append("M");
                } else if (loup != null && loup.getX() == x && loup.getY() == y) {
                    sb.append("L");
                } else {
                    Element e = grille[x][y];
                    if (e instanceof Rocher) sb.append("X");
                    else if (e instanceof Herbe) sb.append(".");
                    else if (e instanceof Cactus) sb.append("C");
                    else if (e instanceof Marguerite) sb.append("F");
                    else if (e instanceof Sortie) sb.append("S");
                    else sb.append("?");
                }
            }
            sb.append("\n");
        }
        return sb.toString();
    }
}