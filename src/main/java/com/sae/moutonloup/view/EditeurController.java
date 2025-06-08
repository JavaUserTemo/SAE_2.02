package com.sae.moutonloup.view;

import com.sae.moutonloup.model.*;
import javafx.fxml.FXML;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import java.io.*;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Alert;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import com.sae.moutonloup.ia.AlgorithmeParcours;

public class EditeurController {
    private static final Map<String, Image> IMAGE_CACHE = new HashMap<>();

    private int toursRestantsChasseLoup = 0;
    private int nbTours = 0;
    private int nbHerbe = 0, nbCactus = 0, nbMarguerite = 0;

    private boolean estTourDuMouton = true;
    private Mouton mouton;
    private Loup loup;

    private ElementType elementSelectionne = ElementType.HERBE;
    private Element[][] grille;
    private int largeur = 10;
    private int hauteur = 10;

    private Sortie sortie;
    private boolean partieEnCours = false;

    @FXML
    private GridPane grillePane;

    public void initialize() {
        prechargerImages();
    }

    private void prechargerImages() {
        try {
            IMAGE_CACHE.put("herbe", new Image(getClass().getResource("/images/herbe.png").toExternalForm()));
            IMAGE_CACHE.put("cactus", new Image(getClass().getResource("/images/cactus.png").toExternalForm()));
            IMAGE_CACHE.put("marguerite", new Image(getClass().getResource("/images/marguerite.png").toExternalForm()));
            IMAGE_CACHE.put("rocher", new Image(getClass().getResource("/images/rocher.png").toExternalForm()));
            IMAGE_CACHE.put("sortie", new Image(getClass().getResource("/images/sortie.png").toExternalForm()));
            IMAGE_CACHE.put("mouton", new Image(getClass().getResource("/images/mouton.png").toExternalForm()));
            IMAGE_CACHE.put("loup", new Image(getClass().getResource("/images/loup.png").toExternalForm()));
        } catch (Exception e) {
            System.out.println("Problème images: " + e.getMessage());
        }
    }

    @FXML
    private void importerGrille() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Importer un labyrinthe");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Fichiers texte", "*.txt"));
        File fichier = fileChooser.showOpenDialog(grillePane.getScene().getWindow());

        if (fichier != null) {
            try (BufferedReader br = new BufferedReader(new FileReader(fichier))) {
                List<String> lignes = new ArrayList<>();
                String ligne;
                while ((ligne = br.readLine()) != null) {
                    lignes.add(ligne);
                }

                if (lignes.isEmpty()) {
                    System.out.println("Fichier vide");
                    return;
                }

                hauteur = lignes.size();
                largeur = lignes.get(0).length();
                grille = new Element[largeur][hauteur];

                for (int y = 0; y < hauteur; y++) {
                    String ligneActuelle = lignes.get(y);
                    for (int x = 0; x < Math.min(ligneActuelle.length(), largeur); x++) {
                        char c = ligneActuelle.charAt(x);
                        switch (c) {
                            case 'x' -> grille[x][y] = new Rocher();
                            case 'h' -> grille[x][y] = new Herbe();
                            case 'c' -> grille[x][y] = new Cactus();
                            case 'f' -> grille[x][y] = new Marguerite();
                            case 'm' -> {
                                mouton = new Mouton(new Position(x, y));
                                grille[x][y] = new Herbe();
                            }
                            case 'l' -> {
                                loup = new Loup(new Position(x, y));
                                grille[x][y] = new Herbe();
                            }
                            case 's' -> {
                                sortie = new Sortie(x, y);
                                grille[x][y] = sortie;
                            }
                            default -> grille[x][y] = new Herbe();
                        }
                    }
                }

                afficherGrille();

            } catch (IOException e) {
                System.out.println("Erreur importation: " + e.getMessage());
            }
        }
    }

    @FXML
    private void exporterGrille() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Exporter le labyrinthe");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Fichiers texte", "*.txt"));
        File fichier = fileChooser.showSaveDialog(grillePane.getScene().getWindow());

        if (fichier != null) {
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(fichier))) {
                for (int y = 0; y < hauteur; y++) {
                    StringBuilder ligne = new StringBuilder();
                    for (int x = 0; x < largeur; x++) {
                        if (mouton != null && mouton.getX() == x && mouton.getY() == y) {
                            ligne.append("m");
                        } else if (loup != null && loup.getX() == x && loup.getY() == y) {
                            ligne.append("l");
                        } else {
                            Element e = grille[x][y];
                            if (e instanceof Rocher) ligne.append("x");
                            else if (e instanceof Herbe) ligne.append("h");
                            else if (e instanceof Cactus) ligne.append("c");
                            else if (e instanceof Marguerite) ligne.append("f");
                            else if (e instanceof Sortie) ligne.append("s");
                            else ligne.append("h");
                        }
                    }
                    bw.write(ligne.toString());
                    bw.newLine();
                }
            } catch (IOException e) {
                System.out.println("Erreur export: " + e.getMessage());
            }
        }
    }

    public void setDimensions(int lignes, int colonnes) {
        this.largeur = colonnes;
        this.hauteur = lignes;
        grille = new Element[largeur][hauteur];
        genererGrilleParDefaut();
        afficherGrille();
    }

    @FXML
    private void rejouer() {
        partieEnCours = false;
        grille = new Element[largeur][hauteur];
        mouton = null;
        loup = null;
        sortie = null;
        nbTours = nbHerbe = nbCactus = nbMarguerite = 0;
        toursRestantsChasseLoup = 0;
        estTourDuMouton = true;
        genererGrilleParDefaut();
        afficherGrille();
    }

    private void genererGrilleParDefaut() {
        for (int y = 0; y < hauteur; y++) {
            for (int x = 0; x < largeur; x++) {
                if (x == 0 || y == 0 || x == largeur - 1 || y == hauteur - 1) {
                    grille[x][y] = new Rocher();
                } else {
                    grille[x][y] = new Herbe();
                }
            }
        }
    }

    private void afficherGrille() {
        grillePane.getChildren().clear();
        for (int y = 0; y < hauteur; y++) {
            for (int x = 0; x < largeur; x++) {
                ImageView imageView = new ImageView(getImageFromCache(x, y));
                imageView.setFitWidth(40);
                imageView.setFitHeight(40);
                int finalX = x;
                int finalY = y;
                imageView.setOnMouseClicked(e -> placerElement(finalX, finalY));
                grillePane.add(imageView, x, y);
            }
        }
    }

    private Image getImageFromCache(int x, int y) {
        if (mouton != null && mouton.getX() == x && mouton.getY() == y) {
            return IMAGE_CACHE.get("mouton");
        }
        if (loup != null && loup.getX() == x && loup.getY() == y) {
            return IMAGE_CACHE.get("loup");
        }

        Element element = grille[x][y];
        if (element instanceof Cactus) return IMAGE_CACHE.get("cactus");
        if (element instanceof Marguerite) return IMAGE_CACHE.get("marguerite");
        if (element instanceof Rocher) return IMAGE_CACHE.get("rocher");
        if (element instanceof Sortie) return IMAGE_CACHE.get("sortie");
        return IMAGE_CACHE.get("herbe");
    }

    private void placerElement(int x, int y) {
        if (partieEnCours) return;
        if (elementSelectionne == null) return;

        boolean bordure = (x == 0 || y == 0 || x == largeur - 1 || y == hauteur - 1);

        switch (elementSelectionne) {
            case HERBE -> {
                if (!bordure) grille[x][y] = new Herbe();
            }
            case CACTUS -> {
                if (!bordure) grille[x][y] = new Cactus();
            }
            case MARGUERITE -> {
                if (!bordure) grille[x][y] = new Marguerite();
            }
            case ROCHER -> {
                if (!bordure) grille[x][y] = new Rocher();
            }
            case MOUTON -> {
                if (!bordure && !(grille[x][y] instanceof Rocher)) {
                    mouton = new Mouton(new Position(x, y));
                    if (!(grille[x][y] instanceof Vegetal)) {
                        grille[x][y] = new Herbe();
                    }
                }
            }
            case LOUP -> {
                if (!bordure && !(grille[x][y] instanceof Rocher)) {
                    loup = new Loup(new Position(x, y));
                    if (!(grille[x][y] instanceof Vegetal)) {
                        grille[x][y] = new Herbe();
                    }
                }
            }
            case SORTIE -> {
                if (bordure && grille[x][y] instanceof Rocher) {
                    sortie = new Sortie(x, y);
                    grille[x][y] = sortie;
                }
            }
            case SUPPRIMER -> {
                if (mouton != null && mouton.getX() == x && mouton.getY() == y) {
                    mouton = null;
                } else if (loup != null && loup.getX() == x && loup.getY() == y) {
                    loup = null;
                } else if (grille[x][y] instanceof Sortie) {
                    sortie = null;
                    grille[x][y] = new Rocher();
                } else if (!bordure) {
                    grille[x][y] = new Herbe();
                }
            }
        }

        elementSelectionne = null;
        afficherGrille();
    }

    @FXML public void boutonLoup()       { elementSelectionne = ElementType.LOUP; }
    @FXML public void boutonMouton()     { elementSelectionne = ElementType.MOUTON; }
    @FXML public void boutonCactus()     { elementSelectionne = ElementType.CACTUS; }
    @FXML public void boutonMarguerite() { elementSelectionne = ElementType.MARGUERITE; }
    @FXML public void boutonRocher()     { elementSelectionne = ElementType.ROCHER; }
    @FXML public void boutonSupprimer()  { elementSelectionne = ElementType.SUPPRIMER; }
    @FXML public void boutonSortie()     { elementSelectionne = ElementType.SORTIE; }

    // Vérification connectivité
    private boolean estConnexeVersSortie() {
        if (sortie == null) return false;
        boolean[][] visite = new boolean[largeur][hauteur];
        explorerDepuisSortie(sortie.getX(), sortie.getY(), visite);

        for (int y = 0; y < hauteur; y++) {
            for (int x = 0; x < largeur; x++) {
                if (!(grille[x][y] instanceof Rocher) && !visite[x][y]) {
                    return false;
                }
            }
        }
        return true;
    }

    private void explorerDepuisSortie(int x, int y, boolean[][] visite) {
        if (x < 0 || y < 0 || x >= largeur || y >= hauteur) return;
        if (visite[x][y]) return;
        if (grille[x][y] instanceof Rocher) return;

        visite[x][y] = true;
        explorerDepuisSortie(x + 1, y, visite);
        explorerDepuisSortie(x - 1, y, visite);
        explorerDepuisSortie(x, y + 1, visite);
        explorerDepuisSortie(x, y - 1, visite);
    }

    @FXML
    private void demarrerPartie() {
        if (!verifierPreconditions()) return;

        partieEnCours = true;
        estTourDuMouton = true;
        nbTours = 0;
        nbHerbe = nbCactus = nbMarguerite = 0;
        toursRestantsChasseLoup = 0;

        new Thread(this::jouerAuto).start();
    }

    private boolean verifierPreconditions() {
        if (mouton == null) {
            afficherErreur("Il faut placer un mouton !");
            return false;
        }
        if (loup == null) {
            afficherErreur("Il faut placer un loup !");
            return false;
        }
        if (sortie == null) {
            afficherErreur("Il faut placer une sortie !");
            return false;
        }
        if (!estConnexeVersSortie()) {
            afficherErreur("Le labyrinthe n'est pas connexe vers la sortie !");
            return false;
        }
        return true;
    }

    private void afficherErreur(String message) {
        javafx.application.Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText("Impossible de continuer");
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    private Position choisirDeplacementAleatoire(Animal animal) {
        ArrayList<Position> accessibles = new ArrayList<>();
        int vitesse = animal.getVitesse();
        int x0 = animal.getX(), y0 = animal.getY();

        for (int dx = -vitesse; dx <= vitesse; dx++) {
            for (int dy = -vitesse; dy <= vitesse; dy++) {
                if (Math.abs(dx) + Math.abs(dy) == vitesse) {
                    int x = x0 + dx;
                    int y = y0 + dy;

                    if (x >= 0 && x < largeur && y >= 0 && y < hauteur) {
                        if (estPositionValideEtAccessible(x, y, animal)) {
                            accessibles.add(new Position(x, y));
                        }
                    }
                }
            }
        }

        if (accessibles.isEmpty()) return null;
        return accessibles.get(new java.util.Random().nextInt(accessibles.size()));
    }

    private boolean estPositionValideEtAccessible(int x, int y, Animal animal) {
        if (x < 0 || y < 0 || x >= largeur || y >= hauteur) return false;
        if (grille[x][y] instanceof Rocher) return false;

        if (animal instanceof Mouton && loup != null && loup.getX() == x && loup.getY() == y) {
            return false;
        }

        if (animal instanceof Loup && grille[x][y] instanceof Sortie) {
            return mouton != null && mouton.getX() == x && mouton.getY() == y;
        }

        return true;
    }

    private Position choisirDeplacementIntelligent(Animal animal, Position objectif) {
        List<Position> chemin;

        if (animal instanceof Mouton) {
            chemin = AlgorithmeParcours.aStar(animal.getPosition(), objectif, grille);
        } else {
            chemin = AlgorithmeParcours.dijkstra(animal.getPosition(), objectif, grille);
        }

        if (chemin.isEmpty()) return choisirDeplacementAleatoire(animal);

        Position positionActuelle = animal.getPosition();
        int vitesse = animal.getVitesse();

        // Chercher dans le chemin
        for (Position pos : chemin) {
            int distance = positionActuelle.distanceManhattan(pos);
            if (distance == vitesse) {
                if (estPositionValideEtAccessible(pos.getX(), pos.getY(), animal)) {
                    return pos;
                }
            }
        }

        // Recherche directionnelle
        Position meilleurePosition = null;
        int meilleureDistanceVersObjectif = Integer.MAX_VALUE;

        for (int dx = -vitesse; dx <= vitesse; dx++) {
            for (int dy = -vitesse; dy <= vitesse; dy++) {
                if (Math.abs(dx) + Math.abs(dy) == vitesse) {
                    int x = positionActuelle.getX() + dx;
                    int y = positionActuelle.getY() + dy;

                    if (estPositionValideEtAccessible(x, y, animal)) {
                        Position candidat = new Position(x, y);
                        int distanceVersObjectif = candidat.distanceManhattan(objectif);

                        if (distanceVersObjectif < meilleureDistanceVersObjectif) {
                            meilleureDistanceVersObjectif = distanceVersObjectif;
                            meilleurePosition = candidat;
                        }
                    }
                }
            }
        }

        return meilleurePosition != null ? meilleurePosition : choisirDeplacementAleatoire(animal);
    }

    private boolean aLigneDeVue(Position a, Position b) {
        if (a.getX() == b.getX()) {
            int x = a.getX();
            int minY = Math.min(a.getY(), b.getY());
            int maxY = Math.max(a.getY(), b.getY());
            for (int y = minY + 1; y < maxY; y++) {
                if (grille[x][y] instanceof Rocher) return false;
            }
            return true;
        }

        if (a.getY() == b.getY()) {
            int y = a.getY();
            int minX = Math.min(a.getX(), b.getX());
            int maxX = Math.max(a.getX(), b.getX());
            for (int x = minX + 1; x < maxX; x++) {
                if (grille[x][y] instanceof Rocher) return false;
            }
            return true;
        }

        return false;
    }

    private void jouerAuto() {
        while (partieEnCours) {
            if (mouton == null || loup == null || sortie == null) break;

            if (mouton.getX() == loup.getX() && mouton.getY() == loup.getY()) {
                javafx.application.Platform.runLater(() -> finDePartie("Le loup a gagné !"));
                break;
            }

            if (mouton.getX() == sortie.getX() && mouton.getY() == sortie.getY()) {
                javafx.application.Platform.runLater(() -> finDePartie("Le mouton a gagné !"));
                break;
            }

            boolean deplacementOk = false;

            if (estTourDuMouton) {
                Position nouvellePos = null;

                if (mouton.getPosition().distanceManhattan(loup.getPosition()) <= 5 &&
                        aLigneDeVue(mouton.getPosition(), loup.getPosition())) {
                    nouvellePos = choisirDeplacementIntelligent(mouton, new Position(sortie.getX(), sortie.getY()));
                } else {
                    nouvellePos = choisirDeplacementAleatoire(mouton);
                }

                if (nouvellePos != null) {
                    deplacementOk = deplacerMouton(nouvellePos.getX(), nouvellePos.getY());
                    if (deplacementOk) estTourDuMouton = false;
                }
            } else {
                Position nouvellePos = null;

                if (loup.getPosition().distanceManhattan(mouton.getPosition()) <= 5 &&
                        aLigneDeVue(loup.getPosition(), mouton.getPosition())) {
                    toursRestantsChasseLoup = 3;
                }

                if (toursRestantsChasseLoup > 0) {
                    nouvellePos = choisirDeplacementIntelligent(loup, mouton.getPosition());
                    toursRestantsChasseLoup--;
                } else {
                    nouvellePos = choisirDeplacementAleatoire(loup);
                }

                if (nouvellePos != null) {
                    deplacementOk = deplacerLoup(nouvellePos.getX(), nouvellePos.getY());
                    if (deplacementOk) estTourDuMouton = true;
                }
            }

            if (!deplacementOk) {
                javafx.application.Platform.runLater(() -> finDePartie("Partie bloquée"));
                break;
            }

            if (nbTours % 2 == 0) {
                javafx.application.Platform.runLater(this::afficherGrille);
            }

            try {
                Thread.sleep(800);
            } catch (InterruptedException e) {
                break;
            }
        }
    }

    private enum ElementType {
        HERBE, CACTUS, MARGUERITE, ROCHER, MOUTON, LOUP, SUPPRIMER, SORTIE
    }

    private void finDePartie(String gagnant) {
        partieEnCours = false;
        javafx.application.Platform.runLater(this::afficherGrille);

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Fin de partie");
        alert.setHeaderText(gagnant);
        alert.setContentText(
                "Tours : " + nbTours + "\n" +
                        "Herbe : " + nbHerbe + "\n" +
                        "Cactus : " + nbCactus + "\n" +
                        "Marguerite : " + nbMarguerite + "\n\n" +
                        "Rejouer ?"
        );

        ButtonType boutonRejouer = new ButtonType("Rejouer");
        ButtonType boutonQuitter = new ButtonType("Quitter", javafx.scene.control.ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(boutonRejouer, boutonQuitter);

        alert.showAndWait().ifPresent(reponse -> {
            if (reponse == boutonRejouer) {
                rejouer();
            } else {
                javafx.stage.Stage stage = (javafx.stage.Stage) grillePane.getScene().getWindow();
                stage.close();
            }
        });
    }

    private boolean deplacerMouton(int x, int y) {
        Position nouvellePos = new Position(x, y);
        Position posActuelle = mouton.getPosition();

        int distance = posActuelle.distanceManhattan(nouvellePos);
        if (distance != mouton.getVitesse()) return false;
        if (!estPositionValideEtAccessible(x, y, mouton)) return false;

        Element caseDestination = grille[x][y];
        if (caseDestination instanceof Vegetal vegetal) {
            mouton.manger(vegetal);
            if (vegetal instanceof Herbe) nbHerbe++;
            else if (vegetal instanceof Cactus) nbCactus++;
            else if (vegetal instanceof Marguerite) nbMarguerite++;

            grille[x][y] = genererVegetalAleatoire();
        }

        mouton.setPosition(nouvellePos);
        nbTours++;
        return true;
    }

    private boolean deplacerLoup(int x, int y) {
        Position nouvellePos = new Position(x, y);
        Position posActuelle = loup.getPosition();

        if (mouton != null && mouton.getX() == x && mouton.getY() == y) {
            int distance = posActuelle.distanceManhattan(nouvellePos);
            if (distance != loup.getVitesse()) return false;
            loup.setPosition(nouvellePos);
            nbTours++;
            return true;
        }

        int distance = posActuelle.distanceManhattan(nouvellePos);
        if (distance != loup.getVitesse()) return false;
        if (!estPositionValideEtAccessible(x, y, loup)) return false;

        loup.setPosition(nouvellePos);
        nbTours++;
        return true;
    }

    private Vegetal genererVegetalAleatoire() {
        int r = new java.util.Random().nextInt(3);
        return switch (r) {
            case 0 -> new Herbe();
            case 1 -> new Marguerite();
            default -> new Cactus();
        };
    }
}