/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package accesoupv.controller;

import accesoupv.model.MyAlert;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.TitledPane;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

/**
 * FXML Controller class
 *
 * @author Alejandro
 */
public class AyudaController implements Initializable {
    
    @FXML
    private TitledPane paneVPN;
    @FXML
    private TitledPane paneDSIC;
    @FXML
    private WebView vpnWeb;
    @FXML
    private Hyperlink vpnLink;
    @FXML
    private Hyperlink evirLink;
    @FXML
    private Hyperlink clipLinux;
    @FXML
    private Hyperlink clipWindows;
    @FXML
    private Label copiedLinux;
    @FXML
    private Label copiedWindows;
    
    //Constants
    public static final String VPN_WEB = "https://www.upv.es/contenidos/INFOACCESO/infoweb/infoacceso/dat/697481normalc.html";
    public static final String EVIR_ERROR_MESSAGE = "Hubo un error al tratar de abrir el Escritorio Remoto.";
    public static final String EVIR_LINUX = "linuxdesktop.dsic.upv.es";
    public static final String EVIR_WINDOWS = "windesktop.dsic.upv.es";
    //Private instances
    private Stage primaryStage;
    private final Clipboard clipboard = Clipboard.getSystemClipboard();
    
    public void init(Stage stage, String page) {
        primaryStage = stage;
        primaryStage.setTitle("Ayuda");
        switch(page) {
            case "VPN": Platform.runLater(() -> paneVPN.setExpanded(true)); break;
            case "DSIC": Platform.runLater(() -> paneDSIC.setExpanded(true)); break;
        }
    }
    
    private void addToClipboard(String text) {
        ClipboardContent content = new ClipboardContent();
        content.putString(text);
        clipboard.setContent(content);
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
        vpnWeb.getEngine().load(VPN_WEB);
        vpnLink.setOnAction(e -> vpnWeb.getEngine().load(VPN_WEB));
        evirLink.setOnAction(e -> {
            try { Runtime.getRuntime().exec("mstsc");
            } catch (IOException ex) { new MyAlert(Alert.AlertType.ERROR, EVIR_ERROR_MESSAGE).show(); }
        });
        clipLinux.setOnAction((e) -> { 
            addToClipboard(EVIR_LINUX); 
            copiedWindows.setVisible(false);
            copiedLinux.setVisible(true);
        });
        clipWindows.setOnAction((e) -> { 
            addToClipboard(EVIR_WINDOWS); 
            copiedLinux.setVisible(false);
            copiedWindows.setVisible(true);
        });
        paneVPN.expandedProperty().addListener((obs, oldValue, newValue) -> primaryStage.setMaximized(newValue));
    }
}
