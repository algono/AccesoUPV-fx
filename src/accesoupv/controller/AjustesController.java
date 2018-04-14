/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package accesoupv.controller;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * FXML Controller class
 *
 * @author Alejandro
 */
public class AjustesController implements Initializable {
    
    @FXML
    private Button buttonAccept;
    @FXML
    private Button buttonHelp;
    @FXML
    private TextField textVPN;
    @FXML
    private ComboBox<String> comboDrive;
    @FXML
    private TextField textUser;
    
    //Constants (Messages)
    public static final String SUCCESS_MESSAGE = "El archivo ha sido creado con éxito.\n¿Desea abrir la carpeta en la cual ha sido guardado?";
    public static final String ERROR_MESSAGE = "Ha habido un error al crear el programa. Vuelva a intentarlo.";
    public static final String FOLDER_ERROR_MESSAGE = "Ha habido un error al abrir la carpeta. Ábrala manualmente.";
        
    private Map<String,String> createMap() {
            Map<String,String> map = new HashMap<>();
            map.put("drive", comboDrive.getValue());
            map.put("vpn", textVPN.getText());
            String user = textUser.getText();
            map.put("letter", user.charAt(0)+ "");
            map.put("user", user);
            return map;
    }
    
    private ObservableList<String> createDriveList() {
        ArrayList<String> drives = new ArrayList<>();
        char letter = 'Z';
        while (letter >= 'A') {
            String drive = letter + ":";
            if (!new File(drive).exists()) drives.add(drive);
            letter--;
        }
        return FXCollections.observableList(drives);
    }
    
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        ObservableList<String> drives = createDriveList();
        comboDrive.setItems(drives);
        if (drives.contains("W:")) comboDrive.getSelectionModel().select("W:"); 
        else comboDrive.getSelectionModel().selectFirst();
        buttonAccept.disableProperty().bind(
            Bindings.or(
                    Bindings.isEmpty(textUser.textProperty()),
                    Bindings.isEmpty(textVPN.textProperty())
            )
        );
        
        buttonHelp.setOnAction((e) -> {
            try {
                Stage stage = new Stage();
                FXMLLoader myLoader = new FXMLLoader(getClass().getResource("/customcmdcreator/view/AyudaView.fxml"));
                Parent root = (Parent) myLoader.load();
                AyudaController dialogue = myLoader.<AyudaController>getController();
                dialogue.init(stage);
                Scene scene = new Scene(root);

                stage.setScene(scene);
                stage.initModality(Modality.APPLICATION_MODAL);
                stage.show();
            } catch (IOException ex) {
            }
        });
        Platform.runLater(() -> buttonHelp.requestFocus());
    }    
}
