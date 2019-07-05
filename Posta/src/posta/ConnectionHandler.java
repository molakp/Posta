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
        try {
            
            inStream = new ObjectInputStream(incoming.getInputStream());
            objectOutputStream= new  ObjectOutputStream(incoming.getOutputStream());
            System.out.println("entrato nel try creati stream");
            while (true) {
                try {

                    //String email = ((String) inStream.readObject());
                    
                    Email email = ((Email) inStream.readObject());
                    System.out.println("Echo: " + email.emailString());
                    objectOutputStream.writeObject(email);
                    try {
                       // String[] output = email.split("\\|");

                       /* try (FileWriter fw = new FileWriter("C:\\Users\\silve\\Desktop\\Uni\\prog3\\Lab\\Grafica\\"+ output[3]+".txt", true); BufferedWriter bw = new BufferedWriter(fw); PrintWriter out = new PrintWriter(bw)) {
                            out.println(email);

                        } catch (IOException e) {
                            System.err.println(e.toString());
                        } */

                    } catch (Exception e) {
                        System.out.println(e.getMessage());
                    }
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(ConnectionHandler.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

}
