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
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

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
               
                System.out.println("Avvio nuovo thread");
                Socket clientSocket = s.accept();
                Runnable connectionHandler = new ConnectionHandler(clientSocket);
                new Thread(connectionHandler).start();

            }

            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
