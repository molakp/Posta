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
import java.util.Observable;
import java.util.concurrent.locks.Lock;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

/**
 *
 * @author silve
 */
public class ConnectionHandler implements Runnable {

    @FXML
    ListView logListView;

    private Socket incoming;
    private PipedWriter outPipedWriter;
    private String eventToLog;

    public ConnectionHandler(Socket incomingSocket) {
        incoming = incomingSocket;

    }

    @Override
    public void run() {
        //  launch("");

        ObjectInputStream inStream;
        DataOutputStream objectOutputStream;
        String filePath = "C:\\Users\\silve\\Documents\\GitHub\\Posta\\";

        try {
            inStream = new ObjectInputStream(incoming.getInputStream());
            objectOutputStream = new DataOutputStream(incoming.getOutputStream());
            System.out.println("entrato nel try e creati stream. I'm thread: " + Thread.currentThread().toString());

            while (true) {
                if (incoming.isClosed() == false) {
                    try {

                        // Ricevo la mail e inizio a processarla
                        Email email = ((Email) inStream.readObject());
                        System.out.println("Echo: " + email.emailString());
                        inStream.close();

                        try {
                            String[] output = email.getTesto().split("\\|");

                            try {

                                System.out.println("Writing email in sender database ");
                                eventToLog = "Writing email in sender database " + email.getMittente();
                                //  setChanged();
                                //notifyObservers();
                                // outPipedWriter.write("Writing email in "+ email.getMittente() +" database ");
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

                                        //objectOutputStream.writeInt(1);

                                        System.out.println("Email wrote in  database of " + destiString + "\n The buffer size is: " + objectOutputStream.size());
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
                    Thread.currentThread().interrupt();

                
            }
        } catch (IOException ex) {
            Logger.getLogger(ConnectionHandler.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public Thread getThreadByName(String threadName) {
        for (Thread t : Thread.getAllStackTraces().keySet()) {
            if (t.getName().equals(threadName)) {
                return t;
            }
        }
        return null;
    }
}
