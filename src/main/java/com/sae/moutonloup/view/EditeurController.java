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


public class EditeurController {
    private int nbTours = 0;
    private int nbHerbe = 0, nbCactus = 0, nbMarguerite = 0;

    private int nbLignes;
    private int nbColonnes;

    private boolean estTourDuMouton = false;
    private Mouton mouton;
    private Loup loup;

    private ElementType elementSelectionne = ElementType.HERBE;
    private Element[][] grille;
    private int largeur = 10;
    private int hauteur = 10;



    @FXML
    private void importerGrille() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Importer un labyrinthe");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Fichiers texte", "*.txt"));
        File fichier = fileChooser.showOpenDialog(grillePane.getScene().getWindow());

        if (fichier != null) {
            try (BufferedReader br = new BufferedReader(new FileReader(fichier))) {
                String ligne;
                int y = 0;
                while ((ligne = br.readLine()) != null && y < hauteur) {
                    for (int x = 0; x < Math.min(ligne.length(), largeur); x++) {
                        char c = ligne.charAt(x);
                        switch (c) {
                            case 'x' -> grille[x][y] = new Rocher();
                            case 'h' -> grille[x][y] = new Herbe();
                            case 'c' -> grille[x][y] = new Cactus();
                            case 'f' -> grille[x][y] = new Marguerite();
                            case 'm' -> {
                                mouton = new Mouton(new Position(x, y));
                                grille[x][y] = mouton;
                            }
                            case 'l' -> {
                                loup = new Loup(new Position(x, y));
                                grille[x][y] = loup;
                            }
                            case 's' -> grille[x][y] = new Sortie();
                            default -> grille[x][y] = new Herbe();
                        }
                    }
                    y++;
                }
                afficherGrille();
            } catch (IOException e) {
                e.printStackTrace();
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
                        Element e = grille[x][y];
                        if (e instanceof Rocher) ligne.append("x");
                        else if (e instanceof Herbe) ligne.append("h");
                        else if (e instanceof Cactus) ligne.append("c");
                        else if (e instanceof Marguerite) ligne.append("f");
                        else if (e instanceof Mouton) ligne.append("m");
                        else if (e instanceof Loup) ligne.append("l");
                        else if (e instanceof Sortie) ligne.append("s");
                        else ligne.append("h");
                    }
                    bw.write(ligne.toString());
                    bw.newLine();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void rejouer() {
        grille = new Element[largeur][hauteur];
        mouton = null;
        loup = null;
        nbTours = nbHerbe = nbCactus = nbMarguerite = 0;
        genererGrilleParDefaut();
        afficherGrille();
    }


    @FXML
    private GridPane grillePane;

    public void initialize() {
    }

    public void setDimensions(int lignes, int colonnes) {
        this.nbLignes = lignes;
        this.nbColonnes = colonnes;
        this.largeur = colonnes;
        this.hauteur = lignes;

        grille = new Element[largeur][hauteur];
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
                ImageView imageView = new ImageView(getImageForElement(grille[x][y]));
                imageView.setFitWidth(40);
                imageView.setFitHeight(40);
                int finalX = x;
                int finalY = y;
                imageView.setOnMouseClicked(e -> placerElement(finalX, finalY));
                grillePane.add(imageView, x, y);
            }
        }
    }

    private void placerElement(int x, int y) {
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
                if (!bordure) {
                    mouton = new Mouton(new Position(x, y)); // associer l'instance
                    grille[x][y] = mouton;
                }
            }

            case LOUP -> {
                if (!bordure) {
                    loup = new Loup(new Position(x, y));
                    grille[x][y] = loup;
                }
            }

            case SORTIE -> {
                if (bordure && grille[x][y] instanceof Rocher) {
                    grille[x][y] = new Sortie();
                }
            }
            case SUPPRIMER -> {
                if (grille[x][y] instanceof Sortie) {
                    grille[x][y] = new Rocher();
                } else if (!bordure) {
                    grille[x][y] = new Herbe();
                }
            }
        }

        elementSelectionne = null;
        afficherGrille();
    }


    private Image getImageForElement(Element element) {
        if (element instanceof Mouton) return load("images/mouton.png");
        if (element instanceof Loup) return load("images/loup.png");
        if (element instanceof Cactus) return load("images/cactus.png");
        if (element instanceof Marguerite) return load("images/marguerite.png");
        if (element instanceof Rocher) return load("images/rocher.png");
        if (element instanceof Sortie) return load("images/sortie.png");
        return load("images/herbe.png");
    }

    private Image load(String chemin) {
        return new Image(getClass().getResource("/" + chemin).toExternalForm());
    }

    @FXML public void boutonLoup()       { elementSelectionne = ElementType.LOUP; }
    @FXML public void boutonMouton()     { elementSelectionne = ElementType.MOUTON; }
    @FXML public void boutonCactus()     { elementSelectionne = ElementType.CACTUS; }
    @FXML public void boutonMarguerite() { elementSelectionne = ElementType.MARGUERITE; }
    @FXML public void boutonRocher()     { elementSelectionne = ElementType.ROCHER; }
    @FXML public void boutonSupprimer()  { elementSelectionne = ElementType.SUPPRIMER; }
    @FXML public void boutonSortie()     { elementSelectionne = ElementType.SORTIE; }

    private boolean estConnexeVersSortie() {
        Position sortiePos = null;


        for (int y = 0; y < hauteur; y++) {
            for (int x = 0; x < largeur; x++) {
                if (grille[x][y] instanceof Sortie) {
                    sortiePos = new Position(x, y);
                    break;
                }
            }
        }

        if (sortiePos == null) return false; // pas de sortie

        boolean[][] visite = new boolean[largeur][hauteur];
        explorerDepuisSortie(sortiePos.getX(), sortiePos.getY(), visite);


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
        if (!verifierPreconditions()) {
            return;
        }

        estTourDuMouton = true;
        jouerTour();
    }



    private boolean verifierPreconditions() {
        int moutons = 0;
        int loups = 0;
        int sorties = 0;

        for (int y = 0; y < hauteur; y++) {
            for (int x = 0; x < largeur; x++) {
                Element e = grille[x][y];
                if (e instanceof Mouton) moutons++;
                else if (e instanceof Loup) loups++;
                else if (e instanceof Sortie) sorties++;
            }
        }

        if (moutons != 1 || loups != 1 || sorties != 1) {
            afficherErreur("Il faut 1 mouton, 1 loup, 1 sortie.");
            return false;
        }

        if (!estConnexeVersSortie()) {
            afficherErreur("Le labyrinthe n'est pas connexe vers la sortie !");
            return false;
        }

        return true;
    }




    private void afficherErreur(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText("Impossible de démarrer");
        alert.setContentText(message);
        alert.showAndWait();
    }





    private void afficherCasesAccessibles(Position pos, int portee, boolean estMouton) {
        grillePane.getChildren().clear();

        for (int y = 0; y < hauteur; y++) {
            for (int x = 0; x < largeur; x++) {
                ImageView imageView = new ImageView(getImageForElement(grille[x][y]));
                imageView.setFitWidth(40);
                imageView.setFitHeight(40);

                int distance = Math.abs(x - pos.getX()) + Math.abs(y - pos.getY());
                int finalX = x;
                int finalY = y;

                if (distance <= portee && (estMouton ? estAccessible(x, y) && !(grille[x][y] instanceof Loup)
                        : estAccessible(x, y) || grille[x][y] instanceof Mouton)) {
                    String couleur = estMouton ? "deepskyblue" : "red";
                    imageView.setStyle("-fx-effect: dropshadow(gaussian, " + couleur + ", 15, 0.5, 0, 0);");

                    imageView.setOnMouseClicked(e -> {
                        boolean deplacementOk = estMouton
                                ? deplacerMouton(finalX, finalY)
                                : deplacerLoup(finalX, finalY);

                        if (deplacementOk) {
                            estTourDuMouton = !estMouton;
                            jouerTour();
                        }
                    });

                } else {
                    imageView.setOnMouseClicked(e -> placerElement(finalX, finalY));
                }

                grillePane.add(imageView, x, y);
            }
        }
    }


    private boolean estAccessible(int x, int y) {
        if (x < 0 || y < 0 || x >= largeur || y >= hauteur) return false;
        return !(grille[x][y] instanceof Rocher);
    }



    private enum ElementType {
        HERBE, CACTUS, MARGUERITE, ROCHER, MOUTON, LOUP, SUPPRIMER, SORTIE
    }

    private void jouerTour() {
        if (mouton == null || loup == null) return;

        if (estTourDuMouton) {
            afficherCasesAccessibles(mouton.getPosition(), mouton.getVitesse(), true);
        } else {
            afficherCasesAccessibles(loup.getPosition(), loup.getVitesse(), false);
        }
    }




    private void finDePartie(String gagnant) {
        System.out.println("🎉 Fin de partie !");
        System.out.println("Vainqueur : " + gagnant);
        System.out.println("Nombre de tours : " + nbTours);
        System.out.println("🌿 Herbe mangée : " + nbHerbe);
        System.out.println("🌵 Cactus mangé : " + nbCactus);
        System.out.println("🌸 Marguerite mangée : " + nbMarguerite);

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Fin de partie");
        alert.setHeaderText(gagnant);
        alert.setContentText(
                "Tours : " + nbTours + "\n" +
                        "🌿 Herbe : " + nbHerbe + "\n" +
                        "🌵 Cactus : " + nbCactus + "\n" +
                        "🌸 Marguerite : " + nbMarguerite + "\n\n" +
                        "Souhaitez-vous rejouer ?");

        ButtonType boutonRejouer = new ButtonType("Rejouer");
        ButtonType boutonQuitter = new ButtonType("Quitter", javafx.scene.control.ButtonBar.ButtonData.CANCEL_CLOSE);

        alert.getButtonTypes().setAll(boutonRejouer, boutonQuitter);

        alert.showAndWait().ifPresent(reponse -> {
            if (reponse == boutonRejouer) {
                rejouer(); // Appelle la méthode qui réinitialise le labyrinthe
            } else {
                // Ferme la fenêtre principale
                javafx.stage.Stage stage = (javafx.stage.Stage) grillePane.getScene().getWindow();
                stage.close();
            }
        });
    }


    private boolean deplacerMouton(int x, int y) {
        Element caseDestination = grille[x][y];

        if (caseDestination instanceof Loup) {
            afficherErreur("Le mouton ne peut pas aller sur la case du loup !");
            afficherCasesAccessibles(mouton.getPosition(), mouton.getVitesse(), true);
            return false;
        }

        if (caseDestination instanceof Vegetal vegetal) {
            mouton.manger(vegetal);
            if (vegetal instanceof Herbe) nbHerbe++;
            if (vegetal instanceof Cactus) nbCactus++;
            if (vegetal instanceof Marguerite) nbMarguerite++;
        }

        grille[mouton.getPosition().getX()][mouton.getPosition().getY()] = new Herbe();
        mouton.setPosition(new Position(x, y));
        grille[x][y] = mouton;
        nbTours++;

        if (caseDestination instanceof Sortie) {
            finDePartie("🐑 Le mouton a gagné !");
            return false;
        }

        return true;
    }




    private boolean deplacerLoup(int x, int y) {
        Element caseDestination = grille[x][y];

        if (caseDestination instanceof Sortie) {
            afficherErreur("Le loup ne peut pas aller sur la case de sortie !");
            afficherCasesAccessibles(loup.getPosition(), loup.getVitesse(), false);
            return false;
        }

        grille[loup.getPosition().getX()][loup.getPosition().getY()] = new Herbe();
        loup.setPosition(new Position(x, y));
        grille[x][y] = loup;
        nbTours++;

        if (caseDestination instanceof Mouton) {
            finDePartie("🐺 Le loup a gagné !");
            return false;
        }

        return true;
    }






}
