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

    private boolean estTourDuMouton = true;
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
        estTourDuMouton = true;
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
        loupEnChasse = false;
        moutonEnFuite = false;

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

        // Vérifier la distance de Manhattan
        int distance = a.distanceManhattan(b);
        if (distance > 5) return false;

        // Vérifier s'il y a des obstacles sur la ligne de vue
        return !aObstacleSurLigneDeVue(a, b);
    }

    private boolean aObstacleSurLigneDeVue(Position a, Position b) {
        int x1 = a.getX(), y1 = a.getY();
        int x2 = b.getX(), y2 = b.getY();

        int dx = Math.abs(x2 - x1);
        int dy = Math.abs(y2 - y1);
        int stepX = x1 < x2 ? 1 : -1;
        int stepY = y1 < y2 ? 1 : -1;

        int err = dx - dy;
        int x = x1, y = y1;

        while (true) {
            // Ne pas vérifier les positions de départ et d'arrivée
            if ((x != x1 || y != y1) && (x != x2 || y != y2)) {
                if (x >= 0 && x < largeur && y >= 0 && y < hauteur) {
                    if (grille[x][y] instanceof Rocher) {
                        return true; // Obstacle trouvé
                    }
                }
            }

            if (x == x2 && y == y2) break;

            int e2 = 2 * err;
            if (e2 > -dy) {
                err -= dy;
                x += stepX;
            }
            if (e2 < dx) {
                err += dx;
                y += stepY;
            }
        }

        return false; // Pas d'obstacle
    }


    private Position choisirDeplacementIntelligent(Animal animal, Position objectif) {
        // Utiliser les algorithmes de parcours pour trouver le meilleur chemin
        List<Position> chemin;

        if (animal instanceof Mouton) {
            chemin = AlgorithmeParcours.aStar(animal.getPosition(), objectif, grille, mouton, loup);
        } else {
            chemin = AlgorithmeParcours.dijkstra(animal.getPosition(), objectif, grille, mouton, loup);
        }

        // Si on a un chemin, essayer de suivre la première étape
        if (!chemin.isEmpty()) {
            Position prochaine = chemin.get(0);
            int distance = animal.getPosition().distanceManhattan(prochaine);

            // Si la prochaine position est à la bonne distance, l'utiliser
            if (distance == animal.getVitesse()) {
                if (AlgorithmeParcours.estDeplacementValide(animal.getPosition(), prochaine,
                        grille, animal, mouton, loup)) {
                    return prochaine;
                }
            }
        }

        // Sinon, utiliser la méthode de recherche directionnelle
        return AlgorithmeParcours.trouverMeilleurePosition(animal, objectif, grille, mouton, loup);
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

                    if (AlgorithmeParcours.estDeplacementValide(posActuelle, candidat,
                            grille, animal, mouton, loup)) {
                        positionsValides.add(candidat);
                    }
                }
            }
        }

        if (positionsValides.isEmpty()) {
            return null; // Aucune position valide trouvée
        }

        // Choisir aléatoirement parmi les positions valides
        int index = new java.util.Random().nextInt(positionsValides.size());
        return positionsValides.get(index);
    }


    private void jouerAuto() {
        while (partieEnCours) {
            if (mouton == null || loup == null || sortie == null) {
                break;
            }

            // Vérifier les conditions de fin
            if (mouton.getX() == loup.getX() && mouton.getY() == loup.getY()) {
                javafx.application.Platform.runLater(() -> finDePartie("Le loup a gagné ! Il a attrapé le mouton."));
                break;
            }

            if (mouton.getX() == sortie.getX() && mouton.getY() == sortie.getY()) {
                javafx.application.Platform.runLater(() -> finDePartie("Le mouton a gagné ! Il a atteint la sortie."));
                break;
            }

            // Mise à jour des états de vision
            boolean loupVoitMouton = peutVoir(loup.getPosition(), mouton.getPosition());
            boolean moutonVoitLoup = peutVoir(mouton.getPosition(), loup.getPosition());

            // Mise à jour des modes
            if (loupVoitMouton) {
                loupEnChasse = true;
            } else {
                loupEnChasse = false;
            }

            if (moutonVoitLoup) {
                moutonEnFuite = true;
            } else {
                moutonEnFuite = false;
            }

            boolean deplacementReussi = false;

            if (estTourDuMouton) {
                deplacementReussi = jouerTourMouton();
                if (deplacementReussi) {
                    estTourDuMouton = false; // Passer au tour du loup
                }
            } else {
                deplacementReussi = jouerTourLoup();
                if (deplacementReussi) {
                    estTourDuMouton = true; // Passer au tour du mouton
                    nbTours++; // Incrémenter le nombre de tours après chaque cycle complet
                }
            }

            if (!deplacementReussi) {
                javafx.application.Platform.runLater(() -> finDePartie("Partie bloquée - Aucun déplacement possible"));
                break;
            }

            // Mettre à jour l'affichage tous les deux demi-tours
            javafx.application.Platform.runLater(this::afficherGrille);

            // Pause pour voir l'animation
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                break;
            }
        }
    }


    private boolean jouerTourMouton() {
        Position nouvellePosition;

        if (moutonEnFuite) {
            // Le mouton fuit vers la sortie
            nouvellePosition = choisirDeplacementIntelligent(mouton, new Position(sortie.getX(), sortie.getY()));
        } else {
            // Le mouton se déplace aléatoirement pour paître
            nouvellePosition = choisirDeplacementAleatoire(mouton);
        }

        if (nouvellePosition != null) {
            return deplacerMouton(nouvellePosition);
        }

        return false;
    }


    private boolean jouerTourLoup() {
        Position nouvellePosition;

        if (loupEnChasse) {
            // Le loup chasse le mouton
            nouvellePosition = choisirDeplacementIntelligent(loup, mouton.getPosition());
        } else {
            // Le loup se déplace aléatoirement
            nouvellePosition = choisirDeplacementAleatoire(loup);
        }

        if (nouvellePosition != null) {
            return deplacerLoup(nouvellePosition);
        }

        return false;
    }


    private boolean deplacerMouton(Position nouvellePosition) {
        if (nouvellePosition == null) return false;

        Position positionActuelle = mouton.getPosition();
        int distance = positionActuelle.distanceManhattan(nouvellePosition);

        // Vérifier que la distance correspond à la vitesse
        if (distance != mouton.getVitesse()) {
            return false;
        }

        // Vérifier que le déplacement est valide
        if (!AlgorithmeParcours.estDeplacementValide(positionActuelle, nouvellePosition,
                grille, mouton, mouton, loup)) {
            return false;
        }

        // Déplacer le mouton
        mouton.setPosition(nouvellePosition);

        // Le mouton mange ce qui se trouve sur sa nouvelle position
        Element elementCase = grille[nouvellePosition.getX()][nouvellePosition.getY()];
        if (elementCase instanceof Vegetal vegetal) {
            mouton.manger(vegetal);


            if (vegetal instanceof Herbe) {
                nbHerbe++;
            } else if (vegetal instanceof Cactus) {
                nbCactus++;
            } else if (vegetal instanceof Marguerite) {
                nbMarguerite++;
            }


            grille[nouvellePosition.getX()][nouvellePosition.getY()] = genererVegetalAleatoire();
        }

        return true;
    }


    private boolean deplacerLoup(Position nouvellePosition) {
        if (nouvellePosition == null) return false;

        Position positionActuelle = loup.getPosition();
        int distance = positionActuelle.distanceManhattan(nouvellePosition);


        if (distance != loup.getVitesse()) {
            return false;
        }


        if (!AlgorithmeParcours.estDeplacementValide(positionActuelle, nouvellePosition,
                grille, loup, mouton, loup)) {
            return false;
        }


        loup.setPosition(nouvellePosition);

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


    private enum ElementType {
        HERBE, CACTUS, MARGUERITE, ROCHER, MOUTON, LOUP, SUPPRIMER, SORTIE
    }


    private void finDePartie(String gagnant) {
        partieEnCours = false;
        javafx.application.Platform.runLater(this::afficherGrille);

        String messageDetaille = String.format(
                "%s\n\n" +
                        " Statistiques de la partie :\n" +
                        "• Nombre de tours : %d\n" +
                        "• Herbe mangée : %d\n" +
                        "• Cactus mangés : %d\n" +
                        "• Marguerites mangées : %d\n\n" +
                        "Voulez-vous rejouer ?",
                gagnant, nbTours, nbHerbe, nbCactus, nbMarguerite,
                loupEnChasse ? "Chasse" : "Exploration",
                moutonEnFuite ? "Fuite" : "Pâturage"
        );

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Fin de partie");
        alert.setHeaderText(" " + gagnant);
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