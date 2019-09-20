/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package posta;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextArea;

public class ControllerServer implements Initializable{
   @FXML
   TextArea text;
  
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        //effetuo il bind al campo text della GUI in modo da aggiornare dinamicamente il contenuto del TextArea
        text.textProperty().bind(SocketServer.logData);

    }
       
}


