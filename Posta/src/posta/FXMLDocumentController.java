/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package posta;

import java.io.*;
import java.io.DataInputStream;
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
import java.util.regex.Pattern;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.BorderPane;
import javax.swing.SwingUtilities;

/**
 *
 * @author silve
 */
public class FXMLDocumentController implements Initializable, Observer {
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

    private ObservableList<Email> emailList = FXCollections.observableArrayList();
    private ObservableList<Email> selectedEmails = FXCollections.observableArrayList(); //qui salvo le email selezionate
    private String user;
    ObjectOutputStream outStream;
    DataInputStream inStream;
    /**
     * filename="Database.txt" va lasciato così altrimenti quando in Posta creo
     * l'oggetto FXMLDocumentController filename rimane a null producendo
     * eccezioni che non si vedono (perchè sono in posta) ma ci sono. In questo
     * modo carica un documento esistente e vuoto, senza dare eccezioni, poi
     * quando il login è avvenuto carica il file corrispondente.
     */
    private String filename = "Database.txt";
    private String filePath;
    private Notification notify;

    public FXMLDocumentController() {

    }

    // Imposta l'utente 
    public void setUser(String userString) {
        user = userString;
        filename = user + ".txt";
        filePath = "C:\\Users\\silve\\Documents\\GitHub\\Posta\\" + filename;
        System.out.println("User is :" + user + "\n File is: " + filename + " \n File path is: " + filePath);
        Thread.currentThread().setName(user); // imposto il nome del thread col nome utente
        loadEmails();

    }

    @FXML
    @Override
    public void initialize(URL ur, ResourceBundle rb) {
        scriviButton = new Button();
        eliminaButton = new Button();

        /* IMPORTANTE!!! nno c'è bisogno di dichiarare list_view= new ListView<>();
            perchè ci pensa già l'FXML a farlo! se lo facciamo creiamo un duplicato che non vedremo nella GUI
            Per questo usiamo addAll, in modo da aggiungere la emailList a dichiarazuione già fatta.
         */
        list_view.getItems().addAll(emailList);
        emailList.sort((Email o1, Email o2) -> (o1.getDate().isBefore(o2.getDate())) ? 1 : 0);// qua basta invertire 0 e 1 per cambiare l'ordine, così mostra prima quelle arrivate/create più recentemente

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

        /*try {
            Thread.sleep(2000);
             receiveEmails();
        } catch (InterruptedException ex) {
            Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
        } */
    }

    /**
     * Avvia Thread di ricezione email, crea runnable con classe receiver ed
     * esegue metodo readFromDatabase alla ricezione per aggiornare la view
     *
     * @param s
     */
    public void receiveEmails() throws InterruptedException {

        //  Runnable emailReceiver = new ReceiveEmail(s);
        Updater up = new Updater(filePath, user, emailList);
        notify = new Notification(up);
        up.addObserver(notify);
        up.addObserver(this);
        // new Thread(emailReceiver).start();
        // Platform.runLater(emailReceiver);
        // Thread.sleep(10);
        Thread t = new Thread(up);
        t.setDaemon(true);
        /*  
         if you want the background threads to simply terminate after all the stages are closed, then you must set daemon to true.
         https://docs.oracle.com/javafx/2/api/javafx/concurrent/Task.html  */
        t.start();

    }

    /**
     *
     * Carica le email presenti nel file in emailList Chiamato solo all'avvio
     * del programma
     */
    private void loadEmails() {

        //leggiamo il database relativo all'utente per caricare le email salvate
        try {
            List<String> records;
            records = readFile(filePath);
            for (String s : records) {
                if (!"\n".equals(s)) {
                    String[] output = s.split("\\|");

                    String[] destinatariStrings = output[3].split(";"); // i destinatari sono separati da un $ 

                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS");
                    // date1 = new SimpleDateFormat("h:mm a").parse(output[1]);
                    LocalDateTime dateTime = LocalDateTime.parse(output[1], formatter);
                    // System.out.println(dateTime);
                    /*  Esempio di email 12345|2019-05-10T17:07:24.764|mario@ciao|silvestro@prog.com$mario@ciao$|adsfdasfasdfs|dsafjdasfjkafjsdabajsdf*/
                    emailList.add(new Email(Integer.parseInt(output[0]), dateTime, output[2], destinatariStrings, output[4], output[5]));
                    updateEmailList();
                }
            }
            receiveEmails();

        } catch (Exception e) {
            System.err.println("Wrong username in login! ");
        }

    }

    /**
     * il contrario di load, scrive tutte le email presenti nell'array nel file
     * presente sotto Posta, non quello dentro src/posta che invece non viene
     * mai toccato. Questo metodo va chiamato ogni volta chge si eseguono
     * operazioni sulle email per aggiornare il fine e salvare tutto, quindi
     * l'ho messo in updateEmailList(), in modo che in un colpo solo aggiorna
     * file e interfaccia
     */
    private void saveEmails() {
        try {
            //  PrintWriter pw = new PrintWriter("Database.txt");
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(filePath));
            //pw.close();
            // BufferedReader br1 = new BufferedReader(new FileReader("Database.txt"));

            emailList.forEach((e) -> {
                try {
                    /* bufferedWriter.write(e.getId() + "|" + e.getDate() + "|" + e.getMittente() + "|");
                    for (String s : e.getDestinatario()) {
                        bufferedWriter.write(s + "$");
                    }
                    bufferedWriter.write("|" + e.getArgomento() + "|" + e.getTesto()); */
                    bufferedWriter.write(e.emailString() + "\n");

                } catch (IOException ex) {
                    Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
                }
            });
            bufferedWriter.close();
        } catch (Exception ex) {
            Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    private List<String> readFile(String file) {
        List<String> records = new ArrayList<String>();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
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

    public void visualizeEmail() {

        System.out.println("Apertura in corso");
        try {
            Stage window = new Stage();

            Pane root = new Pane();
            BorderPane borderPane = new BorderPane();
            root.getChildren().add(borderPane);
            Scene scene = new Scene(root);
            TextArea emailTextArea = new TextArea();
            scene.getStylesheets().add(getClass().getResource("Viper.css").toExternalForm()); // lo carica 
            selectedEmails = list_view.getSelectionModel().getSelectedItems();
            emailTextArea.setText(selectedEmails.get(0).getTesto());
            Button reply = new Button("Reply");
            Button forwardButton = new Button("Forward");
            emailTextArea.setEditable(false);
            VBox topBox = new VBox();
            topBox.getChildren().addAll(emailTextArea, reply, forwardButton);
            borderPane.setCenter(topBox);
            window.setScene(scene);
            window.setTitle("Email v0.6 User:" + user);
            window.show();
            reply.setOnAction(((event) -> {
                window.close();
                reply(selectedEmails.get(0).getMittente());
            }));

            forwardButton.setOnAction(((event) -> {

                window.close();
                forward(selectedEmails.get(0).getTesto());

            }));

        } catch (Exception e) {
            System.out.println(e.getCause() + e.toString());

        }

    }

    /**
     * Simile a scrivi, ma imposta il testo della textArea su textString, per
     * fare il forward
     *
     * @param textString
     */
    public void forward(String textString) {
        System.out.println("SCRIVI");
        try {

            Stage window = new Stage();

            Pane root = new Pane();

            Scene scene = new Scene(root);
            BorderPane borderPane = new BorderPane();

            root.getChildren().add(borderPane);
            TextField destField = new TextField();

            TextField oggettoField = new TextField("Inserisci oggetto");
            TextArea emailTextArea = new TextArea(textString);

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
                    inStream = new DataInputStream(simpleinInputStream);// apro stream input
                    System.out.println("Ho aperto  stream input\n");
                    System.out.print(emailTextArea.getText());
                    String destinatariRawString = destField.getText();
                    String[] arrayDestinatariString = destinatariRawString.split(";");
                    for (String string : arrayDestinatariString) {
                        System.out.println("destinatari " + string);

                    }
                    double randomDouble = Math.random();
                    randomDouble = randomDouble * 50000000 + 1;
                    int idEmail = (int) randomDouble;
                    //limitarsi a creare l'oggetto email, ogni problema legato alla formattazione è delegato al metodo emailString di email
                    Email toSendEmail = new Email(idEmail, LocalDateTime.now(), user, arrayDestinatariString, oggettoField.getText(), emailTextArea.getText());
                    emailList.add(toSendEmail);

                    try {
                        // outStream.writeObject("12345" + "|" + LocalDateTime.now().toString().toString() + "|" + user + "|" + destField.getText() + "|" + oggettoField.getText() + "|" + emailTextArea.getText());
                        outStream.writeObject(toSendEmail);
                        s.close();
                    } catch (IOException ex) {
                        Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
                    }

                    window.close(); // chiudo la finestra

                    System.out.println("Email salvate!");
                    list_view.getItems().clear(); // cancella tutto il contenuto della list view
                    list_view.getItems().addAll(emailList);// ripopola la list view con le email più la nuova appena mandata
                    // updateEmailList();  // aggiorno la lista
                } catch (Exception ex) {
                    Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
                }
            });

        } catch (Exception e) {
            System.out.println(e.getCause() + e.toString());

        }
    }

    public boolean eliminaMail() {
        selectedEmails = list_view.getSelectionModel().getSelectedItems();// così metto le email selezionate qui dentro per poi farci che voglio.

        boolean r = emailList.removeAll(selectedEmails);

        System.out.println("ELIMINA");

        updateEmailList();
        return r;
    }

    public void updateEmailList() {
        //Scrivo quello che ho nella List nel file
        // Aggiorna la vista elenco email e il file di testo
        saveEmails();
        list_view.getItems().clear(); // cancella tutto il contenuto della list view
        list_view.getItems().addAll(emailList);// ripopola la list view con le email aggiornate

    }

    public synchronized void readFromDatabase() {
        emailList.clear();// cancello tutte le email  per evitare duplicati
        list_view.getItems().clear(); // cancella tutto il contenuto della list view
        //  loadEmails(); // leggo le email dal file e le carico nella List
        //leggiamo il database relativo all'utente per caricare le email salvate
        try {
            List<String> records;
            records = readFile(filePath);
            for (String s : records) {
                String[] output = s.split("\\|");

                String[] destinatariStrings = output[3].split(";"); // i destinatari sono separati da un $ 
                for (String destString : destinatariStrings) {
                    System.out.println(" \n Un destinatario è " + destString);
                }

                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS");
                // date1 = new SimpleDateFormat("h:mm a").parse(output[1]);
                LocalDateTime dateTime = LocalDateTime.parse(output[1], formatter);
                System.out.println(dateTime);
                /*  Esempio di email 12345|2019-05-10T17:07:24.764|mario@ciao|silvestro@prog.com$mario@ciao$|adsfdasfasdfs|dsafjdasfjkafjsdabajsdf*/
                emailList.add(new Email(Integer.parseInt(output[0]), dateTime, output[2], destinatariStrings, output[4], output[5]));

            }

        } catch (Exception e) {
            System.err.println(e.getStackTrace());
        }

        list_view.getItems().addAll(emailList);// ripopola la list view con le email aggiornate
    }

    /**
     * Carica interfaccia per scrivere email, crea oggetto Email e lo scrive
     * sullo stream col server,poi aggiorna la vista e aggiunge la mail alla
     * List
     */
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
            window.setTitle("Scrivi User is :" + user);
            window.show();

            inviaButton.setOnAction((event) -> {

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
                    inStream = new DataInputStream(simpleinInputStream);// apro stream input
                    System.out.println("Ho aperto  stream input\n");
                    // outStream.writeObject("ciao");
                    System.out.print(emailTextArea.getText());
                    String destinatariRawString = destField.getText();
                    String[] arrayDestinatariString = destinatariRawString.split(";");
                    for (String string : arrayDestinatariString) {
                        System.out.println("destinatari " + string);

                    }
                    //limitarsi a creare l'oggetto email, ogni problema legato alla formattazione è delegato al metodo emailString di email
                    double randomDouble = Math.random();
                    randomDouble = randomDouble * 50000000 + 1;
                    int idEmail = (int) randomDouble;
                    Email toSendEmail = new Email(idEmail, LocalDateTime.now(), user, arrayDestinatariString, oggettoField.getText(), emailTextArea.getText());
                    emailList.add(toSendEmail);

                    try {
                        // outStream.writeObject("12345" + "|" + LocalDateTime.now().toString().toString() + "|" + user + "|" + destField.getText() + "|" + oggettoField.getText() + "|" + emailTextArea.getText());
                        outStream.writeObject(toSendEmail);

                    } catch (IOException ex) {
                        Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
                    }

                    window.close(); // chiudo la finestra

                    System.out.println("Email salvate!");
                    list_view.getItems().clear(); // cancella tutto il contenuto della list view
                    list_view.getItems().addAll(emailList);// ripopola la list view con le email più la nuova appena mandata
                    // updateEmailList();  // aggiorno la lista 
                    s.close();
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }

            });

        } catch (Exception e) {
            System.out.println(e.getCause() + e.toString());

        }

    }

    /**
     * Overloading di Scrivi da usare nel caso di reply, prende come
     * destinatario la Stringa dest passata come parametro
     *
     * @param String dest
     */
    public void reply(String dest) {
        System.out.println("SCRIVI");
        try {

            Stage window = new Stage();

            Pane root = new Pane();

            Scene scene = new Scene(root);
            BorderPane borderPane = new BorderPane();

            root.getChildren().add(borderPane);
            TextField destField = new TextField();
            String destinatariString = dest + ";";
            destField.setText(destinatariString);
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
                    inStream = new DataInputStream(simpleinInputStream);// apro stream input
                    System.out.println("Ho aperto  stream input\n");

                    System.out.print(emailTextArea.getText());
                    String destinatariRawString = destField.getText();
                    String[] arrayDestinatariString = destinatariRawString.split(";");
                    for (String string : arrayDestinatariString) {
                        System.out.println("destinatari " + string);

                    }
                    //limitarsi a creare l'oggetto email, ogni problema legato alla formattazione è delegato al metodo emailString di email
                    double randomDouble = Math.random();
                    randomDouble = randomDouble * 50000000 + 1;
                    int idEmail = (int) randomDouble;
                    Email toSendEmail = new Email(idEmail, LocalDateTime.now(), user, arrayDestinatariString, oggettoField.getText(), emailTextArea.getText());
                    emailList.add(toSendEmail);

                    try {
                        // outStream.writeObject("12345" + "|" + LocalDateTime.now().toString().toString() + "|" + user + "|" + destField.getText() + "|" + oggettoField.getText() + "|" + emailTextArea.getText());
                        outStream.writeObject(toSendEmail);

                    } catch (IOException ex) {
                        Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    s.close();
                    window.close(); // chiudo la finestra

                    System.out.println("Email salvate!");
                    list_view.getItems().clear(); // cancella tutto il contenuto della list view
                    list_view.getItems().addAll(emailList);// ripopola la list view con le email più la nuova appena mandata
                    // updateEmailList();  // aggiorno la lista
                } catch (Exception ex) {
                    Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
                }
            });

        } catch (Exception e) {
            System.out.println(e.getCause() + e.toString());

        }

    }

    public Notification getNotify() {
        return notify;

    }

    public void AlertEmail() {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("New email!");

        alert.setContentText("You have a new message!");

        alert.showAndWait();
    }

    @Override
    public void update(Observable o, Object arg) {
        //  Alert alert = new Alert(AlertType.INFORMATION);

    }
}

class ReceiveEmail implements Runnable {

    private Socket incoming;
    DataInputStream inStream;

    public ReceiveEmail(Socket incomingSocket) {
        incoming = incomingSocket;

    }

    @Override
    public void run() {
        try {
            inStream = new DataInputStream(incoming.getInputStream());
            boolean keepAlive = true;
            while (keepAlive) {

                try {
                    //Thread.sleep(100);
                    int messageCode = inStream.readInt();
                    if (messageCode == 1) { // se server ha scritto email
                        // Non posso modificare GUI altirmenti lancia
                        //java.lang.IllegalStateException: Not on FX application thread 

                        /* Alert alert = new Alert(AlertType.INFORMATION);
                        alert.setTitle("New email!");
                 
                        alert.setContentText("You have a new message!");

                        alert.showAndWait(); */
                        //FXMLDocumentController fx = new  FXMLDocumentController();
                        //fx.AlertEmail();
                        System.out.println("NOTIFY!!!!" + this.getClass().getName());
                    }

                } catch (Exception ex) {
                    keepAlive = false;
                    inStream.close();
                    System.out.println("Socket error, interrupting service");
                    Logger.getLogger(FXMLDocumentController.class
                            .getName()).log(Level.SEVERE, null, ex);

                }
            }
        } catch (IOException ex) {
            Logger.getLogger(ReceiveEmail.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

}

class Updater extends Observable implements Runnable {

    private String pathFile;
    private String userString;
    private List<Email> emailList = new ArrayList();
    private List<Email> compareEmails = new ArrayList();
    private List<Email> thirdList = new ArrayList();
    private List<Email> iterEmails = new ArrayList();
    private boolean newEmail = false;

    public Updater(String path, String user, List<Email> compEmails) {
        pathFile = path;
        userString = user;
        compareEmails.addAll(compEmails);

    }

    @Override
    public void run() {
        System.out.println("Pathfile: " + pathFile + "Userstring: " + userString);
        List<String> records;
        while (true) {
            try {
                newEmail = false;

                Thread.currentThread().setName("Thread Updater");
                Thread.sleep(5000); // Ogni 5 secondi scansiona il file e carica le email. se ne trova una nuova in cui mittente!= utente fa update a osberver

                records = readFile(pathFile);
                for (String s : records) {
                    if (!"\n".equals(s)) {
                        String[] output = s.split("\\|");

                        String[] destinatariStrings = output[3].split(";"); // i destinatari sono separati da un $

                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS");
                        // date1 = new SimpleDateFormat("h:mm a").parse(output[1]);
                        LocalDateTime dateTime = LocalDateTime.parse(output[1], formatter);
                        // System.out.println(dateTime);
                        /*  Esempio di email 12345|2019-05-10T17:07:24.764|mario@ciao|silvestro@prog.com$mario@ciao$|adsfdasfasdfs|dsafjdasfjkafjsdabajsdf*/
                        emailList.add(new Email(Integer.parseInt(output[0]), dateTime, output[2], destinatariStrings, output[4], output[5]));

                    }
                }
                //System.out.println("Email lista e \n "+ emailList.toString());
                // Ora devo comparare le due liste e scoprire se ci sono nuove email
                thirdList.addAll(emailList);// le email appena lette devono essere salvate per diventare futuro termine di paragone
                //   emailList.removeAll(compareEmails); // se ci sono nuove email rimarranno qui.
                /*   for (Email email : compareEmails) {
                    
                    for (Email e : emailList) {
                        if( e.equals(email) ){
                            iterEmails.add(e);
                            
                        }
                        
                    }
                 }
                emailList.removeAll(iterEmails); */
                for (int i = 0; i < compareEmails.size(); i++) {
                    for (int k = 0; k < emailList.size(); k++) {
                        if (compareEmails.get(i).equals(emailList.get(k))) {
                            emailList.remove(emailList.get(k));
                        }

                    }

                }
                iterEmails.clear();
                for (Email e : emailList) {
                    if (e.getMittente().equals(userString) == false) { // se il mittente non è user allora è una nuova email 
                        newEmail = true;
                        setChanged();
                        notifyObservers(); // notifico gli osservatori
                        final Group group = new Group();
                        Task<Void> task = new Task<Void>() {
                            @Override
                            protected Void call() throws Exception {

                                Platform.runLater(new Runnable() {
                                    @Override
                                    public void run() {
                                        Alert alert = new Alert(AlertType.INFORMATION);
                                        alert.setTitle("New email!");

                                        alert.setContentText("You have a new message!");

                                        alert.showAndWait();

                                    }
                                });

                                return null;
                            }
                        };
                        Thread k = new Thread(task);
                        k.setDaemon(true);
                        k.start();

                        // Thread.sleep(100);
                        // newEmail=false;
                    }
                    // newEmail=false;

                }
                records.clear();
                emailList.clear();
                compareEmails.clear();
                compareEmails.addAll(thirdList);
                thirdList.clear();
            } catch (InterruptedException ex) {
                Logger.getLogger(Updater.class.getName()).log(Level.SEVERE, null, ex);
            }

        }

    }

    /**
     * Ritona booleano che segna nuova email
     *
     * @return
     */
    public boolean getIfNewEmail() {
        return newEmail;
    }

    /**
     * Metodo di supporto per leggere il file
     *
     * @param file
     * @return
     */
    private List<String> readFile(String file) {
        List<String> records = new ArrayList<String>();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line;
            while ((line = reader.readLine()) != null) {
                // System.out.println(line);
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

class Notification extends Observable implements Observer {

    private Updater up;

    public Notification(Updater updater) {
        up = updater;
    }

    @Override
    public void update(Observable ob, Object x) {
        if (up.getIfNewEmail() == true) {
            System.out.println("Hai una nuova Email!!");
            setChanged();
            notifyObservers();
            /* if (!SwingUtilities.isEventDispatchThread()) {
           SwingUtilities.invokeLater(()-> {
              AlertEmail();
          }); 
          } */

        }

    }

    public boolean getIfNewEmail() {
        return up.getIfNewEmail();
    }

    public void AlertEmail() {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("New email!");

        alert.setContentText("You have a new message!");

        alert.showAndWait();
    }

}
