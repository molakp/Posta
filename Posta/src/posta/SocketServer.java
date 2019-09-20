/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package posta;
import java.io.*;
import java.net.*;
import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class SocketServer extends Application implements Runnable {
    public static StringProperty logData = new SimpleStringProperty();

    @Override
    public void start(Stage primaryStage) throws IOException, InterruptedException {
        
        Parent root = FXMLLoader.load(getClass().getResource("Server.fxml"));
        
        primaryStage.setTitle("Info server");
        primaryStage.setScene(new Scene(root));
        primaryStage.show();

        try {

            Runnable thread = new SocketServer();
            new Thread(thread).start();
            
            primaryStage.setOnCloseRequest(
                e -> {
                    e.consume();
                    primaryStage.close();
                    System.exit(0);
                }
        );

        } catch (Exception e) {
            System.out.println("Errore");

        }

    }

    public static void main(String[] args) {
        logData.set("");
        launch(args);
        
    }
    //funzione che aggiorna informazioni server
    public void setInfoServer(String stringa) {
        if (logData.get() == "") {
            logData.set(stringa);
        } else {
            logData.set(logData.get() + "\n" + stringa);
        }

    }
    
    @Override
    public void run() {
        try {
            // establish server socket
            while (true) {
                    //creo il socket server
                    ServerSocket s = new ServerSocket(8189);//x scrivi
                    
                    //rimango in attesa di connessioni
                    Socket clientSocket = s.accept();
                    //setInfoServer("Dopo accept");
                    Runnable connectionHandler = new ConnectionHandler(clientSocket);
                    //avvio il thread che scriverà l'email nel file
                    new Thread(connectionHandler).start();
                    //chiudo il socket per poi riaprilo alla prossima connesione
                    s.close();
                    setInfoServer("Email scritta con successo");
            }

        } catch (Exception e) {
            System.out.println(e.getCause());            
            System.out.println(e.getMessage());
        }
    }
}

