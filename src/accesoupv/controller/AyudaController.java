/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package accesoupv.controller;

import accesoupv.model.ProcessUtils;
import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Accordion;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TitledPane;
import javafx.scene.image.Image;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.web.WebView;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * FXML Controller class
 *
 * @author Alejandro
 */
public class AyudaController implements Initializable {
    
    @FXML
    private Accordion index;
    @FXML
    private TitledPane VpnUPVPane;
    @FXML
    private WebView VpnUPVWeb;
    @FXML
    private Hyperlink VpnUPVLink;
    @FXML
    private Hyperlink VpnDSICLink;
    @FXML
    private Hyperlink evirLink;
    @FXML
    private Hyperlink clipLinux;
    @FXML
    private Hyperlink clipWindows;
    @FXML
    private Hyperlink devicesLink;
    @FXML
    private Label copiedLinux;
    @FXML
    private Label copiedWindows;
    @FXML
    private Button buttonClose;
    
    //Constants
    public static final String VPN_UPV_WEB = "https://www.upv.es/contenidos/INFOACCESO/infoweb/infoacceso/dat/697481normalc.html";
    public static final String VPN_DSIC_WEB = "http://www.dsic.upv.es/docs/infraestructura/portal-ng/manual_portal2_usuario_v8.pdf";
    public static final String EVIR_ERROR_MESSAGE = 
            "Hubo un error al tratar de abrir el Escritorio Remoto.\n"
            + "Ábralo manualmente (Buscar - Escritorio Remoto)";
    public static final String DEVICES_ERROR_MESSAGE = 
            "Hubo un error al tratar de abrir el Administrador de dispositivos.\n" 
            + "Ábralo manualmente (Buscar - Administrador de dispositivos)";
    public static final String VPN_DSIC_ERROR_MESSAGE = "Hubo un error al tratar de abrir la página web.";
    public static final String EVIR_LINUX = "linuxdesktop.dsic.upv.es";
    public static final String EVIR_WINDOWS = "windesktop.dsic.upv.es";
    
    private Stage primaryStage;
    
    public Stage getStage() { return primaryStage; }
    public void initStage(Stage stage) {
        primaryStage = stage;
        primaryStage.setTitle("Ayuda");
    }
    
    public static final AyudaController getInstance() {
        AyudaController dialogue = null;
        try {
            Stage stage = new Stage();
            FXMLLoader myLoader = new FXMLLoader(AyudaController.class.getResource("/accesoupv/view/AyudaView.fxml"));
            Parent root = (Parent) myLoader.load();
            dialogue = myLoader.<AyudaController>getController();
            dialogue.initStage(stage);
            Scene scene = new Scene(root);

            stage.setScene(scene);
            stage.getIcons().add(new Image(AyudaController.class.getResourceAsStream("/accesoupv/resources/icons/help-icon.png")));
            stage.initModality(Modality.APPLICATION_MODAL);
        } catch (IOException ex) {
            new Alert(Alert.AlertType.ERROR, "Hubo un error inesperado.").show();
            System.err.println("Hubo un error al tratar de crear la ventana de ayuda");
            Logger.getLogger(AyudaController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return dialogue;
    }
    
    public static Menu getMenu() {
        Menu menu = new Menu("Ayuda");
        MenuItem menuIndice = new MenuItem("Índice de ayuda");
        menuIndice.setOnAction((evt) -> getInstance().getStage().show());
        
        ObservableList<MenuItem> items = menu.getItems();
        items.addAll(menuIndice, new SeparatorMenuItem());
        
        Accordion indice = getInstance().getIndex();
        for (TitledPane t : indice.getPanes()) {
            MenuItem page = new MenuItem(t.getText());
            page.setOnAction((evt) -> {
                AyudaController instance = getInstance();
                instance.getStage().show();
                instance.setExpandedPage(t.getText());
            });
            items.add(page);
        }
        return menu;
    }
    
    public Accordion getIndex() { return index; }
    
    public void setExpandedPage(String text) {
        for (TitledPane t : index.getPanes()) {
            if (t.getText().equals(text)) {
                t.setExpanded(true); break;
            }
        }
    }
    
    public static final void addToClipboard(String text) {
        ClipboardContent content = new ClipboardContent();
        content.putString(text);
        Clipboard.getSystemClipboard().setContent(content);
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
        VpnUPVPane.expandedProperty().addListener((obs, oldValue, newValue) -> primaryStage.setMaximized(newValue));
        VpnDSICLink.setOnAction(e -> {
            try {
                Desktop.getDesktop().browse(new URI(VPN_DSIC_WEB));
            } catch (URISyntaxException | IOException ex) {
                new Alert(Alert.AlertType.ERROR, VPN_DSIC_ERROR_MESSAGE).show();
            }
        });
        evirLink.setOnAction(e -> {
            try { Runtime.getRuntime().exec("mstsc");
            } catch (IOException ex) { 
                Alert a = new Alert(Alert.AlertType.ERROR, EVIR_ERROR_MESSAGE);
                a.setHeaderText(null); a.show(); 
            }
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
        devicesLink.setOnAction(e -> {
            try { ProcessUtils.startProcess("cmd", "/c", "devmgmt.msc");
            } catch (IOException ex) {
                Alert a = new Alert(Alert.AlertType.ERROR, DEVICES_ERROR_MESSAGE);
                a.setHeaderText(null); a.show(); 
            }
        });
        buttonClose.setOnAction((evt) -> primaryStage.hide());
    }
}
