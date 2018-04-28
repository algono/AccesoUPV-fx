/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package accesoupv.controller;

import static accesoupv.Launcher.acceso;
import accesoupv.model.AccesoUPV;
import accesoupv.model.LoadingScreen;
import accesoupv.model.LoadingTask;
import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
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
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 *
 * @author Alejandro
 */
public class PrincipalController implements Initializable {
    
    @FXML
    private Label textState;
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
    public static final String SUCCESS_CMD_MSG = "El archivo ha sido creado con éxito.\n¿Desea abrir la carpeta en la cual ha sido guardado?";
    public static final String ERROR_CMD_MSG = "Ha habido un error al crear el programa. Vuelva a intentarlo.";
    public static final String ERROR_FOLDER_MSG = "Ha habido un error al tratar de abrir la carpeta. Ábrala manualmente.";
    public static final String ERROR_DSIC_MSG = "No se ha podido acceder al servidor DSIC.";
    
    private void gotoAjustes(boolean exitOnCancelled) {
        if (acceso.isWConnected()) {
            String wMsg = "No se permite acceder a los ajustes mientras el disco W se encuentre conectado.\n\n"
                    + "¿Desea desconectarlo?";
            Alert conf = new Alert(Alert.AlertType.CONFIRMATION, wMsg);
            conf.setTitle("Disco W Conectado");
            conf.setHeaderText(null);
            Optional<ButtonType> res = conf.showAndWait();
            if (res.isPresent() && res.get() == ButtonType.OK) {
                LoadingTask task = new LoadingTask();
                task.addCallable(task::disconnectW);
                boolean succeeded = LoadingScreen.loadTask(task);
                //Si después de intentar desconectarlo sigue conectado, se entiende que ha fallado y no continúa
                if (!succeeded) return;
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
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
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
            new Alert(Alert.AlertType.ERROR, ERROR_CMD_MSG).show();
        }
    }
    
    private void connectVPN() {
        boolean succeeded = acceso.connectVPN();
        //Si la ejecución falló...
        if (!succeeded) {
            //Y la causó una variable inválida... accede a los ajustes para cambiarla
            if (acceso.isIncomplete()) {
                gotoAjustes(true);
                connectVPN();
            } else {
                Platform.exit();
                System.exit(-1);
            }
        }
    }
    /**
     * Checks if the drive where W would be is already set up.
     * @return Whether the execution should continue or not.
     */
    private boolean checkDrive() {
        if (!acceso.isWConnected() && acceso.isDriveUsed()) {
            String WARNING_W = 
                    "La unidad definida para el disco W (" + acceso.getDrive() + ") ya contiene un disco asociado.\n\n"
                    + "Antes de continuar, desconecte el disco asociado, o cambie la unidad para el disco W desde los ajustes.\n ";
            Alert warning = new Alert(Alert.AlertType.WARNING);
            warning.setHeaderText("Unidad " + acceso.getDrive() + " contiene disco");
            warning.setContentText(WARNING_W);
            ButtonType continuar = new ButtonType("Continuar");
            ButtonType ajustes = new ButtonType("Ajustes", ButtonData.LEFT);
            warning.getButtonTypes().setAll(continuar, ajustes, ButtonType.CANCEL);
            Optional<ButtonType> result = warning.showAndWait();
            if (!result.isPresent() || result.get() == ButtonType.CANCEL) { return false; }
            else if (result.get() == ajustes) gotoAjustes(false);
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
            if (!acceso.isWConnected()) {
                //Si la ejecución falló...
                if (!acceso.accessW()) {
                    //...y el nombre del usuario no era válido... acceder a los ajustes para cambiarlo
                    if (acceso.isIncomplete()) gotoAjustes(false);
                    return;
                }
            }
            try {
                Desktop.getDesktop().open(new File(acceso.getDrive()));
            } catch (IOException ex) {
                new Alert(Alert.AlertType.ERROR, ERROR_FOLDER_MSG).show();
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
        //If prefs are incomplete, go to Preferences
        if (acceso.isIncomplete()) { gotoAjustes(true); }
        //Assigns buttons' actions
        menuLinuxDSIC.setOnAction(e -> accessDSIC(AccesoUPV.LINUX_DSIC));
        buttonLinuxDSIC.setOnAction(e -> accessDSIC(AccesoUPV.LINUX_DSIC));
        menuWinDSIC.setOnAction(e -> accessDSIC(AccesoUPV.WIN_DSIC));
        buttonWinDSIC.setOnAction(e -> accessDSIC(AccesoUPV.WIN_DSIC));
        buttonDisconnectW.setOnAction(e -> acceso.disconnectW());
        menuDisconnectW.setOnAction(e -> acceso.disconnectW());
        menuAyuda.setOnAction(e -> gotoAyuda(""));
        menuAyudaDSIC.setOnAction(e -> gotoAyuda("DSIC"));
        menuAyudaVPN.setOnAction(e -> gotoAyuda("VPN"));
        menuAjustes.setOnAction(e -> gotoAjustes(false));
        //Sets bindings
        buttonDisconnectW.disableProperty().bind(Bindings.not(acceso.WConnectedProperty()));
        menuDisconnectW.disableProperty().bind(Bindings.not(acceso.WConnectedProperty()));
        connectVPN(); //Sets up the VPN connection
        textState.setText(acceso.isVPNConnected() ? "(vía VPN)" : "(vía UPVNET)");
    }
}
