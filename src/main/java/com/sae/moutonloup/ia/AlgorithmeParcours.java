package com.sae.moutonloup.ia;

import com.sae.moutonloup.model.*;

import java.util.*;

public class AlgorithmeParcours {

    /**
     * Algorithme de Dijkstra pour trouver le chemin le plus court
     * Utilisé pour le loup qui chasse le mouton
     */
    public static List<Position> dijkstra(Position depart, Position objectif, Element[][] grille) {
        int largeur = grille.length;
        int hauteur = grille[0].length;

        if (!estPositionValide(depart, largeur, hauteur) ||
                !estPositionValide(objectif, largeur, hauteur)) {
            return new ArrayList<>();
        }

        Map<Position, Position> precedent = new HashMap<>();
        Map<Position, Integer> distance = new HashMap<>();
        PriorityQueue<Position> file = new PriorityQueue<>(Comparator.comparingInt(distance::get));

        // Initialisation des distances
        for (int x = 0; x < largeur; x++) {
            for (int y = 0; y < hauteur; y++) {
                Position p = new Position(x, y);
                distance.put(p, Integer.MAX_VALUE);
            }
        }

        distance.put(depart, 0);
        file.add(depart);

        while (!file.isEmpty()) {
            Position courant = file.poll();

            if (courant.equals(objectif)) {
                break;
            }

            int distanceCourante = distance.get(courant);
            if (distanceCourante == Integer.MAX_VALUE) {
                break;
            }

            for (Position voisin : getVoisinsAccessibles(courant, grille, true)) {
                int nouvelleDist = distanceCourante + 1;

                if (nouvelleDist < distance.get(voisin)) {
                    distance.put(voisin, nouvelleDist);
                    precedent.put(voisin, courant);
                    file.remove(voisin);
                    file.add(voisin);
                }
            }
        }

        return construireChemin(precedent, depart, objectif);
    }

    /**
     * Algorithme A* pour trouver le chemin optimal avec heuristique
     * Utilisé pour le mouton qui fuit vers la sortie
     */
    public static List<Position> aStar(Position depart, Position objectif, Element[][] grille) {
        int largeur = grille.length;
        int hauteur = grille[0].length;

        if (!estPositionValide(depart, largeur, hauteur) ||
                !estPositionValide(objectif, largeur, hauteur)) {
            return new ArrayList<>();
        }

        Map<Position, Integer> gScore = new HashMap<>();
        Map<Position, Integer> fScore = new HashMap<>();
        Map<Position, Position> precedent = new HashMap<>();

        Set<Position> openSet = new HashSet<>();
        Set<Position> closedSet = new HashSet<>();

        gScore.put(depart, 0);
        fScore.put(depart, depart.distanceManhattan(objectif));

        PriorityQueue<Position> file = new PriorityQueue<>(Comparator.comparingInt(fScore::get));
        file.add(depart);
        openSet.add(depart);

        while (!file.isEmpty()) {
            Position courant = file.poll();
            openSet.remove(courant);
            closedSet.add(courant);

            if (courant.equals(objectif)) {
                break;
            }

            for (Position voisin : getVoisinsAccessibles(courant, grille, false)) {
                if (closedSet.contains(voisin)) {
                    continue;
                }

                int tentativeG = gScore.get(courant) + 1;

                if (!openSet.contains(voisin)) {
                    openSet.add(voisin);
                } else if (tentativeG >= gScore.getOrDefault(voisin, Integer.MAX_VALUE)) {
                    continue;
                }

                precedent.put(voisin, courant);
                gScore.put(voisin, tentativeG);
                fScore.put(voisin, tentativeG + voisin.distanceManhattan(objectif));

                file.remove(voisin);
                file.add(voisin);
            }
        }

        return construireChemin(precedent, depart, objectif);
    }

    /**
     * Obtient les voisins accessibles d'une position
     */
    private static List<Position> getVoisinsAccessibles(Position pos, Element[][] grille, boolean estLoup) {
        List<Position> voisins = new ArrayList<>();
        int x = pos.getX(), y = pos.getY();
        int largeur = grille.length;
        int hauteur = grille[0].length;

        // Directions possibles : droite, gauche, bas, haut
        int[][] directions = {{1,0}, {-1,0}, {0,1}, {0,-1}};

        for (int[] dir : directions) {
            int nx = x + dir[0];
            int ny = y + dir[1];

            if (nx >= 0 && ny >= 0 && nx < largeur && ny < hauteur) {
                Element element = grille[nx][ny];

                if (!(element instanceof Rocher)) {
                    // Le loup ne peut pas aller sur la sortie
                    if (estLoup && element instanceof Sortie) {
                        continue;
                    }
                    voisins.add(new Position(nx, ny));
                }
            }
        }

        return voisins;
    }

    /**
     * Reconstruit le chemin à partir des précédents
     */
    private static List<Position> construireChemin(Map<Position, Position> precedent,
                                                   Position depart, Position objectif) {
        LinkedList<Position> chemin = new LinkedList<>();
        Position courant = objectif;

        if (!precedent.containsKey(objectif) && !depart.equals(objectif)) {
            return chemin;
        }

        // Reconstruction du chemin en remontant depuis l'objectif
        while (precedent.containsKey(courant)) {
            chemin.addFirst(courant);
            courant = precedent.get(courant);
        }

        return chemin;
    }

    /**
     * Vérifie si une position est valide dans la grille
     */
    private static boolean estPositionValide(Position pos, int largeur, int hauteur) {
        return pos != null &&
                pos.getX() >= 0 && pos.getX() < largeur &&
                pos.getY() >= 0 && pos.getY() < hauteur;
    }
}