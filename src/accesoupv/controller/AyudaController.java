/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package accesoupv.controller;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
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
    private TitledPane paneVpnUPV;
    @FXML
    private TitledPane paneDSIC;
    @FXML
    private WebView VpnUPVWeb;
    @FXML
    private Hyperlink VpnUPVLink;
    @FXML
    private TitledPane paneVpnDSIC;
    @FXML
    private Hyperlink VpnDSICLink;
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
    @FXML
    private Button buttonClose;
    
    //Constants
    public static final String VPN_UPV_WEB = "https://www.upv.es/contenidos/INFOACCESO/infoweb/infoacceso/dat/697481normalc.html";
    public static final String VPN_DSIC_WEB = "http://www.dsic.upv.es/docs/infraestructura/portal-ng/manual_portal2_usuario_v8.pdf";
    public static final String EVIR_ERROR_MESSAGE = "Hubo un error al tratar de abrir el Escritorio Remoto.";
    public static final String VPN_DSIC_ERROR_MESSAGE = "Hubo un error al tratar de abrir la página web.";
    public static final String EVIR_LINUX = "linuxdesktop.dsic.upv.es";
    public static final String EVIR_WINDOWS = "windesktop.dsic.upv.es";
    //Private instances
    private Stage primaryStage;
    private final Clipboard clipboard = Clipboard.getSystemClipboard();
    
    public void init(Stage stage, String page) {
        primaryStage = stage;
        primaryStage.setTitle("Ayuda");
        switch(page) {
            case "VPN-upv": Platform.runLater(() -> paneVpnUPV.setExpanded(true)); break;
            case "VPN-dsic": Platform.runLater(() -> paneVpnDSIC.setExpanded(true)); break;
            case "DSIC": Platform.runLater(() -> paneDSIC.setExpanded(true)); break;
        }
    }
    
    private void addToClipboard(String text) {
        ClipboardContent content = new ClipboardContent();
        content.putString(text);
        clipboard.setContent(content);
    }
    
    /**
     * Initializes the controller class.
     * @param url
     * @param rb
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        VpnUPVWeb.getEngine().load(VPN_UPV_WEB);
        VpnUPVLink.setOnAction(e -> VpnUPVWeb.getEngine().load(VPN_UPV_WEB));
        VpnDSICLink.setOnAction(e -> {
            try {
                Desktop.getDesktop().browse(new URI(VPN_DSIC_WEB));
            } catch (URISyntaxException | IOException ex) {
                new Alert(Alert.AlertType.ERROR, VPN_DSIC_ERROR_MESSAGE).show();
            }
        });
        evirLink.setOnAction(e -> {
            try { Runtime.getRuntime().exec("mstsc");
            } catch (IOException ex) { new Alert(Alert.AlertType.ERROR, EVIR_ERROR_MESSAGE).show(); }
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
        buttonClose.setOnAction((evt) -> primaryStage.hide());
        paneVpnUPV.expandedProperty().addListener((obs, oldValue, newValue) -> primaryStage.setMaximized(newValue));
    }
}
