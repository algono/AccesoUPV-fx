/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package accesoupv.model;

import static accesoupv.Launcher.acceso;
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
    
    public LoadingTask(String errorMsg) {
        callable = null;
        setOnFailed((e) -> {
            new Alert(Alert.AlertType.ERROR, errorMsg).showAndWait();
            Platform.exit();
        });
    }
    
    public void setCallable(Callable c) { callable = c; }
    
    @Override
    protected Void call() throws Exception {
        return (callable != null) ? callable.call() : null;
    }
    //Metodos posibles para LoadingTask
    public Void connectVPN() throws Exception {
        updateMessage("Conectando con la UPV...");
        boolean reachable = InetAddress.getByName("www.upv.es").isReachable(TIMEOUT);
        if (!reachable) {
            updateMessage("Realizando conexión VPN a la UPV...");
            Process p = new ProcessBuilder("cmd.exe", "/c", "rasdial " + acceso.getVPN()).start();
            p.waitFor();
        }
        return null;
    }
    public Void accessW() throws Exception {
        updateMessage("Accediendo al disco W...");
        String drive = acceso.getDrive();
        Process p = new ProcessBuilder("cmd.exe", "/c", "net use " + drive + " " + acceso.getDirW()).start();
        p.waitFor();
        acceso.isWConnected.set(true);
        return null;
    }
    
}
