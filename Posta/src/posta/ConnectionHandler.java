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
import java.io.PrintWriter;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ConnectionHandler implements Runnable {

    String filePath = "C:\\Users\\aldob\\Desktop\\Posta\\";

    private Socket incoming;
    private SocketServer obj = new SocketServer();

    public ConnectionHandler(Socket incomingSocket) {
        incoming = incomingSocket;

    }

    @Override
    public void run() {
        ObjectInputStream inStream;
        ObjectOutputStream objectOutputStream;
        DataInputStream emailRead;
        String filePath = "C:\\Users\\aldob\\Desktop\\Posta\\";

        try {

            inStream = new ObjectInputStream(incoming.getInputStream());

            objectOutputStream = new ObjectOutputStream(incoming.getOutputStream());
            final Object inputObject = inStream.readObject();
            if (inputObject instanceof Email) {//caso in cui si deve scrivere sul file l'email
                if (incoming.isClosed() == false) {
                    try {

                        // Ricevo la mail e inizio a processarla
                        Email email = (Email) inputObject;
                        obj.setInfoServer("Contenuto email :\n" + email.emailString() + "  ");
                        inStream.close();

                        try {
                            String[] output = email.getTesto().split("\\|");

                            try {

                                FileWriter fw = new FileWriter(filePath + email.getMittente() + ".txt", true);
                                synchronized (fw) {
                                    BufferedWriter bw = new BufferedWriter(fw);
                                    PrintWriter out = new PrintWriter(bw);
                                    out.println(email.emailString());

                                    out.close();
                                    bw.close();
                                    fw.close();
                                    obj.setInfoServer("Email scritta nel database del mittente : " + email.getMittente());
                                }
                            } catch (Exception e) {
                                System.out.println("Error in server in sender method");
                                e.printStackTrace();
                            }
                            try {
                                for (String destiString : email.getDestinatario()) {
                                    // Scrivo email in database destinatario
                                    FileWriter fw = new FileWriter(filePath + destiString + ".txt", true);
                                    synchronized (fw) {
                                        BufferedWriter bw = new BufferedWriter(fw);
                                        PrintWriter out = new PrintWriter(bw);
                                        out.println(email.emailString());
                                        out.close();
                                        bw.close();
                                        fw.close();

                                        obj.setInfoServer("Email scritta nel database del destinatario : " + destiString);

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
                        System.out.println("Error in server 1");
                        System.out.println(e.getCause());
                        e.printStackTrace();
                    }
                }
                //email scritta con successo, posso procedere con l'interruzione del thread 
                Thread.currentThread().interrupt();
            } else if (inputObject instanceof String) {//caso in cui devono essere caricate tutte le email
                System.out.println("Aggorna mail");
                // Carica le email presenti nel file in emailList Chiamato solo all'avvio del programma
                //leggiamo il database relativo all'utente per caricare le email salvate
                try {
                    List<String> records;
                    records = readFile(filePath + (String) inputObject + ".txt");
                    ArrayList<Email> emailList = new ArrayList<>();
                    for (String s : records) {
                        if (!"\n".equals(s)) {
                            String[] output = s.split("\\|");

                            String[] destinatariStrings = output[3].split(";"); // i destinatari sono separati da un $ 
                            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS");
                            LocalDateTime dateTime = LocalDateTime.parse(output[1], formatter);
                            emailList.add(new Email(Integer.parseInt(output[0]), dateTime, output[2], destinatariStrings, output[4], output[5]));

                        }
                    }
                    objectOutputStream.writeObject(emailList);
                    //receiveEmails();
                    System.out.println("Fatto aggiornamento");
                } catch (Exception e) {
                    System.err.println("Wrong username in login! ");
                }

            } else if (inputObject instanceof String[]) {
                System.out.println("Entrato in elimina email");
                ArrayList<Email> oldEmails = new ArrayList<>();
                String[] deleteEmails = (String[]) inputObject;
                String user = deleteEmails[0];
                //leggiamo il database relativo all'utente per caricare le email salvate
                try {
                    List<String> records;
                    records = readFile(filePath + user + ".txt");
                    for (String s : records) {
                        if (!"\n".equals(s)) {
                            String[] output = s.split("\\|");

                            String[] destinatariStrings = output[3].split(";"); // i destinatari sono separati da un $ 

                            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS");
                            LocalDateTime dateTime = LocalDateTime.parse(output[1], formatter);
                            oldEmails.add(new Email(Integer.parseInt(output[0]), dateTime, output[2], destinatariStrings, output[4], output[5]));

                        }
                    }
                    System.out.println("Letto file");
                    deleteEmails[0] = "0"; // tolgo user da 
                    ArrayList<Email> email_da_togliere = new ArrayList<>();
                    for (String s : deleteEmails) {
                        System.out.println("LA stringa è:  " + s);
                        if (s != null) {
                            for (Email e : oldEmails) {
                                System.out.println("Mail id  è:  " + e.getId());
                                if (e.getId() == Integer.parseInt(s)) {
                                    // Email t = e;
                                    email_da_togliere.add(e);
                                }

                            }
                        }

                    }
                    oldEmails.removeAll(email_da_togliere);
                    System.out.println(" RImosse email, ");
                     for(Email e: oldEmails)
                         System.out.println(e.toString());
                    try {
                        BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(filePath + user + ".txt"));

                        oldEmails.forEach((e) -> {
                            try {

                                bufferedWriter.write(e.emailString() + "\n");

                            } catch (IOException ex) {
                                Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        });
                        bufferedWriter.close();
                    } catch (Exception ex) {
                        Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
                    }

                } catch (Exception e) {
                    System.err.println("Errore in elimina mail! " + e.getMessage());
                }

            }

        } catch (IOException ex) {
            System.out.println("Entrato qui ..... " + ex.toString());
            Logger.getLogger(ConnectionHandler.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(ConnectionHandler.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    private List<String> readFile(String file) {
        List<String> records = new ArrayList<String>();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line;
            while ((line = reader.readLine()) != null) {
                records.add(line);
            }
            reader.close();
            return records;
        } catch (Exception e) {
            System.out.println("Exception occurred trying to read : " + file);
            e.printStackTrace();
            return null;
        }
    }

}
