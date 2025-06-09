package com.sae.moutonloup.view;

import com.sae.moutonloup.model.*;
import com.sae.moutonloup.ia.AlgorithmeParcours;
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

    // Chemins calculés par les algorithmes
    private List<Position> cheminMouton = new ArrayList<>();
    private List<Position> cheminLoup = new ArrayList<>();
    private int indexCheminMouton = 0;
    private int indexCheminLoup = 0;

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
        cheminMouton.clear();
        cheminLoup.clear();
        indexCheminMouton = 0;
        indexCheminLoup = 0;
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

        // Afficher la boîte de dialogue de choix d'algorithmes
        if (!choisirAlgorithmes()) return;

        partieEnCours = true;
        nbTours = 0;
        nbHerbe = nbCactus = nbMarguerite = 0;
        loupEnChasse = false;
        moutonEnFuite = false;
        cheminMouton.clear();
        cheminLoup.clear();
        indexCheminMouton = 0;
        indexCheminLoup = 0;

        System.out.println("=== DEMARRAGE DE LA PARTIE ===");
        System.out.println("Mouton: " + mouton.getPosition() + " (Algorithme: " + algorithmeRouton + ")");
        System.out.println("Loup: " + loup.getPosition() + " (Algorithme: " + algorithmeLoup + ")");
        System.out.println("Sortie: " + sortie.getX() + "," + sortie.getY());

        new Thread(this::jouerAuto).start();
    }

    private boolean choisirAlgorithmes() {
        // Créer la boîte de dialogue personnalisée
        Alert dialog = new Alert(Alert.AlertType.CONFIRMATION);
        dialog.setTitle("Choix des algorithmes");
        dialog.setHeaderText("Selectionnez les algorithmes pour chaque animal");

        // Créer le contenu personnalisé
        javafx.scene.layout.VBox content = new javafx.scene.layout.VBox(10);
        content.setPadding(new javafx.geometry.Insets(10));

        // Section Mouton
        javafx.scene.control.Label labelMouton = new javafx.scene.control.Label("Algorithme pour le MOUTON:");
        labelMouton.setStyle("-fx-font-weight: bold;");

        javafx.scene.control.ComboBox<AlgorithmeType> comboMouton = new javafx.scene.control.ComboBox<>();
        comboMouton.getItems().addAll(AlgorithmeType.values());
        comboMouton.setValue(algorithmeRouton);

        // Section Loup
        javafx.scene.control.Label labelLoup = new javafx.scene.control.Label("Algorithme pour le LOUP:");
        labelLoup.setStyle("-fx-font-weight: bold;");

        javafx.scene.control.ComboBox<AlgorithmeType> comboLoup = new javafx.scene.control.ComboBox<>();
        comboLoup.getItems().addAll(AlgorithmeType.values());
        comboLoup.setValue(algorithmeLoup);

        // Description des algorithmes
        javafx.scene.control.TextArea description = new javafx.scene.control.TextArea();
        description.setEditable(false);
        description.setPrefRowCount(6);
        description.setText(
                "A_STAR: Rapide avec heuristique, optimal\n" +
                        "DIJKSTRA: Optimal, explore tous les chemins\n" +
                        "BFS: Plus court chemin, largeur d'abord\n" +
                        "DFS: Exploration profonde, peut être long"
        );

        content.getChildren().addAll(
                labelMouton, comboMouton,
                new javafx.scene.control.Separator(),
                labelLoup, comboLoup,
                new javafx.scene.control.Separator(),
                new javafx.scene.control.Label("Description:"),
                description
        );

        dialog.getDialogPane().setContent(content);

        // Boutons personnalisés
        javafx.scene.control.ButtonType boutonDemarrer = new javafx.scene.control.ButtonType("Demarrer");
        javafx.scene.control.ButtonType boutonAnnuler = new javafx.scene.control.ButtonType("Annuler",
                javafx.scene.control.ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getButtonTypes().setAll(boutonDemarrer, boutonAnnuler);

        // Afficher et traiter la réponse
        java.util.Optional<javafx.scene.control.ButtonType> result = dialog.showAndWait();

        if (result.isPresent() && result.get() == boutonDemarrer) {
            // Récupérer les choix
            algorithmeRouton = comboMouton.getValue();
            algorithmeLoup = comboLoup.getValue();

            System.out.println("Algorithmes choisis:");
            System.out.println("- Mouton: " + algorithmeRouton);
            System.out.println("- Loup: " + algorithmeLoup);

            return true;
        }

        return false; // Annulé
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
        if (distance > 5) return false;

        return !aObstacleSurLigneDeVue(a, b);
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
            if ((x != x0 || y != y0) && (x != x1 || y != y1)) {
                if (x >= 0 && x < largeur && y >= 0 && y < hauteur) {
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

    private void jouerAuto() {
        while (partieEnCours) {
            if (mouton == null || loup == null || sortie == null) {
                System.out.println("Éléments manquants - Arrêt de la simulation");
                break;
            }

            System.out.println("\n=== TOUR " + (nbTours + 1) + " ===");

            // Mise à jour des états de vision
            boolean loupVoitMouton = peutVoir(loup.getPosition(), mouton.getPosition());
            boolean moutonVoitLoup = peutVoir(mouton.getPosition(), loup.getPosition());

            // Mise à jour des modes
            boolean ancienModeLoup = loupEnChasse;
            boolean ancienModeMouton = moutonEnFuite;

            loupEnChasse = loupVoitMouton;
            moutonEnFuite = moutonVoitLoup;

            System.out.println("Mode loup: " + (loupEnChasse ? "CHASSE" : "EXPLORATION"));
            System.out.println("Mode mouton: " + (moutonEnFuite ? "FUITE" : "PÂTURAGE"));

            // Si le mode change, recalculer le chemin
            if (ancienModeMouton != moutonEnFuite) {
                cheminMouton.clear();
                indexCheminMouton = 0;
            }
            if (ancienModeLoup != loupEnChasse) {
                cheminLoup.clear();
                indexCheminLoup = 0;
            }

            // Tour du mouton
            boolean moutonPeutBouger = jouerTourMouton();

            // Vérification immédiate après le mouvement du mouton
            if (mouton.getX() == loup.getX() && mouton.getY() == loup.getY()) {
                javafx.application.Platform.runLater(() -> finDePartie("Le loup a gagné ! Il a attrapé le mouton."));
                break;
            }

            // Si le mouton ne peut plus bouger (probablement attrapé), vérifier pourquoi
            if (!moutonPeutBouger) {
                if (mouton.getX() == loup.getX() && mouton.getY() == loup.getY()) {
                    javafx.application.Platform.runLater(() -> finDePartie("Le loup a gagné ! Il a attrapé le mouton."));
                    break;
                } else {
                    javafx.application.Platform.runLater(() -> finDePartie("Partie bloquée - Le mouton ne peut plus bouger"));
                    break;
                }
            }

            // Vérification si le mouton a atteint la sortie
            if (mouton.getX() == sortie.getX() && mouton.getY() == sortie.getY()) {
                javafx.application.Platform.runLater(() -> finDePartie("Le mouton a gagné ! Il a atteint la sortie."));
                break;
            }

            // Tour du loup
            if (!jouerTourLoup()) {
                javafx.application.Platform.runLater(() -> finDePartie("Partie bloquée - Le loup ne peut plus bouger"));
                break;
            }

            // Vérification finale après le mouvement du loup
            if (mouton.getX() == loup.getX() && mouton.getY() == loup.getY()) {
                javafx.application.Platform.runLater(() -> finDePartie("Le loup a gagné ! Il a attrapé le mouton."));
                break;
            }

            nbTours++;
            javafx.application.Platform.runLater(this::afficherGrille);

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                break;
            }
        }
    }

    private boolean jouerTourMouton() {
        // Si on n'a pas de chemin ou que le chemin est terminé, en calculer un nouveau
        if (cheminMouton.isEmpty() || indexCheminMouton >= cheminMouton.size()) {
            calculerNouveauCheminMouton();
        }

        return executerDeplacementMouton();
    }

    private void calculerNouveauCheminMouton() {
        cheminMouton.clear();
        indexCheminMouton = 0;

        if (moutonEnFuite && sortie != null) {
            // Utiliser Dijkstra pour la fuite (trouve le chemin le plus sûr)
            System.out.println("Calcul du chemin de fuite du mouton vers la sortie (Dijkstra)");
            cheminMouton = AlgorithmeParcours.dijkstra(
                    mouton.getPosition(),
                    new Position(sortie.getX(), sortie.getY()),
                    grille,
                    mouton,
                    loup,
                    mouton
            );
        } else {
            // Utiliser A* pour le pâturage (plus rapide pour les courtes distances)
            System.out.println("Mouvement aleatoire du mouton pour paitre (A*)");
            Position positionAleatoire = choisirPositionAleatoire(mouton);
            if (positionAleatoire != null) {
                cheminMouton = AlgorithmeParcours.aStar(
                        mouton.getPosition(),
                        positionAleatoire,
                        grille,
                        mouton,
                        loup,
                        mouton
                );
            }
        }

        System.out.println("Chemin calcule pour le mouton: " + cheminMouton.size() + " etapes");
    }

    private boolean jouerTourLoup() {
        // Simple : recalculer le chemin à chaque tour en mode chasse pour suivre le mouton
        if (loupEnChasse) {
            cheminLoup.clear();
            indexCheminLoup = 0;
            calculerNouveauCheminLoup();
        }

        // Si pas de chemin, en calculer un
        if (cheminLoup.isEmpty() || indexCheminLoup >= cheminLoup.size()) {
            calculerNouveauCheminLoup();
        }

        return executerDeplacementLoup();
    }


    private boolean attaqueDirectePuissante() {
        if (mouton == null) return false;

        Position posLoup = loup.getPosition();
        Position posMouton = mouton.getPosition();
        int vitesse = loup.getVitesse();

        System.out.println("Calcul attaque directe de " + posLoup + " vers " + posMouton);

        // Calculer le chemin direct le plus court
        List<Position> cheminDirect = AlgorithmeParcours.parcoursenLargeur(
                posLoup, posMouton, grille, mouton, loup, loup
        );

        if (!cheminDirect.isEmpty()) {
            System.out.println("Chemin direct trouvé: " + cheminDirect.size() + " étapes");

            // Suivre le chemin direct autant que possible
            for (int i = 0; i < Math.min(vitesse, cheminDirect.size()); i++) {
                Position prochaine = cheminDirect.get(i);

                if (estDeplacementValide(loup.getPosition(), prochaine, loup)) {
                    loup.setPosition(prochaine);
                    System.out.println("Loup attaque directe vers: " + prochaine + " (" + (i+1) + "/" + vitesse + ")");

                    // Vérifier capture immédiate
                    if (prochaine.getX() == posMouton.getX() && prochaine.getY() == posMouton.getY()) {
                        System.out.println("CAPTURE RÉUSSIE !");
                        return true;
                    }
                } else {
                    System.out.println("Déplacement direct bloqué, abandon de l'attaque directe");
                    break;
                }
            }
        } else {
            System.out.println("Aucun chemin direct trouvé, attaque normale");
            return attaqueDirecte();
        }

        return true;
    }


    private boolean attaqueDirecte() {
        if (mouton == null) return false;

        Position posLoup = loup.getPosition();
        Position posMouton = mouton.getPosition();
        int vitesse = loup.getVitesse();

        // Essayer de se déplacer directement vers le mouton
        for (int step = 1; step <= vitesse; step++) {
            // Calculer la direction vers le mouton
            int deltaX = posMouton.getX() - posLoup.getX();
            int deltaY = posMouton.getY() - posLoup.getY();

            // Normaliser pour un seul pas
            int dirX = Integer.compare(deltaX, 0);
            int dirY = Integer.compare(deltaY, 0);

            // Calculer la prochaine position
            int newX = posLoup.getX() + dirX;
            int newY = posLoup.getY() + dirY;
            Position nouvellePos = new Position(newX, newY);

            // Vérifier si le déplacement est valide
            if (estDeplacementValide(posLoup, nouvellePos, loup)) {
                loup.setPosition(nouvellePos);
                posLoup = nouvellePos; // Mettre à jour pour le prochain pas

                System.out.println("Loup attaque directement vers: " + nouvellePos +
                        " (pas " + step + "/" + vitesse + ")");

                // Vérifier si le loup a attrapé le mouton
                if (nouvellePos.getX() == posMouton.getX() && nouvellePos.getY() == posMouton.getY()) {
                    System.out.println("CAPTURE ! Le loup attrape le mouton !");
                    return true;
                }

                // Si on a atteint le mouton ou qu'on est adjacent, arrêter
                if (nouvellePos.distanceManhattan(posMouton) <= 1) {
                    break;
                }
            } else {
                // Si le déplacement direct n'est pas possible, essayer les côtés
                Position[] alternatives = {
                        new Position(posLoup.getX() + 1, posLoup.getY()),
                        new Position(posLoup.getX() - 1, posLoup.getY()),
                        new Position(posLoup.getX(), posLoup.getY() + 1),
                        new Position(posLoup.getX(), posLoup.getY() - 1)
                };

                Position meilleure = null;
                int meilleureDistance = Integer.MAX_VALUE;

                for (Position alt : alternatives) {
                    if (estDeplacementValide(posLoup, alt, loup)) {
                        int dist = alt.distanceManhattan(posMouton);
                        if (dist < meilleureDistance) {
                            meilleureDistance = dist;
                            meilleure = alt;
                        }
                    }
                }

                if (meilleure != null) {
                    loup.setPosition(meilleure);
                    posLoup = meilleure;

                    System.out.println("Loup contourne vers: " + meilleure +
                            " (pas " + step + "/" + vitesse + ")");

                    // Vérifier capture
                    if (meilleure.getX() == posMouton.getX() && meilleure.getY() == posMouton.getY()) {
                        System.out.println("CAPTURE par contournement !");
                        return true;
                    }
                } else {
                    break; // Aucun mouvement possible
                }
            }
        }

        return true;
    }

    private void calculerNouveauCheminLoup() {
        cheminLoup.clear();
        indexCheminLoup = 0;

        if (loupEnChasse && mouton != null) {
            // Utiliser A* pour la chasse (plus rapide avec heuristique)
            System.out.println("Loup chasse -> vers mouton " + mouton.getPosition() + " (A*)");
            cheminLoup = AlgorithmeParcours.aStar(
                    loup.getPosition(),
                    mouton.getPosition(),
                    grille,
                    mouton,
                    loup,
                    loup
            );
        } else {
            // Utiliser BFS pour l'exploration (parcours exhaustif)
            System.out.println("Loup explore (BFS)");
            Position positionAleatoire = choisirPositionAleatoire(loup);
            if (positionAleatoire != null) {
                cheminLoup = AlgorithmeParcours.parcoursenLargeur(
                        loup.getPosition(),
                        positionAleatoire,
                        grille,
                        mouton,
                        loup,
                        loup
                );
            }
        }

        System.out.println("Chemin loup: " + cheminLoup.size() + " etapes");
    }


    private Position predirePositionMouton() {
        if (mouton == null || sortie == null) return null;

        Position posMouton = mouton.getPosition();
        Position posSortie = new Position(sortie.getX(), sortie.getY());

        // Si le mouton fuit, il va vers la sortie
        if (moutonEnFuite) {
            // Calculer la direction générale vers la sortie
            int deltaX = posSortie.getX() - posMouton.getX();
            int deltaY = posSortie.getY() - posMouton.getY();

            // Normaliser la direction (un pas à la fois)
            int dirX = Integer.compare(deltaX, 0);
            int dirY = Integer.compare(deltaY, 0);

            // Prédire où sera le mouton dans 1-2 mouvements
            int predictionSteps = Math.min(2, mouton.getVitesse());

            for (int steps = predictionSteps; steps >= 1; steps--) {
                int predX = posMouton.getX() + (dirX * steps);
                int predY = posMouton.getY() + (dirY * steps);

                // Vérifier que la position prédite est valide
                if (predX >= 0 && predX < largeur && predY >= 0 && predY < hauteur) {
                    Element element = grille[predX][predY];
                    if (!(element instanceof Rocher)) {
                        System.out.println("Position prédite du mouton: (" + predX + "," + predY + ")");
                        return new Position(predX, predY);
                    }
                }
            }
        }

        // Si pas de prédiction possible, retourner la position actuelle
        return posMouton;
    }

    private Position choisirPositionAleatoire(Animal animal) {
        List<Position> positionsValides = new ArrayList<>();

        // Chercher des positions accessibles à une distance raisonnable
        for (int dx = -5; dx <= 5; dx++) {
            for (int dy = -5; dy <= 5; dy++) {
                int newX = animal.getX() + dx;
                int newY = animal.getY() + dy;

                if (newX > 0 && newX < largeur - 1 && newY > 0 && newY < hauteur - 1) {
                    Element element = grille[newX][newY];
                    if (!(element instanceof Rocher)) {
                        // Le loup ne peut pas aller sur la sortie
                        if (animal instanceof Loup && element instanceof Sortie) {
                            continue;
                        }
                        positionsValides.add(new Position(newX, newY));
                    }
                }
            }
        }

        if (positionsValides.isEmpty()) {
            return null;
        }

        int index = new java.util.Random().nextInt(positionsValides.size());
        return positionsValides.get(index);
    }

    private boolean executerDeplacementMouton() {
        int mouvementsRestants = mouton.getVitesse();
        List<Position> mouvementsEffectues = new ArrayList<>();

        while (mouvementsRestants > 0 && indexCheminMouton < cheminMouton.size()) {
            Position prochaine = cheminMouton.get(indexCheminMouton);

            if (estDeplacementValide(mouton.getPosition(), prochaine, mouton)) {
                mouvementsEffectues.add(prochaine);
                mouton.setPosition(prochaine);
                indexCheminMouton++;
                mouvementsRestants--;

                System.out.println("Mouton se déplace vers: " + prochaine +
                        " (mouvements restants: " + mouvementsRestants + ")");

                // Vérifier après chaque déplacement si le mouton se fait attraper
                if (loup != null && mouton.getX() == loup.getX() && mouton.getY() == loup.getY()) {
                    System.out.println("Le mouton se fait attraper par le loup sur la case " + prochaine);
                    return false; // ARRÊTER LE DÉPLACEMENT - le mouton est attrapé !
                }
            } else {
                break; // Arrêter si le déplacement n'est plus valide
            }
        }

        // Le mouton mange après son déplacement complet (seulement s'il n'a pas été attrapé)
        if (!mouvementsEffectues.isEmpty()) {
            Position positionFinale = mouvementsEffectues.get(mouvementsEffectues.size() - 1);
            moutonMange(positionFinale.getX(), positionFinale.getY());
            return true;
        }

        return false;
    }

    private boolean executerDeplacementLoup() {
        int mouvementsRestants = loup.getVitesse();

        while (mouvementsRestants > 0 && indexCheminLoup < cheminLoup.size()) {
            Position prochaine = cheminLoup.get(indexCheminLoup);

            if (estDeplacementValide(loup.getPosition(), prochaine, loup)) {
                loup.setPosition(prochaine);
                indexCheminLoup++;
                mouvementsRestants--;

                System.out.println("Loup se déplace vers: " + prochaine +
                        " (mouvements restants: " + mouvementsRestants + ")");

                // Vérifier après chaque déplacement si le loup attrape le mouton
                if (mouton != null && loup.getX() == mouton.getX() && loup.getY() == mouton.getY()) {
                    System.out.println("Le loup attrape le mouton sur la case " + prochaine);
                    return true; // Arrêter le déplacement, le loup a gagné
                }
            } else {
                break; // Arrêter si le déplacement n'est plus valide
            }
        }

        return true;
    }

    private boolean estDeplacementValide(Position actuelle, Position destination, Animal animal) {
        if (destination.getX() < 0 || destination.getX() >= largeur ||
                destination.getY() < 0 || destination.getY() >= hauteur) {
            return false;
        }

        Element element = grille[destination.getX()][destination.getY()];
        if (element instanceof Rocher) {
            return false;
        }

        // Le loup ne peut pas aller sur la sortie sauf pour attraper le mouton
        if (animal instanceof Loup && element instanceof Sortie) {
            return mouton != null &&
                    mouton.getX() == destination.getX() &&
                    mouton.getY() == destination.getY();
        }

        // Vérifier que c'est un déplacement adjacent (distance de Manhattan = 1)
        return actuelle.distanceManhattan(destination) == 1;
    }

    private void moutonMange(int x, int y) {
        if (mouton == null) return;

        Element element = grille[x][y];
        if (element instanceof Vegetal vegetal) {
            mouton.manger(vegetal);
            System.out.println("Mouton mange: " + vegetal.getClass().getSimpleName() +
                    " (nouvelle vitesse: " + mouton.getVitesse() + ")");

            if (vegetal instanceof Herbe) {
                nbHerbe++;
            } else if (vegetal instanceof Cactus) {
                nbCactus++;
            } else if (vegetal instanceof Marguerite) {
                nbMarguerite++;
            }

            // Faire repousser un nouveau végétal
            grille[x][y] = genererVegetalAleatoire();
        }
    }

    private Vegetal genererVegetalAleatoire() {
        int r = new java.util.Random().nextInt(10);
        if (r < 6) return new Herbe();           // 60% herbe
        else if (r < 8) return new Marguerite(); // 20% marguerite
        else return new Cactus();                // 20% cactus
    }

    // Algorithmes sélectionnés
    private AlgorithmeType algorithmeRouton = AlgorithmeType.DIJKSTRA;
    private AlgorithmeType algorithmeLoup = AlgorithmeType.A_STAR;

    private enum AlgorithmeType {
        A_STAR("A* (Heuristique)"),
        DIJKSTRA("Dijkstra (Optimal)"),
        BFS("BFS (Largeur)"),
        DFS("DFS (Profondeur)");

        private final String nom;

        AlgorithmeType(String nom) {
            this.nom = nom;
        }

        @Override
        public String toString() {
            return nom;
        }
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
                        "Statistiques de la partie :\n" +
                        "- Nombre de tours : %d\n" +
                        "- Herbe mangee : %d\n" +
                        "- Cactus manges : %d\n" +
                        "- Marguerites mangees : %d\n" +
                        "- Mode loup : %s\n" +
                        "- Mode mouton : %s\n\n" +
                        "Voulez-vous rejouer ?",
                gagnant, nbTours, nbHerbe, nbCactus, nbMarguerite,
                loupEnChasse ? "Chasse" : "Exploration",
                moutonEnFuite ? "Fuite" : "Paturage"
        );

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Fin de partie");
        alert.setHeaderText(gagnant);
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