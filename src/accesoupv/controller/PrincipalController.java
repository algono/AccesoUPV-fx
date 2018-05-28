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
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextInputDialog;
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
    public static final String ERROR_FOLDER_MSG = "Ha habido un error al tratar de abrir la carpeta. Ábrala manualmente.";
    public static final String ERROR_DSIC_MSG = "No se ha podido acceder al servidor DSIC.";
    //AccesoUPV Instance
    private static final AccesoUPV acceso = AccesoUPV.getInstance();
    
    private void showAjustes() { showAjustes(false); }
    private void showAjustes(boolean exitOnCancelled) {
        if (acceso.isWConnected()) {
            String wMsg = "No se permite acceder a los ajustes mientras el disco W se encuentre conectado.\n\n"
                    + "¿Desea desconectarlo?";
            Alert warningW = new Alert(Alert.AlertType.WARNING, wMsg);
            warningW.setTitle("Disco W Conectado");
            warningW.setHeaderText(null);
            warningW.getButtonTypes().setAll(ButtonType.OK, ButtonType.CANCEL);
            Optional<ButtonType> res = warningW.showAndWait();
            if (res.isPresent() && res.get() == ButtonType.OK) {
                if (!disconnectW()) return;
            } else return;
        }
        try {    
            Stage stage = new Stage();
            FXMLLoader myLoader = new FXMLLoader(getClass().getResource("/accesoupv/view/AjustesView.fxml"));
            Parent root = (Parent) myLoader.load();
            AjustesController dialogue = myLoader.<AjustesController>getController();
            dialogue.init(stage, exitOnCancelled);
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
    
    private Optional<String> setVPNDialogue(boolean isNew) {
        String inputContentText = (isNew)
                        ? "Introduzca el nombre de la nueva conexión VPN a la UPV: " 
                        : "Introduzca el nombre de la conexión VPN existente a la UPV: ";
        TextInputDialog dialog = new TextInputDialog(acceso.getVPN());
        dialog.setTitle("Introduzca nombre VPN");
        dialog.setHeaderText(null);
        dialog.setContentText(inputContentText);
        return dialog.showAndWait();
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
        if (!alertRes.isPresent() || alertRes.get() == ButtonType.CANCEL) System.exit(0);
        boolean setNewVPN = alertRes.get() == ButtonType.YES;
        Optional<String> newVPN = setVPNDialogue(setNewVPN);
        //Si ha escrito un valor, le cambia el nombre. Si no, sale del programa.
        if (newVPN.isPresent()) {
            acceso.setVPN(newVPN.get());
            if (setNewVPN) acceso.createVPN(); //Si se ha elegido crear una VPN nueva, la crea.
            connectUPV();
        } else { System.exit(0); }
    }
    
    private void connectUPV() {
        boolean succeeded = acceso.connectUPV();
        //Si la ejecución falló...
        if (!succeeded) {
            //Y fue porque no tenía VPN establecida, permite al usuario establecerla
            if (acceso.getVPN() == null) {
                establishVPN();
            } else {
                //Si no fue por eso, permite cambiar el valor de la VPN por si lo puso mal
                Optional<String> newVPN = setVPNDialogue(false);
                if (newVPN.isPresent()) {
                    acceso.setVPN(newVPN.get());
                    connectUPV();
                } else { System.exit(0); }    
            }
        }
    }
    /**
     * Checks if the drive where W should be is set up before this program did it.
     * @return Whether the execution should continue or not.
     */
    private boolean checkDrive() {
        if (!acceso.isWConnected() && acceso.isDriveUsed()) {
            String WARNING_W = 
                    "La unidad definida para el disco W (" + acceso.getDrive() + ") ya contiene un disco asociado.\n\n"
                    + "Antes de continuar, desconecte el disco asociado, o cambie la unidad para el disco W desde los ajustes.\n ";
            Alert warning = new Alert(Alert.AlertType.WARNING);
            String actDrive = acceso.getDrive();
            warning.setHeaderText("Unidad " + actDrive + " contiene disco");
            warning.setContentText(WARNING_W);
            ButtonType continuar = new ButtonType("Continuar");
            ButtonType ajustes = new ButtonType("Ajustes", ButtonData.LEFT);
            warning.getButtonTypes().setAll(continuar, ajustes, ButtonType.CANCEL);
            Optional<ButtonType> result = warning.showAndWait();
            if (!result.isPresent() || result.get() == ButtonType.CANCEL) { return false; }
            else if (result.get() == ajustes) { 
                showAjustes();
                if (acceso.getDrive().equals(actDrive)) { return false; }
            }
            else {
                if (acceso.isDriveUsed()) {
                    WARNING_W = "El disco aún no ha sido desconectado. Si se trata del disco W, puede continuar sin problemas.\n"
                            + "Pero si no, el programa no funcionará correctamente.\n\n"
                            + "¿Desea continuar?";
                    warning.setContentText(WARNING_W);
                    warning.getButtonTypes().setAll(ButtonType.OK, ButtonType.CANCEL);
                    result = warning.showAndWait();
                    if (!result.isPresent() || result.get() == ButtonType.CANCEL) { return false; }
                }
            }
        }
        return true;
    }
    @FXML
    private void accessW(ActionEvent evt) {
        if (checkDrive()) {
            //Si la ejecución falló...
            boolean succeeded = acceso.connectW();
            buttonDisconnectW.setDisable(!succeeded);
            menuDisconnectW.setDisable(!succeeded);
            if (succeeded) {
                try {
                    Desktop.getDesktop().open(new File(acceso.getDrive()));
                } catch (IOException ex) {
                    new Alert(Alert.AlertType.ERROR, ERROR_FOLDER_MSG).show();
                }
            } else {
                //...y el nombre del usuario no era válido... acceder a los ajustes para cambiarlo
                if (acceso.isIncomplete()) {
                    String actUser = acceso.getUser();
                    showAjustes();
                    //Si se ha cambiado el usuario, lo vuelve a intentar
                    if (!acceso.getUser().equals(actUser)) { accessW(evt); }
                }
            }
        }
    }
    private boolean disconnectW() {
        boolean succeeded = acceso.disconnectW();
        buttonDisconnectW.setDisable(succeeded);
        menuDisconnectW.setDisable(succeeded);
        return succeeded;
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
        //Assigns buttons' actions
        menuLinuxDSIC.setOnAction(e -> accessDSIC(AccesoUPV.LINUX_DSIC));
        buttonLinuxDSIC.setOnAction(e -> accessDSIC(AccesoUPV.LINUX_DSIC));
        menuWinDSIC.setOnAction(e -> accessDSIC(AccesoUPV.WIN_DSIC));
        buttonWinDSIC.setOnAction(e -> accessDSIC(AccesoUPV.WIN_DSIC));
        menuAyuda.setOnAction(e -> showAyuda(""));
        menuAyudaDSIC.setOnAction(e -> showAyuda("DSIC"));
        menuAyudaVPN.setOnAction(e -> showAyuda("VPN"));
        menuAjustes.setOnAction(e -> showAjustes());
        buttonDisconnectW.setOnAction(e -> disconnectW());
        menuDisconnectW.setOnAction(e -> disconnectW());
        connectUPV();
    }
}
