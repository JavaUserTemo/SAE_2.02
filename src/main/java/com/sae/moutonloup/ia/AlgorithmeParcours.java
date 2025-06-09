package com.sae.moutonloup.ia;

import com.sae.moutonloup.model.*;
import java.util.*;

public class AlgorithmeParcours {

    public static List<Position> aStar(Position depart, Position objectif, Element[][] grille,
                                       Mouton mouton, Loup loup, Animal animal) {
        if (depart == null || objectif == null || grille == null) {
            return new ArrayList<>();
        }

        int largeur = grille.length;
        int hauteur = grille[0].length;

        Map<Position, Integer> gScore = new HashMap<>();
        Map<Position, Integer> fScore = new HashMap<>();
        Map<Position, Position> precedent = new HashMap<>();

        PriorityQueue<Position> openSet = new PriorityQueue<>(
                Comparator.comparingInt(pos -> fScore.getOrDefault(pos, Integer.MAX_VALUE))
        );
        Set<Position> closedSet = new HashSet<>();

        gScore.put(depart, 0);
        fScore.put(depart, depart.distanceManhattan(objectif));
        openSet.add(depart);

        while (!openSet.isEmpty()) {
            Position courant = openSet.poll();

            if (courant.equals(objectif)) {
                return construireChemin(precedent, depart, objectif);
            }

            closedSet.add(courant);

            for (Position voisin : getVoisinsValides(courant, grille, largeur, hauteur, mouton, loup, animal)) {
                if (closedSet.contains(voisin)) {
                    continue;
                }

                int tentativeG = gScore.get(courant) + 1;

                if (!gScore.containsKey(voisin) || tentativeG < gScore.get(voisin)) {
                    precedent.put(voisin, courant);
                    gScore.put(voisin, tentativeG);
                    fScore.put(voisin, tentativeG + voisin.distanceManhattan(objectif));

                    if (!openSet.contains(voisin)) {
                        openSet.add(voisin);
                    }
                }
            }
        }

        return new ArrayList<>();
    }

    public static List<Position> dijkstra(Position depart, Position objectif, Element[][] grille,
                                          Mouton mouton, Loup loup, Animal animal) {
        if (depart == null || objectif == null || grille == null) {
            return new ArrayList<>();
        }

        int largeur = grille.length;
        int hauteur = grille[0].length;

        Map<Position, Integer> distances = new HashMap<>();
        Map<Position, Position> precedents = new HashMap<>();
        PriorityQueue<Position> file = new PriorityQueue<>(
                Comparator.comparingInt(pos -> distances.getOrDefault(pos, Integer.MAX_VALUE))
        );

        distances.put(depart, 0);
        file.add(depart);

        while (!file.isEmpty()) {
            Position courant = file.poll();

            if (courant.equals(objectif)) {
                return construireChemin(precedents, depart, objectif);
            }

            int distanceCourante = distances.get(courant);

            for (Position voisin : getVoisinsValides(courant, grille, largeur, hauteur, mouton, loup, animal)) {
                int nouvelleDist = distanceCourante + 1;

                if (nouvelleDist < distances.getOrDefault(voisin, Integer.MAX_VALUE)) {
                    distances.put(voisin, nouvelleDist);
                    precedents.put(voisin, courant);
                    file.remove(voisin);
                    file.add(voisin);
                }
            }
        }

        return new ArrayList<>();
    }

    private static List<Position> getVoisinsValides(Position pos, Element[][] grille,
                                                    int largeur, int hauteur,
                                                    Mouton mouton, Loup loup, Animal animal) {
        List<Position> voisins = new ArrayList<>();
        int[][] directions = {{0, 1}, {0, -1}, {1, 0}, {-1, 0}};

        for (int[] dir : directions) {
            int newX = pos.getX() + dir[0];
            int newY = pos.getY() + dir[1];

            if (newX >= 0 && newX < largeur && newY >= 0 && newY < hauteur) {
                Element element = grille[newX][newY];

                // Ne pas passer sur les rochers
                if (element instanceof Rocher) {
                    continue;
                }

                // Le loup ne peut pas aller sur la sortie SAUF pour attraper le mouton
                if (animal instanceof Loup && element instanceof Sortie) {
                    if (mouton == null || mouton.getX() != newX || mouton.getY() != newY) {
                        continue; // Le loup ne peut pas aller sur la sortie
                    }
                }

                voisins.add(new Position(newX, newY));
            }
        }

        return voisins;
    }

    private static List<Position> construireChemin(Map<Position, Position> precedent,
                                                   Position depart, Position objectif) {
        List<Position> chemin = new ArrayList<>();
        Position courant = objectif;

        while (precedent.containsKey(courant)) {
            chemin.add(0, courant);
            courant = precedent.get(courant);
        }

        return chemin;
    }

    public static Position trouverMeilleurePosition(Animal animal, Position objectif,
                                                    Element[][] grille, Mouton mouton, Loup loup) {
        Position posActuelle = animal.getPosition();
        int vitesse = animal.getVitesse();
        Position meilleurePos = null;
        int meilleureDistance = Integer.MAX_VALUE;

        // Parcourir toutes les positions à la distance exacte de la vitesse
        for (int dx = -vitesse; dx <= vitesse; dx++) {
            for (int dy = -vitesse; dy <= vitesse; dy++) {
                // Distance de Manhattan exacte
                if (Math.abs(dx) + Math.abs(dy) == vitesse) {
                    int newX = posActuelle.getX() + dx;
                    int newY = posActuelle.getY() + dy;

                    if (estDeplacementValide(posActuelle, new Position(newX, newY),
                            grille, animal, mouton, loup)) {
                        Position candidat = new Position(newX, newY);
                        int distanceVersObjectif = candidat.distanceManhattan(objectif);

                        if (distanceVersObjectif < meilleureDistance) {
                            meilleureDistance = distanceVersObjectif;
                            meilleurePos = candidat;
                        }
                    }
                }
            }
        }

        return meilleurePos;
    }

    public static boolean estDeplacementValide(Position depart, Position arrivee,
                                               Element[][] grille, Animal animal,
                                               Mouton mouton, Loup loup) {
        if (depart == null || arrivee == null || grille == null) {
            return false;
        }

        int largeur = grille.length;
        int hauteur = grille[0].length;

        // Vérifier que la destination est dans les limites
        if (arrivee.getX() < 0 || arrivee.getX() >= largeur ||
                arrivee.getY() < 0 || arrivee.getY() >= hauteur) {
            return false;
        }

        // Vérifier que la destination n'est pas un rocher
        Element elementArrivee = grille[arrivee.getX()][arrivee.getY()];
        if (elementArrivee instanceof Rocher) {
            return false;
        }

        // Le loup ne peut pas aller sur la sortie sauf pour attraper le mouton
        if (animal instanceof Loup && elementArrivee instanceof Sortie) {
            return mouton != null && mouton.getX() == arrivee.getX() && mouton.getY() == arrivee.getY();
        }

        // Vérifier qu'il n'y a pas d'obstacles sur le chemin
        return !aObstacleSurChemin(depart, arrivee, grille);
    }

    private static boolean aObstacleSurChemin(Position depart, Position arrivee, Element[][] grille) {
        int x0 = depart.getX(), y0 = depart.getY();
        int x1 = arrivee.getX(), y1 = arrivee.getY();

        int dx = Math.abs(x1 - x0);
        int dy = Math.abs(y1 - y0);
        int sx = x0 < x1 ? 1 : -1;
        int sy = y0 < y1 ? 1 : -1;
        int err = dx - dy;

        int x = x0, y = y0;

        while (true) {
            // Ne pas vérifier les cases de départ et d'arrivée
            if ((x != x0 || y != y0) && (x != x1 || y != y1)) {
                if (x >= 0 && x < grille.length && y >= 0 && y < grille[0].length) {
                    if (grille[x][y] instanceof Rocher) {
                        return true;
                    }
                }
            }

            if (x == x1 && y == y1) break;

            int e2 = 2 * err;
            if (e2 > -dy) {
                err -= dy;
                x += sx;
            }
            if (e2 < dx) {
                err += dx;
                y += sy;
            }
        }

        return false;
    }
}