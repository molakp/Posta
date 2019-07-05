/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package posta;

import java.io.*;
import java.net.*;
import java.util.*;
import java.time.LocalDateTime;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.rmi.RemoteException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.scene.control.Button;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Callback;
import java.rmi.registry.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import java.rmi.registry.Registry;
import javafx.geometry.Pos;
import javafx.scene.layout.BorderPane;

/**
 *
 * @author silve
 */
public class FXMLDocumentController implements Initializable {
    // Oggetti finestra principale

    private Posta posta;
    @FXML
    private ListView<Email> emailView;

    @FXML
    private TextArea textArea;

    @FXML
    private Button scriviButton, eliminaButton, aggiornaButton;

    @FXML
    private Label objectLabel, senderLabel, objectLabel1, senderLabel1 = new Label();

    @FXML
    private AnchorPane right_pane = new AnchorPane();

    @FXML
    private ListView<Email> list_view;

    // Oggetti finestra visualize
    @FXML
    Pane l, visualizer;

    // Oggetti della finestra scrivi
    @FXML
    private Button invia;
    @FXML
    private TextField destinatario;
    @FXML
    private TextField oggetto;
    @FXML
    private TextArea testo;
//

    private ObservableList<Email> emailList = FXCollections.observableArrayList();
    private ObservableList<Email> selectedEmails = FXCollections.observableArrayList(); //qui salvo le email selezionate
    private String user;
    ObjectOutputStream outStream;
    ObjectInputStream inStream;
    /**
     * filename="Database.txt" va lasciato così altrimenti quando in Posta creo
     * l'oggetto FXMLDocumentController filename rimane a null producendo
     * eccezioni che non si vedono (perchè sono in posta) ma ci sono. In questo
     * modo carica un documento esistente e vuoto, senza dare eccezioni, poi
     * quando il login è avvenuto carica il file corrispondente.
     */
    private String filename = "Database.txt";

    public FXMLDocumentController() {

    }

    public void setUser(String userString) {
        user = userString;
        filename = user + ".txt";
        System.out.println("User is :" + user + "File is: " + filename);
        loadEmails();

    }

    @FXML
    @Override
    public void initialize(URL ur, ResourceBundle rb) {
        scriviButton = new Button();
        eliminaButton = new Button();

        loadEmails();

        /* IMPORTANTE!!! nno c'è bisogno di dichiarare list_view= new ListView<>();
            perchè ci pensa già l'FXML a farlo! se lo facciamo creiamo un duplicato che non vedremo nella GUI
            Per questo usiamo addAll, in modo da aggiungere la emailList a dichiarazuione già fatta.
         */
        list_view.getItems().addAll(emailList);
        emailList.sort((Email o1, Email o2) -> (o1.getDate().isBefore(o2.getDate())) ? 1 : 0);// qua basta invertire 0 e 1 per cambiare l'ordine, così mostra prima quelle arrivate/create più recentemente

        try {

            String nomeHost = InetAddress.getLocalHost().getHostName();
            System.out.println(nomeHost);
            Socket s = new Socket(nomeHost, 8189);
            System.out.println("Ho aperto il socket verso il server.\n");
            // InputStream inputStream = s.getInputStream();
            //Scanner in = new Scanner(inStream);
            outStream = new ObjectOutputStream(s.getOutputStream());// apro stream output
            System.out.println("Ho aperto stream output\n");
            InputStream simpleinInputStream = s.getInputStream();
             //receiveEmails(s);
            System.out.println("Ho aperto simple stream input\n");
            inStream = new ObjectInputStream(simpleinInputStream);// apro stream input
            System.out.println("Ho aperto  stream input\n");
            // outStream.writeObject("ciao");

        } catch (Exception ex) {
            Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
            System.err.println("Client exception: " + ex.toString());
            ex.printStackTrace();
        }

        /*CellFactory permette di definire delle celle custom, ppossibile anche fare un file xml a parte
           dove disegno la cella come più mi piace. Vedere https://www.turais.de/how-to-custom-listview-cell-in-javafx/ 
         */
        list_view.setCellFactory(param -> new ListCell<Email>() {
            @Override
            protected void updateItem(Email item, boolean empty) {

                super.updateItem(item, empty);

                if (empty || item == null || item.toString() == null) {
                    setText(null);
                } else {
                    setText(item.toString());

                }
            }
        });

        list_view.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);// posso selezionare più item con ctrl + click

        // gestisco doppio click su email per visualizzarla 
        list_view.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                if (mouseEvent.getButton().equals(MouseButton.PRIMARY)) {
                    if (mouseEvent.getClickCount() == 2) {
                        System.out.println("Double clicked");
                        visualizeEmail();
                    }
                }
            }
        });

        updateEmailList();
      
    }

    /**
     * Avvia Thread di ricezione email
     *
     * @param s
     */
    public void receiveEmails(Socket s){
        
          Runnable emailReceiver = new ReceiveEmail(s);
                new Thread(emailReceiver).start();
    
    
    }
    /**
     *
     * Carica le email presenti nel file in emailList Chiamato solo all'avvio
     * del programma
     */
    private void loadEmails() {

        //leggiamo il database per caricare le email salvate
        List<String> records = readFile("C:\\Users\\silve\\Desktop\\Uni\\prog3\\Lab\\Grafica\\Posta\\Posta\\src\\posta\\" + filename);

        for (String s : records) {
            String[] output = s.split("\\|");

            String[] destinatariStrings = output[3].split("$");

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS");
            // date1 = new SimpleDateFormat("h:mm a").parse(output[1]);
            LocalDateTime dateTime = LocalDateTime.parse(output[1], formatter);
            System.out.println(dateTime);

            emailList.add(new Email(Integer.parseInt(output[0]), dateTime, output[2], destinatariStrings, output[4], output[5]));

        }
    }

    /**
     * il contrario di load, scrive tutte le email presenti nell'array nel file
     * Database.txt presente sotto Posta, non quello dentro src/posta che invece
     * non viene mai toccato. Questo metodo va chiamato ogni volta chge si
     * eseguono operazioni sulle email per aggiornare il fine e salvare tutto,
     * quindi l'ho messo in updateEmailList(), in modo che in un colpo solo
     * aggiorna file e interfaccia
     */
    private void saveEmails() {
        try {
            //  PrintWriter pw = new PrintWriter("Database.txt");
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(filename));
            //pw.close();
            // BufferedReader br1 = new BufferedReader(new FileReader("Database.txt"));

            emailList.forEach((e) -> {
                try {
                    bufferedWriter.write(e.getId() + "|" + e.getDate() + "|" + e.getMittente() + "|");
                    for (String s : e.getDestinatario()) {
                        bufferedWriter.write(s + "$");
                    }
                    bufferedWriter.write("|" + e.getArgomento() + "|" + e.getTesto());

                } catch (IOException ex) {
                    Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
                }
            });
            bufferedWriter.close();
        } catch (Exception ex) {
            Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    private List<String> readFile(String filename) {
        List<String> records = new ArrayList<String>();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(filename));
            String line;
            while ((line = reader.readLine()) != null) {
                records.add(line);
            }
            reader.close();
            return records;
        } catch (Exception e) {
            System.err.format("Exception occurred trying to read '%s'.", filename);
            e.printStackTrace();
            return null;
        }
    }

    public void visualizeEmail() {

        System.out.println("Apertura in corso");
        try {
            Stage window = new Stage();

            Pane root = new Pane();

            Scene scene = new Scene(root);
            TextArea emailTextArea = new TextArea();
            scene.getStylesheets().add(getClass().getResource("Viper.css").toExternalForm()); // lo carica 
            selectedEmails = list_view.getSelectionModel().getSelectedItems();
            emailTextArea.setText(selectedEmails.get(0).getTesto());
            Button reply = new Button("Rispondi");
            emailTextArea.setEditable(false);
            root.getChildren().addAll(emailTextArea, reply);
            window.setScene(scene);
            window.setTitle("Email v0.4");
            window.show();

        } catch (Exception e) {
            System.out.println(e.getCause() + e.toString());

        }
    }

    public boolean eliminaMail() {
        //selectedEmails.clear(); lasciare commentata perchè da un sacco di eccezioni e errori anche se non ho capito perchè
        selectedEmails = list_view.getSelectionModel().getSelectedItems();// così metto le email selezionate qui dentro per poi farci che voglio.

        boolean r = emailList.removeAll(selectedEmails);

        System.out.println("ELIMINA");

        updateEmailList();
        return r;
    }

    public void updateEmailList() {
        // Aggiorna la vista elenco email e il file di testo
        saveEmails();
        list_view.getItems().clear(); // cancella tutto il contenuto della list view
        list_view.getItems().addAll(emailList);// ripopola la list view con le email aggiornate

    }

    public void scriviEmail() {

        System.out.println("SCRIVI");
        try {
            Stage window = new Stage();

            Pane root = new Pane();

            Scene scene = new Scene(root);
            BorderPane borderPane = new BorderPane();

            root.getChildren().add(borderPane);
            TextField destField = new TextField();
            TextField oggettoField = new TextField("Inserisci oggetto");
            TextArea emailTextArea = new TextArea("Testo");

            Button inviaButton = new Button("Invia");
            VBox topBox = new VBox();
            topBox.getChildren().addAll(destField, oggettoField);
            borderPane.setTop(topBox);

            borderPane.setCenter(emailTextArea);
            borderPane.setBottom(inviaButton);
            scene.getStylesheets().add(getClass().getResource("Viper.css").toExternalForm()); // lo carica 

            window.setScene(scene);
            window.setTitle("Scrivi");
            window.show();

            inviaButton.setOnAction((event) -> {
                System.out.print(emailTextArea.getText());
                String destinatariRawString = destField.getText();
                String[] arrayDestinatariString = destinatariRawString.split(";");
                Email toSendEmail = new Email(12345, LocalDateTime.now(), user, arrayDestinatariString, oggettoField.getText(), emailTextArea.getText());
                emailList.add(toSendEmail);

                try {
                    // outStream.writeObject("12345" + "|" + LocalDateTime.now().toString().toString() + "|" + user + "|" + destField.getText() + "|" + oggettoField.getText() + "|" + emailTextArea.getText());
                    outStream.writeObject(toSendEmail);

                } catch (IOException ex) {
                    Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
                }

                window.close(); // chiudo la finestra

                System.out.println("Email salvate!");
                updateEmailList();  // aggiorno la lista 
            });

        } catch (Exception e) {
            System.out.println(e.getCause() + e.toString());

        }

    }

}

class ReceiveEmail implements Runnable {

    private Socket incoming;
    ObjectInputStream inStream;
    public ReceiveEmail(Socket incomingSocket) {
        incoming = incomingSocket;

    }

    @Override
    public void run() {
        try {
            inStream = new ObjectInputStream(incoming.getInputStream());
            while (true) {
                try {
                    Email receivedEmail = ((Email) inStream.readObject());
                    System.out.println("Echo: " + receivedEmail.emailString());
                    
                } catch (Exception ex) {
                    Logger.getLogger(FXMLDocumentController.class
                            .getName()).log(Level.SEVERE, null, ex);
                     
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(ReceiveEmail.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

}
