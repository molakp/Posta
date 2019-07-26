/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package posta;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author silve
 */
public class ConnectionHandler implements Runnable {

    private Socket incoming;

    public ConnectionHandler(Socket incomingSocket) {
        incoming = incomingSocket;

    }

    @Override
    public void run() {

        ObjectInputStream inStream;
        ObjectOutputStream objectOutputStream;
        String filePath= "C:\\Users\\silve\\Documents\\GitHub\\Posta\\";
        try {

            inStream = new ObjectInputStream(incoming.getInputStream());
            objectOutputStream = new ObjectOutputStream(incoming.getOutputStream());
            System.out.println("entrato nel try e creati stream");
            while (true) {
                try {

                    //String email = ((String) inStream.readObject());
                    // Ricevo la mail e inizio a processarla
                    Email email = ((Email) inStream.readObject());
                    System.out.println("Echo: " + email.emailString());
                    objectOutputStream.writeObject(email);
                    try {
                        String[] output = email.getTesto().split("\\|");

                        try {
                            System.out.println("Writing email in sender database ");
                            // Scrivo email in database mittente
                            FileWriter fw = new FileWriter(filePath + email.getMittente() + ".txt", true);
                            BufferedWriter bw = new BufferedWriter(fw);
                            PrintWriter out = new PrintWriter(bw);
                            out.println(email.emailString());
                            System.out.println("email scritta");
                            //fw.flush();
                            out.close();
                            bw.close();
                            fw.close();

                            System.out.println("Email wrote in sender database");
                        } catch (Exception e) {
                            System.out.println("Error in server in sender method");
                            e.printStackTrace();
                        }
                        try {
                            System.out.println("Writing email in receiver database ");
                            // Scrivo email in database destinatario
                            FileWriter fw = new FileWriter(filePath + email.getDestinatario()[0] + ".txt", true);
                            BufferedWriter bw = new BufferedWriter(fw);
                            PrintWriter out = new PrintWriter(bw);
                            out.println(email.emailString());
                            out.close();
                            bw.close();
                            fw.close();
                            System.out.println("Email wrote in receiver database");

                        } catch (IOException e) {
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
        } catch (IOException ex) {
            System.out.println("Error in server");
            Logger.getLogger(ConnectionHandler.class.getName()).log(Level.SEVERE, null, ex);

        }

    }

}
