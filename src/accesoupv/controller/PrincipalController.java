/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package accesoupv.controller;

import static accesoupv.Launcher.acceso;
import accesoupv.model.AccesoUPV;
import accesoupv.model.CodeFiller;
import accesoupv.model.LoadingTask;
import java.awt.Desktop;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.binding.Bindings;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.stage.FileChooser;
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
    
    public void gotoLoadingScreen(Task task) {
        try {
            Stage stage = new Stage();
            FXMLLoader myLoader = new FXMLLoader(getClass().getResource("/accesoupv/view/LoadingView.fxml"));
            Parent root = (Parent) myLoader.load();
            LoadingController dialogue = myLoader.<LoadingController>getController();
            dialogue.init(stage, task);
            Scene scene = new Scene(root);
            
            stage.setScene(scene);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
        } catch (IOException ex) {}
    }
    
    @FXML
    private void createCMD(ActionEvent event) {
        try {
            //El usuario elige la ruta del output
            FileChooser fc = new FileChooser();
            fc.setInitialFileName("Programa.cmd");
            fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Archivo de comandos(*.cmd)", "*.cmd"));
            File output = fc.showSaveDialog(textState.getScene().getWindow());
            if (output != null) {
                output.createNewFile();
                InputStream in = getClass().getResourceAsStream("/accesoupv/resources/code.txt");
                CodeFiller filler = new CodeFiller(in, acceso.createMap(), new FileOutputStream(output));
                filler.transcript();
                //Si todo ha ido bien, muestra un mensaje de éxito, y da al usuario la opción de acceder a la carpeta donde se encuentra el output
                Alert success = new Alert(Alert.AlertType.CONFIRMATION, SUCCESS_CMD_MSG);
                success.setHeaderText(null);
                Optional<ButtonType> result = success.showAndWait();
                if (result.get() == ButtonType.OK) {
                    try {
                        Desktop.getDesktop().open(output.getParentFile());
                    } catch (IOException ex) {
                        new Alert(Alert.AlertType.ERROR, ERROR_FOLDER_MSG).show();
                    }
                }
            }
        } catch (IOException ex) {
            new Alert(Alert.AlertType.ERROR, ERROR_CMD_MSG).show();
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
            new Alert(Alert.AlertType.ERROR, ERROR_CMD_MSG).show();
        }
    }
    
    private void connectVPN() {
        LoadingTask task = new LoadingTask();
        task.addCallable(task::connectVPN);
        task.setExitOnFailed(true);
        gotoLoadingScreen(task);
    }
    @FXML
    private void accessW(ActionEvent event) {
        LoadingTask task;
        if (!acceso.isWConnected()) {
            task = new LoadingTask();
            task.addCallable(task::accessW);
            gotoLoadingScreen(task);
        }
        if (acceso.isWConnected()) {
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
        menuLinuxDSIC.setOnAction((e) -> accessDSIC(AccesoUPV.LINUX_DSIC));
        buttonLinuxDSIC.setOnAction((e) -> accessDSIC(AccesoUPV.LINUX_DSIC));
        menuWinDSIC.setOnAction((e) -> accessDSIC(AccesoUPV.WIN_DSIC));
        buttonWinDSIC.setOnAction((e) -> accessDSIC(AccesoUPV.WIN_DSIC));
        menuAyuda.setOnAction((e) -> gotoAyuda(""));
        menuAyudaDSIC.setOnAction((e) -> gotoAyuda("DSIC"));
        menuAyudaVPN.setOnAction((e) -> gotoAyuda("VPN"));
        menuAjustes.setOnAction((e) -> gotoAjustes(false));
        buttonDisconnectW.setOnAction((e) -> {
            LoadingTask task = new LoadingTask();
            task.addCallable(task::disconnectW);
            gotoLoadingScreen(task);
            Alert success = new Alert(Alert.AlertType.INFORMATION, "Disco W eliminado con éxito.");
            success.setTitle("Completado");
            success.setHeaderText(null);
            success.show();
        });
        //Sets bindings
        buttonDisconnectW.disableProperty().bind(Bindings.not(acceso.isWConnected));
        //Sets up the VPN connection
        connectVPN();
    }
}
