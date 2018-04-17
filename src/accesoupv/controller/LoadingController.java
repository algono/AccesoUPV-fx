/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package accesoupv.controller;

import static accesoupv.Launcher.acceso;
import accesoupv.model.AccesoUPV;
import java.net.InetAddress;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * FXML Controller class
 *
 * @author Alejandro
 */
public class LoadingController implements Initializable {

    @FXML
    private Label textState;
    //Messages
    public static final String ERROR_VPN_MSG = "Ha habido un error al tratar de conectarse a la UPV. Inténtelo de nuevo más tarde.";
    //Stage
    private Stage primaryStage;
    
    public void init(Stage stage) {
        primaryStage = stage;
        primaryStage.setTitle("Acceso UPV");
        //remove window decoration
        primaryStage.initStyle(StageStyle.UNDECORATED);
        Platform.runLater(() -> connectVPN());
    }
    
    public void connectVPN() {
        Task<Void> connectTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                updateMessage("Comprobando conectividad a la UPV...");
                boolean reachable = InetAddress.getByName(AccesoUPV.LINUX_DSIC).isReachable(4000);
                if (!reachable) {
                    updateMessage("Realizando conexión VPN a la UPV...");
                    new ProcessBuilder("cmd.exe", "/c", "rasdial " + acceso.getVPN()).start();
                }
                return null;
            }
        };
        textState.textProperty().bind(connectTask.messageProperty());
        connectTask.setOnSucceeded((e) -> primaryStage.hide());
        connectTask.setOnFailed((e) -> {
            new Alert(Alert.AlertType.ERROR, ERROR_VPN_MSG).showAndWait();
            Platform.exit();
        });
        Thread t = new Thread(connectTask);
        t.start();
    }
    
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }    
    
}
