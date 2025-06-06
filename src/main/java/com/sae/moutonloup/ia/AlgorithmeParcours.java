package com.sae.moutonloup.ia;

import com.sae.moutonloup.model.*;

import java.util.*;

public class AlgorithmeParcours {

    public static List<Position> dijkstra(Position depart, Position objectif, Element[][] grille) {
        int largeur = grille.length;
        int hauteur = grille[0].length;

        Map<Position, Position> precedent = new HashMap<>();
        Map<Position, Integer> distance = new HashMap<>();
        PriorityQueue<Position> file = new PriorityQueue<>(Comparator.comparingInt(distance::get));

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

            if (courant.equals(objectif)) break;

            for (Position voisin : voisinsAccessibles(courant, grille, true)) {

                int nouvDist = distance.get(courant) + 1;
                if (nouvDist < distance.get(voisin)) {
                    distance.put(voisin, nouvDist);
                    precedent.put(voisin, courant);
                    file.add(voisin);
                }
            }
        }


        LinkedList<Position> chemin = new LinkedList<>();
        Position p = objectif;
        while (precedent.containsKey(p)) {
            chemin.addFirst(p);
            p = precedent.get(p);
        }

        return chemin;
    }

    public static List<Position> aStar(Position depart, Position objectif, Element[][] grille) {
        int largeur = grille.length;
        int hauteur = grille[0].length;

        Map<Position, Integer> gScore = new HashMap<>();
        Map<Position, Integer> fScore = new HashMap<>();
        Map<Position, Position> precedent = new HashMap<>();

        gScore.put(depart, 0);
        fScore.put(depart, depart.distanceManhattan(objectif));

        PriorityQueue<Position> file = new PriorityQueue<>(Comparator.comparingInt(fScore::get));
        file.add(depart);

        while (!file.isEmpty()) {
            Position courant = file.poll();


            if (courant.equals(objectif)) break;

            for (Position voisin : voisinsAccessibles(courant, grille, false)) {

                int tentativeG = gScore.get(courant) + 1;
                if (tentativeG < gScore.getOrDefault(voisin, Integer.MAX_VALUE)) {
                    precedent.put(voisin, courant);
                    gScore.put(voisin, tentativeG);
                    fScore.put(voisin, tentativeG + voisin.distanceManhattan(objectif));
                    if (!file.contains(voisin)) {
                        file.add(voisin);
                    }
                }
            }
        }
        if (!precedent.containsKey(objectif)) {
            System.out.println("❌ Objectif non atteint pendant A* : " + objectif);
        }


        LinkedList<Position> chemin = new LinkedList<>();
        Position p = objectif;
        while (precedent.containsKey(p)) {
            chemin.addFirst(p);
            p = precedent.get(p);
        }

        return chemin;
    }

    private static List<Position> voisinsAccessibles(Position pos, Element[][] grille, boolean estLoup) {
        List<Position> voisins = new ArrayList<>();
        int x = pos.getX(), y = pos.getY();
        int largeur = grille.length;
        int hauteur = grille[0].length;

        int[][] directions = {{1,0},{-1,0},{0,1},{0,-1}};
        for (int[] dir : directions) {
            int nx = x + dir[0], ny = y + dir[1];
            if (nx >= 0 && ny >= 0 && nx < largeur && ny < hauteur) {
                Element e = grille[nx][ny];


                if (!(e instanceof Rocher) && (!(e instanceof Sortie) || !estLoup)) {
                    voisins.add(new Position(nx, ny));
                }
            }
        }

        return voisins;
    }}

