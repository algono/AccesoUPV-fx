/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package accesoupv.model;

import static accesoupv.Launcher.acceso;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.util.concurrent.Callable;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.control.Alert;

/**
 *
 * @author Alejandro
 */
public class LoadingTask extends Task<Void> {
    //Messages
    public static final String ERROR_VPN = "Ha habido un error al tratar de conectarse a la UPV. Inténtelo de nuevo más tarde.";
    public static final String ERROR_W = "Ha habido un error al tratar de conectarse al disco W. Inténtelo de nuevo más tarde.";
    //Timeout
    public static final int TIMEOUT = 3000;
    //Callable method
    private Callable<Void> callable;
    private final Alert errorAlert;
    private boolean exitOnFailed;
    
    public LoadingTask(String errorMsg, boolean exitOF) {
        callable = null;
        exitOnFailed = exitOF;
        errorAlert = new Alert(Alert.AlertType.ERROR, errorMsg);
        errorAlert.setHeaderText(null);
        setOnFailed((e) -> {
            errorAlert.showAndWait();
            if (exitOnFailed) {
                Platform.exit();
                System.exit(-1);
            }
        });
    }
    //Getters
    public Callable getCallable() { return callable; }
    public String getErrorMessage() { return errorAlert.getContentText(); }
    public boolean getExitOnFailed() { return exitOnFailed; }
    //Setters
    public void setCallable(Callable c) { callable = c; }
    public void setErrorMessage(String msg) { errorAlert.setContentText(msg); }
    public void setExitOnFailed(boolean b) { exitOnFailed = b; }
    
    @Override
    protected Void call() throws Exception {
        return (callable != null) ? callable.call() : null;
    }
    //Metodos posibles para LoadingTask
    public Void connectVPN() throws Exception {
        updateMessage("Conectando con la UPV...");
        Process p = new ProcessBuilder("cmd.exe", "/c", "rasdial " + acceso.getVPN()).start();
        p.waitFor();
        if (!InetAddress.getByName("www.upv.es").isReachable(TIMEOUT)) {
            throw new IOException();
        }
        return null;
    }
    public Void accessW() throws Exception {
        updateMessage("Accediendo al disco W...");
        String drive = acceso.getDrive();
        Process p = new ProcessBuilder("cmd.exe", "/c", "net use " + drive + " " + acceso.getDirW()).start();
        p.waitFor();
        if (!acceso.isWConnected()) {
            throw new IOException();
        }
        return null;
    }
    public Void disconnect() throws Exception {
        if (acceso.isWConnected.get()) {
            updateMessage("Desconectando Disco W...");
            acceso.disconnectW();
        }
        updateMessage("Desconectando de la UPV...");
        acceso.disconnectVPN();
        return null;
    }
}
