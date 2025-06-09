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

    private int nbTours = 0;
    private int nbHerbe = 0, nbCactus = 0, nbMarguerite = 0;

    private Mouton mouton;
    private Loup loup;

    private ElementType elementSelectionne = ElementType.HERBE;
    private Element[][] grille;
    private int largeur = 10;
    private int hauteur = 10;

    private Sortie sortie;
    private boolean partieEnCours = false;

    // États des animaux
    private boolean loupEnChasse = false;
    private boolean moutonEnFuite = false;

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
        loupEnChasse = false;
        moutonEnFuite = false;
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
                    System.out.println("Sortie placée en: " + x + "," + y);
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
        nbTours = 0;
        nbHerbe = nbCactus = nbMarguerite = 0;
        loupEnChasse = false;
        moutonEnFuite = false;

        System.out.println("=== DÉMARRAGE DE LA PARTIE ===");
        System.out.println("Mouton: " + mouton.getPosition());
        System.out.println("Loup: " + loup.getPosition());
        System.out.println("Sortie: " + sortie.getX() + "," + sortie.getY());

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

    private boolean peutVoir(Position a, Position b) {
        if (a == null || b == null) return false;

        int distance = a.distanceManhattan(b);
        System.out.println("Distance entre " + a + " et " + b + ": " + distance);

        if (distance > 5) {
            System.out.println("Trop loin pour voir (distance > 5)");
            return false;
        }

        boolean obstacle = aObstacleSurLigneDeVue(a, b);
        System.out.println("Obstacle sur ligne de vue: " + obstacle);

        return !obstacle;
    }

    private boolean aObstacleSurLigneDeVue(Position a, Position b) {
        int x0 = a.getX(), y0 = a.getY();
        int x1 = b.getX(), y1 = b.getY();

        int dx = Math.abs(x1 - x0);
        int dy = Math.abs(y1 - y0);
        int sx = x0 < x1 ? 1 : -1;
        int sy = y0 < y1 ? 1 : -1;
        int err = dx - dy;

        int x = x0, y = y0;

        while (true) {
            // Ne pas vérifier les positions de départ et d'arrivée
            if ((x != x0 || y != y0) && (x != x1 || y != y1)) {
                if (x >= 0 && x < largeur && y >= 0 && y < hauteur) {
                    if (grille[x][y] instanceof Rocher) {
                        return true; // Obstacle trouvé
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

        return false; // Pas d'obstacle
    }

    private Position choisirDeplacementIntelligent(Animal animal, Position objectif) {
        if (objectif == null) {
            System.out.println("Objectif null, déplacement aléatoire");
            return choisirDeplacementAleatoire(animal);
        }

        Position posActuelle = animal.getPosition();
        int vitesse = animal.getVitesse();

        System.out.println("Animal: " + animal.getClass().getSimpleName() +
                " Position: " + posActuelle +
                " Objectif: " + objectif +
                " Vitesse: " + vitesse);

        // Calculer la direction générale vers l'objectif
        int deltaX = objectif.getX() - posActuelle.getX();
        int deltaY = objectif.getY() - posActuelle.getY();

        System.out.println("Delta vers objectif: deltaX=" + deltaX + ", deltaY=" + deltaY);

        // Générer tous les déplacements possibles à la distance exacte de la vitesse
        List<Position> candidats = new ArrayList<>();

        for (int dx = -vitesse; dx <= vitesse; dx++) {
            for (int dy = -vitesse; dy <= vitesse; dy++) {
                if (Math.abs(dx) + Math.abs(dy) == vitesse) {
                    int newX = posActuelle.getX() + dx;
                    int newY = posActuelle.getY() + dy;
                    Position candidat = new Position(newX, newY);

                    // DEBUG : Afficher TOUS les candidats testés
                    System.out.println("Test candidat: " + candidat + " - Valide: " + estPositionValideSimple(candidat, animal));

                    if (estPositionValideSimple(candidat, animal)) {
                        candidats.add(candidat);
                        System.out.println("Candidat valide: " + candidat +
                                " (distance vers objectif: " + candidat.distanceManhattan(objectif) + ")");
                    }
                }
            }
        }

        if (candidats.isEmpty()) {
            System.out.println("Aucun candidat valide trouvé !");
            return null;
        }

        // Choisir le candidat le plus proche de l'objectif
        Position meilleur = null;
        int meilleureDistance = Integer.MAX_VALUE;

        for (Position candidat : candidats) {
            int distance = candidat.distanceManhattan(objectif);
            if (distance < meilleureDistance) {
                meilleureDistance = distance;
                meilleur = candidat;
            }
        }

        System.out.println("Meilleur choix: " + meilleur + " (distance vers objectif: " + meilleureDistance + ")");
        return meilleur;
    }

    // MÉTHODE SIMPLIFIÉE POUR VÉRIFIER SI UNE POSITION EST VALIDE
    private boolean estPositionValideSimple(Position pos, Animal animal) {
        // Vérifier les limites
        if (pos.getX() < 0 || pos.getX() >= largeur || pos.getY() < 0 || pos.getY() >= hauteur) {
            return false;
        }

        // Vérifier que ce n'est pas un rocher
        Element element = grille[pos.getX()][pos.getY()];
        if (element instanceof Rocher) {
            return false;
        }

        // Le loup ne peut pas aller sur la sortie (sauf si le mouton y est)
        if (animal instanceof Loup && element instanceof Sortie) {
            if (mouton != null) {
                return mouton.getX() == pos.getX() && mouton.getY() == pos.getY();
            }
            return false;
        }

        return true;
    }

    private Position choisirDeplacementAleatoire(Animal animal) {
        List<Position> positionsValides = new ArrayList<>();
        Position posActuelle = animal.getPosition();
        int vitesse = animal.getVitesse();

        // Chercher toutes les positions valides à la distance exacte de la vitesse
        for (int dx = -vitesse; dx <= vitesse; dx++) {
            for (int dy = -vitesse; dy <= vitesse; dy++) {
                if (Math.abs(dx) + Math.abs(dy) == vitesse) {
                    int newX = posActuelle.getX() + dx;
                    int newY = posActuelle.getY() + dy;
                    Position candidat = new Position(newX, newY);

                    if (estPositionValideSimple(candidat, animal)) {
                        positionsValides.add(candidat);
                    }
                }
            }
        }

        if (positionsValides.isEmpty()) {
            System.out.println("Aucune position aléatoire valide pour " + animal.getClass().getSimpleName());
            return null; // Aucune position valide trouvée
        }

        // Choisir aléatoirement parmi les positions valides
        int index = new java.util.Random().nextInt(positionsValides.size());
        Position choix = positionsValides.get(index);
        System.out.println("Déplacement aléatoire vers: " + choix);
        return choix;
    }

    private void jouerAuto() {
        while (partieEnCours) {
            if (mouton == null || loup == null || sortie == null) {
                System.out.println("Éléments manquants - Arrêt de la simulation");
                break;
            }

            System.out.println("\n=== TOUR " + (nbTours + 1) + " ===");
            System.out.println("Mouton: " + mouton.getPosition() + " (vitesse: " + mouton.getVitesse() + ")");
            System.out.println("Loup: " + loup.getPosition() + " (vitesse: " + loup.getVitesse() + ")");
            System.out.println("Sortie: " + sortie.getX() + "," + sortie.getY());

            // Mise à jour des états de vision
            boolean loupVoitMouton = peutVoir(loup.getPosition(), mouton.getPosition());
            boolean moutonVoitLoup = peutVoir(mouton.getPosition(), loup.getPosition());

            System.out.println("Loup voit mouton: " + loupVoitMouton);
            System.out.println("Mouton voit loup: " + moutonVoitLoup);

            // Mise à jour des modes
            loupEnChasse = loupVoitMouton;
            moutonEnFuite = moutonVoitLoup;

            System.out.println("Mode loup: " + (loupEnChasse ? "CHASSE" : "EXPLORATION"));
            System.out.println("Mode mouton: " + (moutonEnFuite ? "FUITE" : "PÂTURAGE"));

            // Tour du mouton
            System.out.println("\n--- Tour du mouton ---");
            if (!jouerTourMouton()) {
                javafx.application.Platform.runLater(() -> finDePartie("Partie bloquée - Le mouton ne peut plus bouger"));
                break;
            }

            // Vérifications après le mouvement du mouton
            if (mouton.getX() == sortie.getX() && mouton.getY() == sortie.getY()) {
                javafx.application.Platform.runLater(() -> finDePartie("Le mouton a gagné ! Il a atteint la sortie."));
                break;
            }

            if (mouton.getX() == loup.getX() && mouton.getY() == loup.getY()) {
                javafx.application.Platform.runLater(() -> finDePartie("Le loup a gagné ! Il a attrapé le mouton."));
                break;
            }

            // Tour du loup
            System.out.println("\n--- Tour du loup ---");
            if (!jouerTourLoup()) {
                javafx.application.Platform.runLater(() -> finDePartie("Partie bloquée - Le loup ne peut plus bouger"));
                break;
            }

            // Vérification finale
            if (mouton.getX() == loup.getX() && mouton.getY() == loup.getY()) {
                javafx.application.Platform.runLater(() -> finDePartie("Le loup a gagné ! Il a attrapé le mouton."));
                break;
            }

            nbTours++;
            javafx.application.Platform.runLater(this::afficherGrille);

            try {
                Thread.sleep(1500); // Ralentir un peu pour mieux voir
            } catch (InterruptedException e) {
                break;
            }
        }
    }

    private boolean jouerTourMouton() {
        Position nouvellePosition;

        if (moutonEnFuite && sortie != null) {
            System.out.println("Le mouton fuit vers la sortie !");
            nouvellePosition = choisirDeplacementIntelligent(mouton, new Position(sortie.getX(), sortie.getY()));
        } else {
            System.out.println("Le mouton se déplace aléatoirement pour paître");
            nouvellePosition = choisirDeplacementAleatoire(mouton);
        }

        if (nouvellePosition != null) {
            System.out.println("Mouton se déplace vers: " + nouvellePosition);
            return deplacerMouton(nouvellePosition);
        } else {
            System.out.println("Aucune position valide trouvée pour le mouton !");
            return false;
        }
    }

    private boolean jouerTourLoup() {
        Position nouvellePosition;

        if (loupEnChasse) {
            System.out.println("Le loup chasse le mouton !");
            nouvellePosition = choisirDeplacementIntelligent(loup, mouton.getPosition());
        } else {
            System.out.println("Le loup se déplace aléatoirement");
            nouvellePosition = choisirDeplacementAleatoire(loup);
        }

        if (nouvellePosition != null) {
            System.out.println("Loup se déplace vers: " + nouvellePosition);
            return deplacerLoup(nouvellePosition);
        } else {
            System.out.println("Aucune position valide trouvée pour le loup !");
            return false;
        }
    }

    private boolean deplacerMouton(Position nouvellePosition) {
        if (nouvellePosition == null) return false;

        Position positionActuelle = mouton.getPosition();
        int distance = positionActuelle.distanceManhattan(nouvellePosition);

        // Vérifier que la distance correspond à la vitesse
        if (distance != mouton.getVitesse()) {
            System.out.println("Distance incorrecte pour le mouton: " + distance + " != " + mouton.getVitesse());
            return false;
        }

        // Déplacer le mouton
        mouton.setPosition(nouvellePosition);
        System.out.println("Mouton déplacé avec succès vers: " + nouvellePosition);

        // Le mouton mange ce qui se trouve sur sa nouvelle position
        Element elementCase = grille[nouvellePosition.getX()][nouvellePosition.getY()];
        if (elementCase instanceof Vegetal vegetal) {
            mouton.manger(vegetal);
            System.out.println("Mouton mange: " + vegetal.getClass().getSimpleName());

            if (vegetal instanceof Herbe) {
                nbHerbe++;
            } else if (vegetal instanceof Cactus) {
                nbCactus++;
            } else if (vegetal instanceof Marguerite) {
                nbMarguerite++;
            }

            // Faire repousser un nouveau végétal
            grille[nouvellePosition.getX()][nouvellePosition.getY()] = genererVegetalAleatoire();
        }

        return true;
    }

    private boolean deplacerLoup(Position nouvellePosition) {
        if (nouvellePosition == null) return false;

        Position positionActuelle = loup.getPosition();
        int distance = positionActuelle.distanceManhattan(nouvellePosition);

        // Vérifier que la distance correspond à la vitesse
        if (distance != loup.getVitesse()) {
            System.out.println("Distance incorrecte pour le loup: " + distance + " != " + loup.getVitesse());
            return false;
        }

        // Déplacer le loup
        loup.setPosition(nouvellePosition);
        System.out.println("Loup déplacé avec succès vers: " + nouvellePosition);

        return true;
    }

    private Vegetal genererVegetalAleatoire() {
        int r = new java.util.Random().nextInt(10);
        if (r < 6) return new Herbe();           // 60% herbe
        else if (r < 8) return new Marguerite(); // 20% marguerite
        else return new Cactus();                // 20% cactus
    }

    private enum ElementType {
        HERBE, CACTUS, MARGUERITE, ROCHER, MOUTON, LOUP, SUPPRIMER, SORTIE
    }

    private void finDePartie(String gagnant) {
        partieEnCours = false;
        javafx.application.Platform.runLater(this::afficherGrille);

        System.out.println("=== FIN DE PARTIE ===");
        System.out.println("Résultat: " + gagnant);
        System.out.println("Nombre de tours: " + nbTours);
        System.out.println("Statistiques du mouton:");
        System.out.println("- Herbe mangée: " + nbHerbe);
        System.out.println("- Cactus mangés: " + nbCactus);
        System.out.println("- Marguerites mangées: " + nbMarguerite);

        String messageDetaille = String.format(
                "%s\n\n" +
                        "📊 Statistiques de la partie :\n" +
                        "• Nombre de tours : %d\n" +
                        "• Herbe mangée : %d\n" +
                        "• Cactus mangés : %d\n" +
                        "• Marguerites mangées : %d\n" +
                        "• Mode loup : %s\n" +
                        "• Mode mouton : %s\n\n" +
                        "Voulez-vous rejouer ?",
                gagnant, nbTours, nbHerbe, nbCactus, nbMarguerite,
                loupEnChasse ? "Chasse" : "Exploration",
                moutonEnFuite ? "Fuite" : "Pâturage"
        );

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Fin de partie");
        alert.setHeaderText("🎯 " + gagnant);
        alert.setContentText(messageDetaille);

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
}