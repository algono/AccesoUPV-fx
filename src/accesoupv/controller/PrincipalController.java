/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package accesoupv.controller;

import accesoupv.model.AccesoUPV;
import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.beans.binding.Bindings;
import javafx.event.ActionEvent;
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
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 *
 * @author Alejandro
 */
public class PrincipalController implements Initializable {
    
    @FXML
    private MenuItem menuDisconnectW;
    @FXML
    private MenuItem menuLinuxDSIC;
    @FXML
    private MenuItem menuWinDSIC;
    @FXML
    private MenuItem menuAjustes;
    @FXML
    private MenuItem menuAyuda;
    @FXML
    private MenuItem menuAyudaVPN;
    @FXML
    private MenuItem menuAyudaDSIC;
    @FXML
    private Button buttonDisconnectW;
    @FXML
    private Button buttonLinuxDSIC;
    @FXML
    private Button buttonWinDSIC;
    
    //Messages
    public static final String WARNING_FOLDER_W_MSG = "El disco ha sido conectado correctamente, pero no se ha podido abrir la carpeta.\n"
            + "Ábrala manualmente.";
    public static final String ERROR_DSIC_MSG = "No se ha podido acceder al servidor DSIC.";
    //AccesoUPV Instance
    private static final AccesoUPV acceso = AccesoUPV.getInstance();
    
    private void showAjustes() {
        if (acceso.isWConnected()) {
            String wMsg = "No se permite acceder a los ajustes mientras el disco W se encuentre conectado.\n\n"
                    + "¿Desea desconectarlo?";
            Alert warningW = new Alert(Alert.AlertType.WARNING, wMsg);
            warningW.setTitle("Disco W Conectado");
            warningW.setHeaderText(null);
            warningW.getButtonTypes().setAll(ButtonType.OK, ButtonType.CANCEL);
            Optional<ButtonType> res = warningW.showAndWait();
            if (res.isPresent() && res.get() == ButtonType.OK) {
                if (!acceso.disconnectW()) return;
            } else return;
        }
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
    
    private void showAyuda(String page) {
        try {
            Stage stage = new Stage();
            FXMLLoader myLoader = new FXMLLoader(getClass().getResource("/accesoupv/view/AyudaView.fxml"));
            Parent root = (Parent) myLoader.load();
            AyudaController dialogue = myLoader.<AyudaController>getController();
            dialogue.init(stage, page);
            Scene scene = new Scene(root);

            stage.setScene(scene);
            stage.getIcons().add(new Image(getClass().getResourceAsStream("/accesoupv/resources/icons/help-icon.png")));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.show();
        } catch (IOException ex) {
            new Alert(Alert.AlertType.ERROR, "Ha habido un error inesperado al tratar de abrir la ventana.").show();
        }
    }
    
    private void establishVPN() {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setHeaderText(null);
        Label content = new Label("Debe establecer una red VPN para poder acceder a la UPV desde fuera del campus.\n\n"
                + "¿Desea que se cree la VPN automáticamente?\n\n"
                + "Si ya tiene una creada o prefiere crearla manualmente, elija \"No\".");
        Hyperlink help = new Hyperlink("Para saber cómo crear una VPN manualmente, pulse aquí");
        help.setOnAction((evt) -> showAyuda("VPN"));
        alert.getDialogPane().setContent(new VBox(content, help));
        alert.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO,ButtonType.CANCEL);
        Optional<ButtonType> alertRes = alert.showAndWait();
        
        //Si el usuario canceló o cerró el diálogo, sale del programa
        if (!alertRes.isPresent() || alertRes.get() == ButtonType.CANCEL) System.exit(0);
        
        //Si pulsó que sí, se debe crear una VPN nueva. Si pulsó que no, se debe introducir una existente.
        boolean setNewVPN = alertRes.get() == ButtonType.YES;
        boolean hasNewVPN = acceso.setVPNDialog(setNewVPN);
        
        //Si la VPN ha sido cambiada, continúa. Si no, vuelve a la primera Alert.
        if (hasNewVPN) {
            //Si se ha elegido crear una VPN nueva, la crea.
            if (setNewVPN) acceso.createVPN();
            connectVPN();
        } else { establishVPN(); }
    }
    
    private void connectVPN() {
        if (acceso.getVPN() == null) establishVPN();
        boolean succeeded = acceso.connectVPN();
        //Si la ejecución falló, permite cambiar el valor de la VPN por si lo puso mal
        if (!succeeded) {
            boolean hasNewVPN = acceso.setVPNDialog(false);
            if (hasNewVPN) connectVPN();
            else establishVPN();
        }
    }
    
    @FXML
    private void accessW(ActionEvent evt) {
        boolean succeeded = acceso.connectW();
        if (succeeded) {
            try {
                Desktop.getDesktop().open(new File(acceso.getDrive()));
            } catch (IOException ex) {
                new Alert(Alert.AlertType.WARNING, WARNING_FOLDER_W_MSG).show();
            }
        }
    }
    
    private void accessDSIC(String server) {
        try {
            new ProcessBuilder("cmd.exe", "/c", "mstsc /v:" + server).start();
        } catch (IOException ex) {
            new Alert(Alert.AlertType.ERROR, ERROR_DSIC_MSG).show();
        }
    }
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        //Assigns actions to buttons and menu items
        menuLinuxDSIC.setOnAction(e -> accessDSIC(AccesoUPV.LINUX_DSIC));
        buttonLinuxDSIC.setOnAction(e -> accessDSIC(AccesoUPV.LINUX_DSIC));
        
        menuWinDSIC.setOnAction(e -> accessDSIC(AccesoUPV.WIN_DSIC));
        buttonWinDSIC.setOnAction(e -> accessDSIC(AccesoUPV.WIN_DSIC));
        
        menuAyuda.setOnAction(e -> showAyuda(""));
        menuAyudaDSIC.setOnAction(e -> showAyuda("DSIC"));
        menuAyudaVPN.setOnAction(e -> showAyuda("VPN"));
        
        menuAjustes.setOnAction(e -> showAjustes());
        
        buttonDisconnectW.setOnAction(e -> acceso.disconnectW());
        menuDisconnectW.setOnAction(e -> acceso.disconnectW());
        
        //Setting bindings for buttons
        buttonDisconnectW.disableProperty().bind(Bindings.not(acceso.connectedWBinding()));
        menuDisconnectW.disableProperty().bind(Bindings.not(acceso.connectedWBinding()));
        
        //Si no está ya conectado a la UPV, trata de conectarse a la VPN
        if (!acceso.isConnectedToUPV()) connectVPN();
    }
}
