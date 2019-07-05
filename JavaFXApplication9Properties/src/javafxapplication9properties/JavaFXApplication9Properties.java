/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package javafxapplication9properties;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import java.util.Random;
import javafx.beans.value.*;
/**
 *
 * @author silve
 */
public class JavaFXApplication9Properties extends Application {

    @Override
    public void start(Stage primaryStage) {
        Bill electricBill = new Bill();
        final Random r = new Random();
      
        electricBill.amountDueProperty().addListener(new ChangeListener() {
            //@Override
            public void changed(ObservableValue o, Object oldV, Object newV) {
                double n = ((Double) newV);                       
                System.out.println("Electric bill has changed to " + n + "!");
            }
        });
        Button btn = new Button();
        btn.setText("Change amount");
        btn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                double d = r.nextDouble();
                electricBill.setAmountDue(d);
            }
        });

        StackPane root = new StackPane();
        root.getChildren().add(btn);
        Scene scene = new Scene(root, 300, 250);
        primaryStage.setTitle("Prova Properties");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
}
