/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package posta;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Date;
import javafx.beans.property.SimpleStringProperty;

/**
 * La classe Email specifica ID, mittente, destinatario, argomento, testo e data
 * di spedizione del messaggio.
 *
 * @author silve
 */
public class Email implements  Serializable{

    private static final long serialVersionUID = 1L;

    private final int id;
    private final String mittente;
    private final String[] destinatario;
    private final String argomento;
    private String testo ;
    private final LocalDateTime date;

    //costruttore inzializza tutti i campi della email
    public Email(int id, LocalDateTime date, String mittente, String[] destinatario, String argomento, String testo) {
        this.id = id;
        this.mittente = mittente;
        this.destinatario = destinatario;
        this.argomento = argomento;
        this.testo = testo;
        this.date = date;

    }

    /**
     * @return the id
     */
    public int getId() {
        return id;
    }

    /**
     * @return the mittente
     */
    public String getMittente() {
        return mittente;
    }

    /**
     * @return the destinatario
     */
    public String[] getDestinatario() {
        return destinatario;
    }

    /**
     * @return the argomento
     */
    public String getArgomento() {
        return argomento;
    }

    /**
     * @return the testo
     */
    public String getTesto() {
        return testo;
    }

    public void setTesto(String s) {
        testo=s;
    }

    public String Testo() {
        return testo;
    }

    /**
     * @return the date
     */
    public LocalDateTime getDate() {
        return date;
    }

    @Override
    // Ritorna una stringa formattata contenete email in formato leggibile
    public String toString() {
        String s = "Mittente: " + getMittente() + "\n Destinatario: " + Arrays.toString(getDestinatario()) + "\nTesto: " + getTesto() + "\nData: " + getDate();
        return s;
    }
    // Ritorna stringa contenente email  con sintassi adatta per database
    public String emailString() {
        String s = Integer.toString(getId())+"|"+getDate()+"|"+ getMittente() +"|"+ Arrays.toString(getDestinatario())+"|"+getArgomento() +"|"+ getTesto() + "|";
        return s;
    }
}
