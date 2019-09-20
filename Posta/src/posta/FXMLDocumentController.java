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
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.Initializable;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.fxml.FXML;
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
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.Group;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.BorderPane;

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
    //qui vengono salvate le email selezionate
    private ObservableList<Email> selectedEmails = FXCollections.observableArrayList();
    private String user;
    ObjectOutputStream outStream;
    ObjectInputStream inStream;

    private String filename = "Database.txt";
    private String filePath;
    private Notification notify;

    // Imposta l'utente 
    public void setUser(String userString) {
        System.out.println("Vince SETUSER");
        try {
            user = userString;
            filename = user + ".txt";
            filePath = "C:\\Users\\aldob\\Desktop\\Posta\\" + filename;
            Thread.currentThread().setName(user); // imposto il nome del thread col nome utente
            UpdateEmailGUI();
             receiveEmails();
        } catch (Exception ex) {
            Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    @FXML
    @Override
    public void initialize(URL ur, ResourceBundle rb) {
        System.out.println("Vince INITUIALIZE");
        scriviButton = new Button();
        eliminaButton = new Button();

        // list_view.getItems().addAll(emailList);
        // emailList.sort((Email o1, Email o2) -> (o1.getDate().isBefore(o2.getDate())) ? 1 : 0);// basta invertire 0 e 1 per cambiare l'ordine, così mostra prima quelle arrivate/create più recentemente
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

        // posso selezionare più item con ctrl + click
        list_view.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        // gestisco doppio click su email per visualizzarla 
        list_view.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                if (mouseEvent.getButton().equals(MouseButton.PRIMARY)) {
                    if (mouseEvent.getClickCount() == 2) {
                        visualizeEmail();
                    }
                }
            }
        });

    }

    /**
     * Avvia Thread di ricezione email, crea runnable con classe receiver ed
     * esegue metodo readFromDatabase alla ricezione per aggiornare la view
     *
     * @param s
     */
    public void receiveEmails() throws InterruptedException {

        Updater up = new Updater(filePath, user, emailList);
        notify = new Notification(up);
        up.addObserver(notify);
        up.addObserver(this);
        Thread t = new Thread(up);
        t.setDaemon(true);
        /*  
         if you want the background threads to simply terminate after all the stages are closed, then you must set daemon to true.
         https://docs.oracle.com/javafx/2/api/javafx/concurrent/Task.html  */
        t.start();

    }

    // Carica le email presenti nel file in emailList Chiamato solo all'avvio del programma
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
                    LocalDateTime dateTime = LocalDateTime.parse(output[1], formatter);
                    emailList.add(new Email(Integer.parseInt(output[0]), dateTime, output[2], destinatariStrings, output[4], output[5]));
                    UpdateEmailGUI();
                }
            }
            // receiveEmails();

        } catch (Exception e) {
            System.err.println("Wrong username in login! ");
        }

    }

    private void saveEmails() {
        try {
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(filePath));

            emailList.forEach((e) -> {
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
                    Socket s = new Socket(nomeHost, 8189);
                    outStream = new ObjectOutputStream(s.getOutputStream());// apro stream output
                    InputStream simpleinInputStream = s.getInputStream();
                    inStream = new ObjectInputStream(simpleinInputStream);// apro stream input
                    String destinatariRawString = destField.getText();
                    String[] arrayDestinatariString = destinatariRawString.split(";");
                    double randomDouble = Math.random();
                    randomDouble = randomDouble * 50000000 + 1;
                    int idEmail = (int) randomDouble;
                    //limitarsi a creare l'oggetto email, ogni problema legato alla formattazione è delegato al metodo emailString di email
                    Email toSendEmail = new Email(idEmail, LocalDateTime.now(), user, arrayDestinatariString, oggettoField.getText(), emailTextArea.getText());
                    emailList.add(toSendEmail);

                    try {
                        outStream.writeObject(toSendEmail);
                        s.close();
                    } catch (IOException ex) {
                        Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
                    }

                    window.close(); // chiudo la finestra

                    list_view.getItems().clear(); // cancella tutto il contenuto della list view
                    list_view.getItems().addAll(emailList);// ripopola la list view con le email più la nuova appena mandata
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

        boolean r = emailList.removeAll(selectedEmails); //  locale al client
        String[] delete_emails = new String[20];
        delete_emails[0] = user;
        int i = 1;
        for (Email mail : selectedEmails) {
            delete_emails[i] = Integer.toString(mail.getId());
            i++;
        }

        try {
            String nomeHost = InetAddress.getLocalHost().getHostName();
            Socket s = new Socket(nomeHost, 8189);
            outStream = new ObjectOutputStream(s.getOutputStream());// apro stream output

            outStream.writeObject(delete_emails);
            list_view.getItems().clear(); // cancella tutto il contenuto della list view
            list_view.getItems().addAll(emailList);// ripopola la list view con le email aggiornate
            // UpdateEmailGUI();
        } catch (IOException ex) {
            Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
        }

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
        //leggiamo il database relativo all'utente per caricare le email salvate
        try {
            List<String> records;
            records = readFile(filePath);
            for (String s : records) {
                String[] output = s.split("\\|");

                String[] destinatariStrings = output[3].split(";"); // i destinatari sono separati da un $ 

                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS");
                LocalDateTime dateTime = LocalDateTime.parse(output[1], formatter);
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
                    Socket s = new Socket(nomeHost, 8189);
                    outStream = new ObjectOutputStream(s.getOutputStream());// apro stream output
                    //InputStream simpleinInputStream = s.getInputStream();
                    inStream = new ObjectInputStream(s.getInputStream());// apro stream input
                    String destinatariRawString = destField.getText();
                    String[] arrayDestinatariString = destinatariRawString.split(";");

                    //limitarsi a creare l'oggetto email, ogni problema legato alla formattazione è delegato al metodo emailString di email
                    double randomDouble = Math.random();
                    randomDouble = randomDouble * 50000000 + 1;
                    int idEmail = (int) randomDouble;
                    Email toSendEmail = new Email(idEmail, LocalDateTime.now(), user, arrayDestinatariString, oggettoField.getText(), emailTextArea.getText());
                    System.out.println(toSendEmail.toString());
                    emailList.add(toSendEmail);

                    try {
                        outStream.writeObject(toSendEmail);

                    } catch (IOException ex) {
                        Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    //outStream.close();
                    // inStream.close();
                    window.close(); // chiudo la finestra

                    // list_view.getItems().clear(); // cancella tutto il contenuto della list view
                    // list_view.getItems().addAll(emailList);// ripopola la list view con le email più la nuova appena mandata
                    s.close();
                    UpdateEmailGUI();
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
                    Socket s = new Socket(nomeHost, 8189);

                    outStream = new ObjectOutputStream(s.getOutputStream());// apro stream output
                    InputStream simpleinInputStream = s.getInputStream();
                    inStream = new ObjectInputStream(simpleinInputStream);// apro stream input

                    String destinatariRawString = destField.getText();
                    String[] arrayDestinatariString = destinatariRawString.split(";");

                    //limitarsi a creare l'oggetto email, ogni problema legato alla formattazione è delegato al metodo emailString di email
                    double randomDouble = Math.random();
                    randomDouble = randomDouble * 50000000 + 1;
                    int idEmail = (int) randomDouble;
                    Email toSendEmail = new Email(idEmail, LocalDateTime.now(), user, arrayDestinatariString, oggettoField.getText(), emailTextArea.getText());
                    emailList.add(toSendEmail);

                    try {
                        outStream.writeObject(toSendEmail);

                    } catch (IOException ex) {
                        Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    s.close();
                    window.close(); // chiudo la finestra

                    list_view.getItems().clear(); // cancella tutto il contenuto della list view
                    list_view.getItems().addAll(emailList);// ripopola la list view con le email più la nuova appena mandata
                } catch (Exception ex) {
                    Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
                }
            });

        } catch (Exception e) {
            System.out.println(e.getCause() + e.toString());

        }

    }

    public void UpdateEmailGUI() throws IOException {
        try {
            String nomeHost = InetAddress.getLocalHost().getHostName();
            Socket s = new Socket(nomeHost, 8189);
            outStream = new ObjectOutputStream(s.getOutputStream());// apro stream output
            System.out.println(user);
            outStream.writeObject(user);

            InputStream simpleinInputStream = s.getInputStream();
            inStream = new ObjectInputStream(simpleinInputStream);// apro stream input
            ArrayList<Email> emails = new ArrayList<>();
            emails = (ArrayList<Email>) inStream.readObject();
            inStream.close();
            simpleinInputStream.close();
            s.close();
            emailList.clear();
            emailList.addAll(emails);
            list_view.getItems().clear(); // cancella tutto il contenuto della list view
            list_view.getItems().addAll(emails);// ripopola la list view con le email aggiornate

        } catch (Exception e) {
            System.out.println(e.getCause());
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
            boolean keepAlive = true;
            while (keepAlive) {

                try {
                    //Thread.sleep(100);
                    ArrayList<Email> emails = new ArrayList<>();
                    emails = (ArrayList<Email>) inStream.readObject();

                } catch (Exception ex) {
                    keepAlive = false;
                    inStream.close();
                    Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);

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
    private  ObjectOutputStream outStream;
    private ObjectInputStream inStream;

    public Updater(String path, String user, List<Email> compEmails) {
        pathFile = path;
        userString = user;
        compareEmails.addAll(compEmails);

    }

    @Override
    public void run() {
        List<String> records;
        while (true) {
            try {
                newEmail = false;

                Thread.currentThread().setName("Thread Updater");
                Thread.sleep(5000); // Ogni 5 secondi scansiona il file e carica le email. se ne trova una nuova in cui mittente!= utente fa update a osberver

                /* records = readFile(pathFile);
                for (String s : records) {
                    if (!"\n".equals(s)) {
                        String[] output = s.split("\\|");

                        String[] destinatariStrings = output[3].split(";"); // i destinatari sono separati da un $

                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS");
                        LocalDateTime dateTime = LocalDateTime.parse(output[1], formatter);
                        emailList.add(new Email(Integer.parseInt(output[0]), dateTime, output[2], destinatariStrings, output[4], output[5]));

                    }
                } */
                try {
                    String nomeHost = InetAddress.getLocalHost().getHostName();
                    Socket s = new Socket(nomeHost, 8189);
                    outStream = new ObjectOutputStream(s.getOutputStream());// apro stream output
                  
                    outStream.writeObject(userString);

                    InputStream simpleinInputStream = s.getInputStream();
                    inStream = new ObjectInputStream(simpleinInputStream);// apro stream input
                    ArrayList<Email> emails = new ArrayList<>();
                    emails = (ArrayList<Email>) inStream.readObject();
                    inStream.close();
                    simpleinInputStream.close();
                    s.close();
                    emailList.clear();
                    emailList.addAll(emails);
                  

                } catch (Exception e) {
                    System.out.println(e.getCause());
                }
                // Ora devo comparare le due liste e scoprire se ci sono nuove email
                thirdList.addAll(emailList);// le email appena lette devono essere salvate per diventare futuro termine di paragone

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
                    }

                }
              
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
            setChanged();
            notifyObservers();
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
