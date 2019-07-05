/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package posta;

import java.io.IOException;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.control.Dialog;
import javax.swing.JOptionPane;
import javafx.collections.ObservableList;
import javafx.collections.FXCollections;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;

/**
 *
 * @author silve
 */
public class Posta extends Application {

    /**
     * @param args the command line arguments
     *
     */
    Stage window; // mi serve per potere accedere allo stage da altri metodi come il close()

    public static void main(String[] args) {

        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
            
        try {
            
            window = stage;
            Pane basePane = new Pane();

            Scene scene = new Scene(basePane);//aggiungo pannello alla scena
            scene.getStylesheets().add(getClass().getResource("Viper.css").toExternalForm()); // lo carica
            BorderPane borderPane = new BorderPane();
           
            basePane.getChildren().add(borderPane);
            TextField userField = new TextField();
            Button loginButton = new Button("Login");
            VBox topBox = new VBox();
            topBox.getChildren().addAll(userField, loginButton);
            borderPane.setCenter(topBox);
            BorderPane.setMargin(topBox, new Insets(48,48,48,48));
            loginButton.setOnAction((event) -> {
                  try {
                      FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("prova1.fxml"));
                      
                      Parent root = (Parent) fxmlLoader.load();
                      FXMLDocumentController controller = fxmlLoader.<FXMLDocumentController>getController();
                      
                      controller.setUser(userField.getText());
                      // Parent root = FXMLLoader.load(getClass().getResource("prova1.fxml"));
                      
                      Scene scene1 = new Scene(root);
                      scene1.getStylesheets().add(getClass().getResource("Viper.css").toExternalForm()); // lo carica
                      
                      window.setScene(scene1);
                      window.setTitle("Email v0.4");
                      window.show();
                      
                      window.setOnCloseRequest(
                              e -> {
                                  e.consume();// consumo l'evento per gestirlo completamente
                                  close();
                                  
                              }
                      ); // chiamo close quando tento di chiudere la finestra
                  } catch (IOException ex) {
                      Logger.getLogger(Posta.class.getName()).log(Level.SEVERE, null, ex);
                  }

            });
           
           window.setScene(scene); 
           window.setTitle("Login");
           window.show();

        } catch (Exception e) {
            System.out.println(e.getCause() + e.toString());

        }

    }

    //cosa fare quando chiudo finestra
    private void close() {
        try {

            Boolean risposta = ConfirmBox.display("Sei sicuro?", "Premi si per uscire");
            if (risposta) {
                window.close();
            }
        } catch (Exception e) {
            System.out.println(e.getCause() + e.toString());

        }

    }

}
