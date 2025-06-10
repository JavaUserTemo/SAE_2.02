package com.sae.moutonloup.ia;

import com.sae.moutonloup.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;
import java.util.List;


public class AlgorithmeParcoursTest {

    private Element[][] grilleSimple;
    private Element[][] grilleAvecObstacles;
    private Element[][] grilleImpossible;
    private Mouton mouton;
    private Loup loup;

    @BeforeEach
    void setUp() {
        // Grille simple 5x5 sans obstacles
        grilleSimple = new Element[5][5];
        for (int x = 0; x < 5; x++) {
            for (int y = 0; y < 5; y++) {
                grilleSimple[x][y] = new Herbe();
            }
        }

        // Grille 5x5 avec obstacles
        grilleAvecObstacles = new Element[5][5];
        for (int x = 0; x < 5; x++) {
            for (int y = 0; y < 5; y++) {
                grilleAvecObstacles[x][y] = new Herbe();
            }
        }
        // Ajouter des rochers
        grilleAvecObstacles[2][1] = new Rocher();
        grilleAvecObstacles[2][2] = new Rocher();
        grilleAvecObstacles[2][3] = new Rocher();

        // Grille impossible (totalement bloquée)
        grilleImpossible = new Element[3][3];
        for (int x = 0; x < 3; x++) {
            for (int y = 0; y < 3; y++) {
                grilleImpossible[x][y] = new Rocher();
            }
        }

        mouton = new Mouton(new Position(0, 0));
        loup = new Loup(new Position(0, 0));
    }

    // ====== TESTS A* ======
    @Test
    @DisplayName("A* - Chemin simple sans obstacles")
    void testAStarCheminSimple() {
        Position depart = new Position(0, 0);
        Position objectif = new Position(4, 4);

        List<Position> chemin = AlgorithmeParcours.aStar(depart, objectif, grilleSimple, mouton, loup, mouton);

        assertFalse(chemin.isEmpty(), "Le chemin ne doit pas être vide");
        assertEquals(objectif, chemin.get(chemin.size() - 1), "Le dernier élément doit être l'objectif");
        assertTrue(chemin.size() <= 8, "Le chemin doit être optimal (distance Manhattan = 8)");
    }

    @Test
    @DisplayName("A* - Contournement d'obstacles")
    void testAStarAvecObstacles() {
        Position depart = new Position(0, 2);
        Position objectif = new Position(4, 2);

        List<Position> chemin = AlgorithmeParcours.aStar(depart, objectif, grilleAvecObstacles, mouton, loup, mouton);

        assertFalse(chemin.isEmpty(), "Un chemin doit être trouvé malgré les obstacles");
        assertEquals(objectif, chemin.get(chemin.size() - 1), "L'objectif doit être atteint");

        // Vérifier qu'aucune position du chemin n'est un rocher
        for (Position pos : chemin) {
            assertFalse(grilleAvecObstacles[pos.getX()][pos.getY()] instanceof Rocher,
                    "Le chemin ne doit pas passer par des rochers");
        }
    }

    @Test
    @DisplayName("A* - Chemin impossible")
    void testAStarCheminImpossible() {
        Position depart = new Position(0, 0);
        Position objectif = new Position(2, 2);

        List<Position> chemin = AlgorithmeParcours.aStar(depart, objectif, grilleImpossible, mouton, loup, mouton);

        assertTrue(chemin.isEmpty(), "Aucun chemin ne doit être trouvé dans une grille bloquée");
    }

    @Test
    @DisplayName("A* - Paramètres null")
    void testAStarParametresNull() {
        Position depart = new Position(0, 0);
        Position objectif = new Position(1, 1);

        List<Position> chemin1 = AlgorithmeParcours.aStar(null, objectif, grilleSimple, mouton, loup, mouton);
        List<Position> chemin2 = AlgorithmeParcours.aStar(depart, null, grilleSimple, mouton, loup, mouton);
        List<Position> chemin3 = AlgorithmeParcours.aStar(depart, objectif, null, mouton, loup, mouton);

        assertTrue(chemin1.isEmpty(), "Retourner liste vide si départ null");
        assertTrue(chemin2.isEmpty(), "Retourner liste vide si objectif null");
        assertTrue(chemin3.isEmpty(), "Retourner liste vide si grille null");
    }

    // ====== TESTS DIJKSTRA ======
    @Test
    @DisplayName("Dijkstra - Chemin optimal")
    void testDijkstraCheminOptimal() {
        Position depart = new Position(0, 0);
        Position objectif = new Position(2, 2);

        List<Position> chemin = AlgorithmeParcours.dijkstra(depart, objectif, grilleSimple, mouton, loup, mouton);

        assertFalse(chemin.isEmpty(), "Dijkstra doit trouver un chemin");
        assertEquals(objectif, chemin.get(chemin.size() - 1), "L'objectif doit être atteint");
        assertEquals(4, chemin.size(), "Le chemin doit être optimal (4 étapes pour distance Manhattan = 4)");
    }

    @Test
    @DisplayName("Dijkstra - Avec obstacles")
    void testDijkstraAvecObstacles() {
        Position depart = new Position(1, 2);
        Position objectif = new Position(3, 2);

        List<Position> chemin = AlgorithmeParcours.dijkstra(depart, objectif, grilleAvecObstacles, mouton, loup, mouton);

        assertFalse(chemin.isEmpty(), "Dijkstra doit contourner les obstacles");
        assertEquals(objectif, chemin.get(chemin.size() - 1), "L'objectif doit être atteint");
    }

    // ====== TESTS BFS ======
    @Test
    @DisplayName("BFS - Plus court chemin")
    void testBFSPlusCourtChemin() {
        Position depart = new Position(0, 0);
        Position objectif = new Position(1, 1);

        List<Position> chemin = AlgorithmeParcours.parcoursenLargeur(depart, objectif, grilleSimple, mouton, loup, mouton);

        assertFalse(chemin.isEmpty(), "BFS doit trouver un chemin");
        assertEquals(objectif, chemin.get(chemin.size() - 1), "L'objectif doit être atteint");
        assertEquals(2, chemin.size(), "BFS doit trouver le plus court chemin (2 étapes)");
    }

    @Test
    @DisplayName("BFS - Exploration exhaustive")
    void testBFSExplorationExhaustive() {
        Position depart = new Position(0, 0);
        Position objectif = new Position(4, 0);

        List<Position> chemin = AlgorithmeParcours.parcoursenLargeur(depart, objectif, grilleSimple, mouton, loup, mouton);

        assertFalse(chemin.isEmpty(), "BFS doit explorer toutes les possibilités");
        assertEquals(4, chemin.size(), "Chemin direct de 4 étapes");
    }

    // ====== TESTS DFS ======
    @Test
    @DisplayName("DFS - Trouve un chemin")
    void testDFSTrouveUnChemin() {
        Position depart = new Position(0, 0);
        Position objectif = new Position(2, 2);

        List<Position> chemin = AlgorithmeParcours.parcoursEnProfondeur(depart, objectif, grilleSimple, mouton, loup, mouton);

        assertFalse(chemin.isEmpty(), "DFS doit trouver un chemin");
        assertEquals(objectif, chemin.get(chemin.size() - 1), "L'objectif doit être atteint");
    }

    @Test
    @DisplayName("DFS - Peut être plus long que optimal")
    void testDFSPeutEtreLong() {
        Position depart = new Position(0, 0);
        Position objectif = new Position(1, 0);

        List<Position> chemin = AlgorithmeParcours.parcoursEnProfondeur(depart, objectif, grilleSimple, mouton, loup, mouton);

        assertFalse(chemin.isEmpty(), "DFS doit trouver un chemin");
        assertEquals(objectif, chemin.get(chemin.size() - 1), "L'objectif doit être atteint");
        // DFS peut trouver un chemin plus long que optimal
        assertTrue(chemin.size() >= 1, "Chemin d'au moins 1 étape");
    }

    // ====== TESTS MÉTHODES UTILITAIRES ======
    @Test
    @DisplayName("Déplacement valide - Cases adjacentes")
    void testDeplacementValide() {
        Position depart = new Position(2, 2);
        Position adjacente = new Position(2, 3);
        Position nonAdjacente = new Position(4, 4);

        assertTrue(AlgorithmeParcours.estDeplacementValide(depart, adjacente, grilleSimple, mouton, mouton, loup),
                "Déplacement vers case adjacente doit être valide");

        assertFalse(AlgorithmeParcours.estDeplacementValide(depart, nonAdjacente, grilleSimple, mouton, mouton, loup),
                "Déplacement vers case non-adjacente doit être invalide");
    }

    @Test
    @DisplayName("Déplacement valide - Rochers bloqués")
    void testDeplacementBloqueParRocher() {
        Position depart = new Position(1, 2);
        Position rocher = new Position(2, 2);

        assertFalse(AlgorithmeParcours.estDeplacementValide(depart, rocher, grilleAvecObstacles, mouton, mouton, loup),
                "Ne peut pas se déplacer vers un rocher");
    }

    @Test
    @DisplayName("Meilleure position - Calcul optimal")
    void testTrouverMeilleurePosition() {
        Position objectif = new Position(4, 4);
        mouton.setPosition(new Position(0, 0));

        Position meilleure = AlgorithmeParcours.trouverMeilleurePosition(mouton, objectif, grilleSimple, mouton, loup);

        assertNotNull(meilleure, "Une meilleure position doit être trouvée");
        // La position doit être dans la direction de l'objectif
        assertTrue(meilleure.getX() >= 0 && meilleure.getX() < 5, "Position X valide");
        assertTrue(meilleure.getY() >= 0 && meilleure.getY() < 5, "Position Y valide");
    }

    @Test
    @DisplayName("Positions accessibles - Dans rayon donné")
    void testTrouverPositionsAccessibles() {
        Position centre = new Position(2, 2);
        int distance = 2;

        List<Position> positions = AlgorithmeParcours.trouverPositionsAccessibles(centre, distance, grilleSimple, mouton, mouton, loup);

        assertFalse(positions.isEmpty(), "Des positions accessibles doivent être trouvées");

        // Vérifier que toutes les positions sont dans le rayon
        for (Position pos : positions) {
            assertTrue(centre.distanceManhattan(pos) <= distance,
                    "Toutes les positions doivent être dans le rayon spécifié");
        }
    }

    // ====== TESTS DE PERFORMANCE ======
    @Test
    @DisplayName("Performance - Grille large")
    void testPerformanceGrilleLarge() {
        // Créer une grille 20x20
        Element[][] grilleLarge = new Element[20][20];
        for (int x = 0; x < 20; x++) {
            for (int y = 0; y < 20; y++) {
                grilleLarge[x][y] = new Herbe();
            }
        }

        Position depart = new Position(0, 0);
        Position objectif = new Position(19, 19);

        long debut = System.currentTimeMillis();
        List<Position> chemin = AlgorithmeParcours.aStar(depart, objectif, grilleLarge, mouton, loup, mouton);
        long duree = System.currentTimeMillis() - debut;

        assertFalse(chemin.isEmpty(), "Un chemin doit être trouvé même sur grande grille");
        assertTrue(duree < 1000, "L'algorithme doit être rapide (< 1 seconde)");
        System.out.println("Durée A* sur grille 20x20: " + duree + "ms");
    }

    // ====== TEST DE COMPARAISON DES ALGORITHMES ======
    @Test
    @DisplayName("Comparaison - Tous les algorithmes trouvent un chemin")
    void testComparaisonAlgorithmes() {
        Position depart = new Position(0, 0);
        Position objectif = new Position(3, 3);

        List<Position> cheminAStar = AlgorithmeParcours.aStar(depart, objectif, grilleSimple, mouton, loup, mouton);
        List<Position> cheminDijkstra = AlgorithmeParcours.dijkstra(depart, objectif, grilleSimple, mouton, loup, mouton);
        List<Position> cheminBFS = AlgorithmeParcours.parcoursenLargeur(depart, objectif, grilleSimple, mouton, loup, mouton);
        List<Position> cheminDFS = AlgorithmeParcours.parcoursEnProfondeur(depart, objectif, grilleSimple, mouton, loup, mouton);

        assertFalse(cheminAStar.isEmpty(), "A* doit trouver un chemin");
        assertFalse(cheminDijkstra.isEmpty(), "Dijkstra doit trouver un chemin");
        assertFalse(cheminBFS.isEmpty(), "BFS doit trouver un chemin");
        assertFalse(cheminDFS.isEmpty(), "DFS doit trouver un chemin");

        // A* et Dijkstra doivent être optimaux
        assertEquals(cheminAStar.size(), cheminBFS.size(), "A* et BFS doivent donner des chemins de même longueur");
        assertEquals(cheminDijkstra.size(), cheminBFS.size(), "Dijkstra et BFS doivent donner des chemins de même longueur");

        System.out.println("Longueurs des chemins trouvés:");
        System.out.println("A*: " + cheminAStar.size());
        System.out.println("Dijkstra: " + cheminDijkstra.size());
        System.out.println("BFS: " + cheminBFS.size());
        System.out.println("DFS: " + cheminDFS.size());
    }
}