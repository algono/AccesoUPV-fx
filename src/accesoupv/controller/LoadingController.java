/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package accesoupv.controller;

import static accesoupv.Launcher.acceso;
import accesoupv.model.AccesoUPV;
import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutionException;
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
    //Constants
    public static final String ERROR_VPN_MSG = "Ha habido un error al tratar de conectarse a la UPV. Inténtelo de nuevo más tarde.";
    public static final int TIMEOUT = 3000;
    //Stage
    private Stage primaryStage;
    //Task this loading screen is for
    private Task<Boolean> task;
    
    public boolean init(Stage stage, String taskId) {
        primaryStage = stage;
        primaryStage.setTitle("Acceso UPV");
        //remove window decoration
        primaryStage.initStyle(StageStyle.UNDECORATED);
        boolean completed = false;
        switch(taskId) {
            case "VPN": completed = connectVPN(); break;
            case "W": accessW(); break;
        }
        return completed;
    }
    
    public boolean startTask() {
        boolean completed = false;
        new Thread(task).start();
        try {
            completed = task.get();
        } catch (InterruptedException | ExecutionException ex) {
            task.getOnFailed().handle(null);
        }
        return completed;
    }
    
    /**
     *
     * @return If VPN was connected in the process
     */
    public boolean connectVPN() {
        task = new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception {
                updateMessage("Comprobando conectividad a la UPV...");
                boolean reachable = InetAddress.getByName(AccesoUPV.LINUX_DSIC).isReachable(TIMEOUT);
                if (!reachable) {
                    updateMessage("Realizando conexión VPN a la UPV...");
                    Process p = new ProcessBuilder("cmd.exe", "/c", "rasdial " + acceso.getVPN()).start();
                    p.waitFor();
                    return true;
                }
                return false;
            }
        };
        textState.textProperty().bind(task.messageProperty());
        task.setOnSucceeded((e) -> primaryStage.hide());
        task.setOnFailed((e) -> {
            new Alert(Alert.AlertType.ERROR, ERROR_VPN_MSG).showAndWait();
            Platform.exit();
        });
        return startTask();     
    }
    
    public boolean accessW() {
        task = new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception {
                updateMessage("Accediendo al disco W...");
                String drive = acceso.getDrive();
                Process p = new ProcessBuilder("cmd.exe", "/c", "net use " + drive + " " + acceso.getDirW()).start();
                p.waitFor();
                try {
                    Desktop.getDesktop().open(new File(drive));
                } catch (IOException ex) {
                    new Alert(Alert.AlertType.ERROR, "Ha habido un error al tratar de abrir la carpeta del disco W. Hágalo manualmente.").show();
                    return false;
                }
                return true;
            }
        };
        textState.textProperty().bind(task.messageProperty());
        task.setOnSucceeded((e) -> primaryStage.hide());
        task.setOnFailed((evt) -> {
            new Alert(Alert.AlertType.ERROR, "Ha habido un error al tratar de conectarse al disco W. Inténtelo de nuevo más tarde.").show();
        });
        boolean isConnected = startTask();
        acceso.isWConnected.set(isConnected);
        return isConnected;
    }
    
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        //TODO
    }    
    
}
