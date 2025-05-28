package com.sae.moutonloup.view;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.control.TextField;

public class DemarrageController {

    @FXML
    private TextField champLignes;
    @FXML
    private TextField champColonnes;

    @FXML
    private void jouer() {
        try {
            int lignes = Integer.parseInt(champLignes.getText());
            int colonnes = Integer.parseInt(champColonnes.getText());

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/sae/moutonloup/Editeur.fxml"));
            Parent root = loader.load();

            EditeurController controller = loader.getController();
            controller.setDimensions(lignes, colonnes);


            Stage stage = new Stage();
            stage.setTitle("Éditeur de labyrinthe");
            stage.setScene(new Scene(root));
            stage.setMaximized(true);
            stage.show();

            Stage currentStage = (Stage) champLignes.getScene().getWindow();
            currentStage.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
