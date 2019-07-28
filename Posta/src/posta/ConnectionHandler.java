/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package posta;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.*;
import java.io.DataOutputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.locks.Lock;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.stage.Stage;


/**
 *
 * @author silve
 */
public class ConnectionHandler implements Runnable {
    
    @FXML
    ListView logListView;

    private Socket incoming;

    public ConnectionHandler(Socket incomingSocket) {
        incoming = incomingSocket;

    }

    @Override
    public void run() {

        ObjectInputStream inStream;
        DataOutputStream objectOutputStream;
         String filePath = "C:\\Users\\silve\\Documents\\GitHub\\Posta\\";
      /*  try {
       
        Stage window = new Stage();
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("ServerGUI.fxml"));
         Parent root = (Parent) fxmlLoader.load();
       
       
         root = (Parent) fxmlLoader.load();
       
       // ConnectionHandler controller = fxmlLoader.<ConnectionHandler>getController();
        Scene scene1 = new Scene(root);
        scene1.getStylesheets().add(getClass().getResource("Viper.css").toExternalForm()); // lo carica
                      
        window.setScene(scene1);
        window.setTitle("Server GUI");
        window.show();
         } catch (Exception ex) {
            Logger.getLogger(ConnectionHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
*/
        
        try {

            inStream = new ObjectInputStream(incoming.getInputStream());
            objectOutputStream = new DataOutputStream(incoming.getOutputStream());
            System.out.println("entrato nel try e creati stream");
            while (true) {
                try {

                    //String email = ((String) inStream.readObject());
                    // Ricevo la mail e inizio a processarla
                    Email email = ((Email) inStream.readObject());
                    System.out.println("Echo: " + email.emailString());
                   
                    try {
                        String[] output = email.getTesto().split("\\|");

                        try {

                            System.out.println("Writing email in sender database ");
                            // Scrivo email in database mittente
                            FileWriter fw = new FileWriter(filePath + email.getMittente() + ".txt", true);
                            synchronized (fw) {
                                BufferedWriter bw = new BufferedWriter(fw);
                                PrintWriter out = new PrintWriter(bw);
                                out.println(email.emailString());

                                //fw.flush();
                                out.close();
                                bw.close();
                                fw.close();

                                System.out.println("Email wrote in sender database");
                            }
                        } catch (Exception e) {
                            System.out.println("Error in server in sender method");
                            e.printStackTrace();
                        }
                        try {
                            for (String destiString : email.getDestinatario()) {
                                
                                System.out.println("Writing email in receiver database ");
                                // Scrivo email in database destinatario
                                FileWriter fw = new FileWriter(filePath + destiString + ".txt", true);
                                synchronized (fw) {
                                    BufferedWriter bw = new BufferedWriter(fw);
                                    PrintWriter out = new PrintWriter(bw);
                                    out.println(email.emailString());
                                    out.close();
                                    bw.close();
                                    fw.close();
                                    System.out.println("Email wrote in  database of " + destiString);
                                    objectOutputStream.writeInt(1);
                                }
                            }
                        } catch (Exception e) {
                            System.out.println("Error in server in receiver method");
                            System.err.println(e.toString());
                        }

                    } catch (Exception e) {
                        System.out.println("Error in server");
                        System.out.println(e.getMessage());
                    }
                } catch (Exception e) {
                    System.out.println("Error in server");
                    System.out.println(e.getMessage());
                }
            }
        } catch (Exception ex) {
            System.out.println("Error in server");
            Logger.getLogger(ConnectionHandler.class.getName()).log(Level.SEVERE, null, ex);

        }

    }

}
