/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package accesoupv.controller;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import javafx.stage.Stage;

/**
 * FXML Controller class
 *
 * @author Alejandro
 */
public class AjustesController implements Initializable {
    
    @FXML
    private Text textWarning;
    @FXML
    private Button buttonAccept;
    @FXML
    private Button buttonClose;
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
    
    private Stage primaryStage;
    private final BooleanProperty noPrefs = new SimpleBooleanProperty();
    private ObservableList<String> dataDrives;
    
    public void init(Stage stage, boolean nPrefs) {
        primaryStage = stage;
        primaryStage.setTitle("Preferencias");
        noPrefs.set(nPrefs);
        if (nPrefs) {
            primaryStage.setOnCloseRequest((we) -> Platform.exit());
            textWarning.setVisible(true);
            if (dataDrives.contains("W:")) {
                comboDrive.getSelectionModel().select("W:");
            } else {
                comboDrive.getSelectionModel().selectFirst();
            }
        } else {
            //Escribe las preferencias guardadas (si hay)
            textUser.setText(PrincipalController.user);
            comboDrive.getSelectionModel().select(PrincipalController.drive);
            textVPN.setText(PrincipalController.vpn);
        }
    }
    
    @FXML
    private void savePrefs(ActionEvent event) {
            PrincipalController.vpn = textVPN.getText();
            PrincipalController.drive = comboDrive.getValue();
            PrincipalController.user = textUser.getText();
            //Cierra la ventana de ajustes
            Node mynode = (Node) event.getSource();
            mynode.getScene().getWindow().hide();
    }
    
    private void createDriveList() {
        ArrayList<String> drives = new ArrayList<>();
        char letter = 'Z';
        while (letter >= 'A') {
            String drive = letter + ":";
            if (!new File(drive).exists()) drives.add(drive);
            letter--;
        }
        dataDrives = FXCollections.observableList(drives);
    }
    
    @FXML
    private void closeDialogue(ActionEvent event) {
        Node mynode = (Node) event.getSource();
        mynode.getScene().getWindow().hide();
    }
    
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        createDriveList();
        comboDrive.setItems(dataDrives);
        
        buttonAccept.disableProperty().bind(
            Bindings.or(
                    Bindings.isEmpty(textUser.textProperty()),
                    Bindings.isEmpty(textVPN.textProperty())
            )
        );
        
        buttonClose.disableProperty().bind(noPrefs);
        
        Platform.runLater(() -> buttonClose.requestFocus());
    }    
}
