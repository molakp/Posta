/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package posta;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import static com.sun.glass.ui.Cursor.setVisible;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javax.swing.JFrame;
import javax.swing.JLabel;
import static posta.ConfirmBox.display;


/**
 *
 * @author silve
 */
public class SocketServer {

    public static void main(String[] args) {
        System.out.println("Finestra del socket server");
        try {
            // establish server socket
            ServerSocket s = new ServerSocket(8189);
          
            while (true) {
                try {
                    
                System.out.println("Avvio nuovo thread");
                Socket clientSocket = s.accept();
                Runnable connectionHandler = new ConnectionHandler(clientSocket);
               // Finestra f= new Finestra
                new Thread(connectionHandler).start();
                    
                } catch (Exception e) {
                   
                }


            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*public void update(Observable ob, Object x) {
        display.setText("Saldo = " + cb.getSaldo());
    } */

    /*@Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Hello World!");
        Button btn = new Button();
        btn.setText("Say 'Hello World'");
        btn.setOnAction(new EventHandler<ActionEvent>() {
 
            @Override
            public void handle(ActionEvent event) {
                System.out.println("Hello World!");
            }
        });
        
        StackPane root = new StackPane();
        root.getChildren().add(btn);
        primaryStage.setScene(new Scene(root, 300, 250));
        primaryStage.show();
    } 

     */
    public void createSocket() {

    }
}

/*class Finestra extends JFrame implements
        Observer {

    private JLabel display;
    private ContoBancario cb;

    public Finestra( conto) {
        cb = conto;
        display = new JLabel();
        add(display);
        display.setText("Saldo = " + 0);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        pack();
        setVisible(true);
    }

    public void update(Observable ob, Object x) {
        display.setText("Saldo = " + cb.getSaldo());
    }
} */
