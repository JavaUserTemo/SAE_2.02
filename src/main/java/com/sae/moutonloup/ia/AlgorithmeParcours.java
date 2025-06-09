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

        // Initialiser toutes les distances à l'infini
        for (int x = 0; x < largeur; x++) {
            for (int y = 0; y < hauteur; y++) {
                distances.put(new Position(x, y), Integer.MAX_VALUE);
            }
        }

        distances.put(depart, 0);
        file.add(depart);

        while (!file.isEmpty()) {
            Position courant = file.poll();

            if (courant.equals(objectif)) {
                return construireChemin(precedents, depart, objectif);
            }

            int distanceCourante = distances.get(courant);
            if (distanceCourante == Integer.MAX_VALUE) {
                continue;
            }

            for (Position voisin : getVoisinsValides(courant, grille, largeur, hauteur, mouton, loup, animal)) {
                int nouvelleDist = distanceCourante + 1;

                if (nouvelleDist < distances.get(voisin)) {
                    distances.put(voisin, nouvelleDist);
                    precedents.put(voisin, courant);
                    file.remove(voisin);
                    file.add(voisin);
                }
            }
        }

        return new ArrayList<>();
    }


    public static List<Position> parcoursenLargeur(Position depart, Position objectif, Element[][] grille,
                                                   Mouton mouton, Loup loup, Animal animal) {
        if (depart == null || objectif == null || grille == null) {
            return new ArrayList<>();
        }

        int largeur = grille.length;
        int hauteur = grille[0].length;

        Queue<Position> file = new LinkedList<>();
        Map<Position, Position> precedent = new HashMap<>();
        Set<Position> visite = new HashSet<>();

        file.offer(depart);
        visite.add(depart);

        while (!file.isEmpty()) {
            Position courant = file.poll();

            if (courant.equals(objectif)) {
                return construireChemin(precedent, depart, objectif);
            }

            for (Position voisin : getVoisinsValides(courant, grille, largeur, hauteur, mouton, loup, animal)) {
                if (!visite.contains(voisin)) {
                    visite.add(voisin);
                    precedent.put(voisin, courant);
                    file.offer(voisin);
                }
            }
        }

        return new ArrayList<>();
    }


    public static List<Position> parcoursEnProfondeur(Position depart, Position objectif, Element[][] grille,
                                                      Mouton mouton, Loup loup, Animal animal) {
        if (depart == null || objectif == null || grille == null) {
            return new ArrayList<>();
        }

        int largeur = grille.length;
        int hauteur = grille[0].length;

        Stack<Position> pile = new Stack<>();
        Map<Position, Position> precedent = new HashMap<>();
        Set<Position> visite = new HashSet<>();

        pile.push(depart);

        while (!pile.isEmpty()) {
            Position courant = pile.pop();

            if (visite.contains(courant)) {
                continue;
            }

            visite.add(courant);

            if (courant.equals(objectif)) {
                return construireChemin(precedent, depart, objectif);
            }

            for (Position voisin : getVoisinsValides(courant, grille, largeur, hauteur, mouton, loup, animal)) {
                if (!visite.contains(voisin)) {
                    precedent.put(voisin, courant);
                    pile.push(voisin);
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

        // Utiliser A* pour planifier le déplacement optimal
        List<Position> chemin = aStar(posActuelle, objectif, grille, mouton, loup, animal);

        if (chemin.isEmpty()) {
            return null;
        }

        // Prendre les premières positions du chemin selon la vitesse
        int index = Math.min(vitesse - 1, chemin.size() - 1);
        return chemin.get(index);
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

        // Vérifier que c'est un déplacement adjacent (case par case)
        return depart.distanceManhattan(arrivee) == 1;
    }


    public static List<Position> trouverPositionsAccessibles(Position depart, int distance,
                                                             Element[][] grille, Animal animal,
                                                             Mouton mouton, Loup loup) {
        List<Position> positions = new ArrayList<>();
        int largeur = grille.length;
        int hauteur = grille[0].length;

        for (int x = 0; x < largeur; x++) {
            for (int y = 0; y < hauteur; y++) {
                Position pos = new Position(x, y);
                if (depart.distanceManhattan(pos) <= distance) {
                    List<Position> chemin = aStar(depart, pos, grille, mouton, loup, animal);
                    if (!chemin.isEmpty() && chemin.size() <= distance) {
                        positions.add(pos);
                    }
                }
            }
        }

        return positions;
    }
}