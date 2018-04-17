/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package accesoupv.controller;

import accesoupv.AccesoUPV;
import accesoupv.model.CodeFiller;
import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 *
 * @author Alejandro
 */
public class PrincipalController implements Initializable {
    
    @FXML
    private Label labelState;
    @FXML
    private VBox loadingScreen;
    @FXML
    private MenuItem menuAccessW;
    @FXML
    private MenuItem menuAccessDSIC;
    @FXML
    private MenuItem menuAjustes;
    @FXML
    private MenuItem menuAyuda;
    @FXML
    private MenuItem menuAyudaVPN;
    @FXML
    private MenuItem menuAyudaDSIC;
    @FXML
    private MenuItem menuDisconnectW;
    @FXML
    private MenuItem menuExit;
    @FXML
    private Button buttonAccessW;
    @FXML
    private Button buttonAyudaDSIC;
    @FXML
    private Button buttonDisconnectW;
    @FXML
    private Button buttonAccessDSIC;
    
    //Variables
    public static String drive, vpn, user;
    //Messages
    public static final String SUCCESS_MESSAGE = "El archivo ha sido creado con éxito.\n¿Desea abrir la carpeta en la cual ha sido guardado?";
    public static final String ERROR_MESSAGE = "Ha habido un error al crear el programa. Vuelva a intentarlo.";
    public static final String FOLDER_ERROR_MESSAGE = "Ha habido un error al abrir la carpeta. Ábrala manualmente.";
    
    private void loadPrefs() {
        Preferences prefs = Preferences.userNodeForPackage(AccesoUPV.class);
        drive = prefs.get("drive", "");
        vpn = prefs.get("vpn", "");
        user = prefs.get("user", "");
        if (drive.isEmpty() || vpn.isEmpty() || user.isEmpty()) { gotoAjustes(true); }
    }
    
    private Map<String,String> createMap() {
            Map<String,String> map = new HashMap<>();
            map.put("drive", drive);
            map.put("vpn", vpn);
            map.put("letter", user.charAt(0)+ "");
            map.put("user", user);
            return map;
    }
    
    @FXML
    private void createCMD(ActionEvent event) {
        try {
            InputStream input = getClass().getResourceAsStream("/accesoupv/resources/code.txt");
            CodeFiller filler = new CodeFiller(input, createMap());
            Node mynode = (Node) event.getSource();
            File out = filler.chooseOutput(mynode.getScene().getWindow(), "Programa.cmd", new FileChooser.ExtensionFilter("Archivo de comandos(*.cmd)", "*.cmd"));
            filler.run();
            //Si todo ha ido bien, muestra un mensaje de éxito, y da al usuario la opción de acceder a la carpeta donde se encuentra el output
            Alert success = new Alert(Alert.AlertType.CONFIRMATION, SUCCESS_MESSAGE);
            success.setHeaderText(null);
            Optional<ButtonType> result = success.showAndWait();
            if (result.get() == ButtonType.OK) {
                try {
                    Desktop.getDesktop().open(out.getParentFile());
                } catch (IOException ex) {
                    new Alert(Alert.AlertType.ERROR, FOLDER_ERROR_MESSAGE).show();
                }
            }
            Platform.exit();
        } catch (IOException ex) {
            new Alert(Alert.AlertType.ERROR, ERROR_MESSAGE).show();
        }
    }
    
    private void gotoAjustes(boolean noPrefs) {
        try {    
            Stage stage = new Stage();
            FXMLLoader myLoader = new FXMLLoader(getClass().getResource("/accesoupv/view/AjustesView.fxml"));
            Parent root = (Parent) myLoader.load();
            AjustesController dialogue = myLoader.<AjustesController>getController();
            dialogue.init(stage, noPrefs);
            Scene scene = new Scene(root);

            stage.setScene(scene);
            stage.initModality(Modality.APPLICATION_MODAL);
            if (noPrefs) stage.showAndWait();
            else stage.show();
        } catch (IOException ex) {}
    }
    
    private void gotoAyuda(String page) {
        try {
            Stage stage = new Stage();
            FXMLLoader myLoader = new FXMLLoader(getClass().getResource("/accesoupv/view/AyudaView.fxml"));
            Parent root = (Parent) myLoader.load();
            AyudaController dialogue = myLoader.<AyudaController>getController();
            dialogue.init(stage, page);
            Scene scene = new Scene(root);

            stage.setScene(scene);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.show();
        } catch (IOException ex) {
        }
    }
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        loadPrefs();
        menuAyuda.setOnAction((e) -> gotoAyuda(""));
        menuAyudaDSIC.setOnAction((e) -> gotoAyuda("DSIC"));
        buttonAyudaDSIC.setOnAction((e) -> gotoAyuda("DSIC"));
        menuAyudaVPN.setOnAction((e) -> gotoAyuda("VPN"));
        menuAjustes.setOnAction((e) -> gotoAjustes(false));
    }    
    
}
