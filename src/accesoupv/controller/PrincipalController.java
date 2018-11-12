/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package accesoupv.controller;

import accesoupv.model.AccesoUPV;
import static accesoupv.model.services.AccesoVpnDSICService.PORTAL_DSIC_WEB;
import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 *
 * @author Alejandro
 */
public class PrincipalController implements Initializable {
    
    @FXML
    private MenuBar menuBar;
    @FXML
    private MenuItem menuAccessW;
    @FXML
    private MenuItem menuDisconnectW;
    @FXML
    private MenuItem menuAccessDSIC;
    @FXML
    private MenuItem menuDisconnectDSIC;
    @FXML
    private MenuItem menuLinuxDSIC;
    @FXML
    private MenuItem menuWinDSIC;
    @FXML
    private MenuItem menuAjustes;
    @FXML
    private MenuItem menuPortalDSIC;
    @FXML
    private MenuItem menuDisconnectVpnDSIC;
    @FXML
    private Button buttonAccessW;
    @FXML
    private Button buttonDisconnectW;
    @FXML
    private Button buttonAccessDSIC;
    @FXML
    private Button buttonDisconnectDSIC;
    @FXML
    private Button buttonLinuxDSIC;
    @FXML
    private Button buttonWinDSIC;
    @FXML
    private Button buttonPortalDSIC;
    @FXML
    private Button buttonDisconnectVpnDSIC;
    
    //Messages
    public static final String ERROR_DSIC_MSG = "No se ha podido acceder al escritorio virtual del DSIC.";
    public static final String ERROR_PORTAL_DSIC_MSG = "Ha habido un error al tratar de acceder a la web del Portal.";
    public static final String EVIR_VPN_DSIC_WARNING = 
            "El acceso al Portal DSIC no puede estar conectado mientras se accede a un Escritorio Remoto del DSIC.\n\n"
            + "Si continúa, este será desconectado automáticamente.";
    //AccesoUPV Instance
    private static final AccesoUPV acceso = AccesoUPV.getInstance();
    
    private void showAjustes() {
        try {
            Stage stage = new Stage();
            FXMLLoader myLoader = new FXMLLoader(getClass().getResource("/accesoupv/view/AjustesView.fxml"));
            Parent root = (Parent) myLoader.load();
            stage.setTitle("Preferencias");
            Scene scene = new Scene(root);

            stage.setScene(scene);
            stage.getIcons().add(new Image(getClass().getResourceAsStream("/accesoupv/resources/icons/preferences-icon.png")));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
        } catch (IOException ex) {}
    }
    
    private void accessEVIR(String server) {
        if (acceso.isVpnDSICConnected()) { //Si la VPN del DSIC está conectada muestra un aviso al usuario, pues no funcionaría.
            Alert conf = new Alert(Alert.AlertType.WARNING, EVIR_VPN_DSIC_WARNING, ButtonType.OK, ButtonType.CANCEL);
            conf.setHeaderText(null);
            Optional<ButtonType> confRes = conf.showAndWait();
            if (confRes.isPresent() && confRes.get() == ButtonType.OK) { //Si aceptó trata de desconectar la VPN del DSIC.
                acceso.disconnectVpnDSIC();
            } else return; //Si decidió cancelar se termina el proceso de acceder al EVIR.
        }
        //Si tras todo lo anterior se desconectó correctamente, continúa con el proceso.
        if (!acceso.isVpnDSICConnected() && acceso.connectVpnUPV()) {
            try {
                new ProcessBuilder("cmd.exe", "/c", "mstsc", "/v:" + server).start();
            } catch (IOException ex) {
                new Alert(Alert.AlertType.ERROR, ERROR_DSIC_MSG).show();
            }
        }
    }
    
    private void accessPortal() {
        if (acceso.connectVpnDSIC()) {
            try {
                Desktop.getDesktop().browse(new URI("https://" + PORTAL_DSIC_WEB));
            } catch (URISyntaxException | IOException ex) {
                Alert a = new Alert(Alert.AlertType.ERROR, ERROR_PORTAL_DSIC_MSG);
                Hyperlink link = new Hyperlink(PORTAL_DSIC_WEB);
                link.setOnAction((evt) -> AyudaController.addToClipboard(PORTAL_DSIC_WEB));
                HBox box = new HBox(new Label("Link (click para copiar al portapapeles): "), link);
                a.getDialogPane().getChildren().add(box);
                a.getDialogPane().setExpandableContent(new TextArea(ex.getMessage()));
                a.show();
            }
        }
    }
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        //Assigns actions to buttons and menu items
        menuLinuxDSIC.setOnAction(e -> accessEVIR(AccesoUPV.LINUX_DSIC));
        buttonLinuxDSIC.setOnAction(e -> accessEVIR(AccesoUPV.LINUX_DSIC));
        
        menuWinDSIC.setOnAction(e -> accessEVIR(AccesoUPV.WIN_DSIC));
        buttonWinDSIC.setOnAction(e -> accessEVIR(AccesoUPV.WIN_DSIC));
        
        //Añade un menú de ayuda a la barra de menú
        menuBar.getMenus().add(AyudaController.getMenu());
        
        menuAjustes.setOnAction(e -> showAjustes());
        
        buttonAccessW.setOnAction(e -> acceso.connectW());
        menuAccessW.setOnAction(e -> acceso.connectW());
        
        buttonAccessDSIC.setOnAction(e -> acceso.connectDSIC());
        menuAccessDSIC.setOnAction(e -> acceso.connectDSIC());
        
        buttonDisconnectW.setOnAction(e -> acceso.disconnectW());
        menuDisconnectW.setOnAction(e -> acceso.disconnectW());
        
        buttonDisconnectDSIC.setOnAction(e -> acceso.disconnectDSIC());
        menuDisconnectDSIC.setOnAction(e -> acceso.disconnectDSIC());
        
        buttonPortalDSIC.setOnAction(e -> accessPortal());
        menuPortalDSIC.setOnAction(e -> accessPortal());
        
        buttonDisconnectVpnDSIC.setOnAction(e -> acceso.disconnectVpnDSIC());
        menuDisconnectVpnDSIC.setOnAction(e -> acceso.disconnectVpnDSIC());
        
        //Setting bindings for buttons
        buttonDisconnectW.disableProperty().bind(acceso.connectedWProperty().not());
        menuDisconnectW.disableProperty().bind(acceso.connectedWProperty().not());
        buttonDisconnectDSIC.disableProperty().bind(acceso.connectedDSICProperty().not());
        menuDisconnectDSIC.disableProperty().bind(acceso.connectedDSICProperty().not());
        buttonDisconnectVpnDSIC.disableProperty().bind(acceso.connectedVpnDSICProperty().not());
        menuDisconnectVpnDSIC.disableProperty().bind(acceso.connectedVpnDSICProperty().not());
        
        Platform.setImplicitExit(false); //Se asegura de que el programa no se cierre solo
        acceso.init(); //Comprueba si está conectado a la UPV, y si no, trata de conectarse a la VPN
        Platform.runLater(() -> Platform.setImplicitExit(true));
        
    }
}
