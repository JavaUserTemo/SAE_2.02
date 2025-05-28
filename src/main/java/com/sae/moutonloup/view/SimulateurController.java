package com.sae.moutonloup.view;

import com.sae.moutonloup.control.Simulateur;
import com.sae.moutonloup.model.*;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.util.ArrayList;

public class SimulateurController {

    @FXML
    private GridPane grilleLabyrinthe;

    @FXML
    private Button boutonDemarrer;

    private Labyrinthe labyrinthe;
    private Simulateur simulateur;
    private boolean simulationLancee = false;
    private boolean tourDuLoup = true;

    @FXML
    public void initialize() {
        labyrinthe = new Labyrinthe(10, 10);
        labyrinthe.placerMouton(1, 1);
        labyrinthe.placerLoup(8, 8);

        simulateur = new Simulateur(labyrinthe, labyrinthe.getMouton(), labyrinthe.getLoup());

        boutonDemarrer.setOnAction(e -> {
            simulationLancee = true;
            tourDuLoup = true;
            afficherGrille();
        });

        afficherGrille();
    }

    private void afficherGrille() {
        grilleLabyrinthe.getChildren().clear();

        for (int y = 0; y < labyrinthe.getNbLignes(); y++) {
            for (int x = 0; x < labyrinthe.getNbColonnes(); x++) {
                StackPane cellule = new StackPane();
                Rectangle fond = new Rectangle(40, 40);
                fond.setStroke(Color.GRAY);

                Element element = labyrinthe.getElement(x, y);
                if (element instanceof Rocher) fond.setFill(Color.DARKGRAY);
                else if (element instanceof Herbe) fond.setFill(Color.LIGHTGREEN);
                else if (element instanceof Marguerite) fond.setFill(Color.PINK);
                else if (element instanceof Cactus) fond.setFill(Color.LIMEGREEN);
                else fond.setFill(Color.WHITE);

                Position moutonPos = labyrinthe.getMouton().getPosition();
                Position loupPos = labyrinthe.getLoup().getPosition();

                if (moutonPos.getX() == x && moutonPos.getY() == y)
                    fond.setFill(Color.BLUE);
                else if (loupPos.getX() == x && loupPos.getY() == y)
                    fond.setFill(Color.RED);

                cellule.getChildren().add(fond);
                int finalX = x;
                int finalY = y;

                cellule.setOnMouseClicked(e -> {
                    if (!simulationLancee) return;

                    Animal actif = tourDuLoup ? labyrinthe.getLoup() : labyrinthe.getMouton();
                    if (deplacementPossible(actif, finalX, finalY)) {
                        actif.setPosition(new Position(finalX, finalY));

                        if (!tourDuLoup) simulateur.tourMouton();
                        else simulateur.tourLoup();

                        simulateur.incrementerTour();
                        tourDuLoup = !tourDuLoup;
                        afficherGrille();
                    }
                });

                grilleLabyrinthe.add(cellule, x, y);
            }
        }

        if (simulationLancee) {
            Animal actif = tourDuLoup ? labyrinthe.getLoup() : labyrinthe.getMouton();
            for (Position p : casesAccessibles(actif)) {
                StackPane cellule = getCaseAt(p.getX(), p.getY());
                if (cellule != null) {
                    Rectangle overlay = new Rectangle(40, 40, Color.rgb(0, 0, 255, 0.3));
                    cellule.getChildren().add(overlay);
                }
            }
        }
    }

    private boolean deplacementPossible(Animal animal, int x, int y) {
        Position pos = animal.getPosition();
        int vitesse = animal.getVitesse();
        int dx = x - pos.getX();
        int dy = y - pos.getY();
        return Math.abs(dx) + Math.abs(dy) == vitesse &&
                labyrinthe.getCase(x, y).estAccessible();
    }

    private ArrayList<Position> casesAccessibles(Animal animal) {
        ArrayList<Position> acces = new ArrayList<>();
        int vitesse = animal.getVitesse();
        int x0 = animal.getX(), y0 = animal.getY();

        for (int dx = -vitesse; dx <= vitesse; dx++) {
            for (int dy = -vitesse; dy <= vitesse; dy++) {
                if (Math.abs(dx) + Math.abs(dy) == vitesse) {
                    int x = x0 + dx;
                    int y = y0 + dy;
                    if (x >= 0 && x < labyrinthe.getNbColonnes() && y >= 0 && y < labyrinthe.getNbLignes()) {
                        if (labyrinthe.getCase(x, y).estAccessible()) {
                            acces.add(new Position(x, y));
                        }
                    }
                }
            }
        }
        return acces;
    }

    private StackPane getCaseAt(int x, int y) {
        for (javafx.scene.Node node : grilleLabyrinthe.getChildren()) {
            if (GridPane.getColumnIndex(node) == x && GridPane.getRowIndex(node) == y) {
                return (StackPane) node;
            }
        }
        return null;
    }
}
