/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package accesoupv.model;

import accesoupv.model.tasks.*;
import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;
import javafx.scene.control.Alert;
import javafx.scene.control.Hyperlink;
import lib.LoadingStage;

/**
 *
 * @author Alejandro
 */
public final class AccesoUPV {
    
    //Variables
    private String vpn, user, drive;
    private String connectedVPN, connectedUser, connectedDrive;
    //Preferences
    private final Preferences prefs = Preferences.userNodeForPackage(this.getClass());
    //Servers
    public static final String LINUX_DSIC = "linuxdesktop.dsic.upv.es";
    public static final String WIN_DSIC = "windesktop.dsic.upv.es";
    public static final String UPV_SERVER = "www.upv.es";
    //Timeouts (unit: miliseconds)
    public static final int INITIAL_PING_TIMEOUT = 500; //Timeout for checking if the user is already connected to the UPV
    public static final int FINAL_PING_TIMEOUT = 4000; //Timeout for checking if the connected VPN is able to connect the UPV
    
    // Singleton patron (only 1 instance of this object can be constructed)
    private static AccesoUPV acceso;
    
    //Creating a new object loads again all prefs
    private AccesoUPV() {
        loadPrefs();
        //Whenever the application is going to exit, saves prefs
        Runtime.getRuntime().addShutdownHook(new Thread(() -> savePrefs()));
        acceso = this; //Stores this object
    }
    // Returns the object instance stored here
    public static AccesoUPV getInstance() {
        if (acceso == null) acceso = new AccesoUPV();
        return acceso;
    }
    //Load and save variables (prefs)
    public void loadPrefs() {
        vpn = prefs.get("vpn", null);
        user = prefs.get("user", null);
        drive = prefs.get("drive", "W:"); //Drive by default is W:
    }
    
    public void savePrefs() {
        if (vpn == null) prefs.remove("vpn");
        else prefs.put("vpn", vpn);
        if (user == null) prefs.remove("user");
        else prefs.put("user", user);
        if (drive == null || drive.equals("W:")) prefs.remove("drive");
        else prefs.put("drive", drive);
    }
    
    //Getters
    public String getVPN() { return vpn; }
    public String getUser() { return user; }
    public String getDrive() { return drive; }
    //Setters
    public void setVPN(String v) { vpn = v.isEmpty() ? null : v; }
    public void setUser(String u) { user = u.isEmpty() ? null : u; }
    public void setDrive(String d) { drive = d; }
    
    //Checks if any variable is not defined
    public boolean isIncomplete() { return vpn == null || user == null || drive == null; }
    //Checks if the drive letter is currently being used
    public boolean isDriveUsed() { return new File(drive).exists(); }
    //Gets the boolean values
    public boolean isVPNConnected() { return connectedVPN != null; }
    public boolean isWConnected() { return connectedDrive != null; }
    
    public static List<String> getAvailableDrives() {
        List<String> drives = new ArrayList<>();
        char letter = 'Z';
        while (letter >= 'D') {
            String d = letter + ":";
            if (!new File(d).exists()) drives.add(d);
            letter--;
        }
        return drives;
    }
    
    public boolean connectUPV() {
        //Si ya es posible acceder a la UPV, estamos conectados
        try {
            if (InetAddress.getByName("www.upv.es").isReachable(INITIAL_PING_TIMEOUT)) {
                return true;
            }
        } catch (IOException ex) {}
        //Si no, trata de conectarse a la VPN
        return connectVPN();
    }
    
    public boolean createVPN() {
        LoadingStage stage = new LoadingStage(new CreateVPNTask(vpn));
        stage.showAndWait();
        return stage.isSucceeded();
    }
    
    /**
     * @return If the operation completed successfully.
     */
    public boolean connectVPN() {
        if (vpn == null) return false;
        AccesoTask task = new VPNTask(vpn, true);
        task.showErrorMessage(false);
        //Si el usuario cancela el proceso de conexion, sale del programa
        task.setOnCancelled((evt) -> System.exit(0));
        LoadingStage stage = new LoadingStage(task);
        stage.showAndWait();
        boolean succeeded = stage.isSucceeded();
        if (succeeded) {
            //Si desde la VPN a la que se conectó no se puede acceder a la UPV, se entiende que ha elegido una incorrecta.
            try {    
                if (!InetAddress.getByName("www.upv.es").isReachable(FINAL_PING_TIMEOUT)) {
                    Alert alert = new Alert(Alert.AlertType.ERROR, VPNTask.ERROR_INVALID_VPN);
                    alert.setHeaderText(null);
                    alert.showAndWait();
                    disconnectVPN();
                    vpn = null;
                    return false;
                }
            } catch (IOException ex) {}
            //Almacena el nombre de la VPN a la que se ha conectado
            connectedVPN = vpn;
        } else {
            Alert errorAlert = task.getErrorAlert();
            Hyperlink helpLink = new Hyperlink("Para más información acerca de posibles errores, pulse aquí");
            helpLink.setOnAction(e -> {
                try {
                    Desktop desktop = Desktop.getDesktop();
                    URI oURL = new URI(VPNTask.WEB_ERROR_VPN);
                    desktop.browse(oURL);
                } catch (IOException | URISyntaxException ex) {
                    new Alert(Alert.AlertType.ERROR, "Ha ocurrido un error al tratar de abrir el navegador.").show();
                }
            });
            errorAlert.showAndWait();
        }
        return succeeded;
    }
    
    public boolean connectW() {
        if (user == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "No ha especificado ningún usuario. Establezca uno.");
            alert.setHeaderText(null);
            alert.showAndWait();
            return false;
        }
        boolean succeeded;
        if (isDriveUsed()) {
            connectedUser = user;
            connectedDrive = drive;
            succeeded = true;
        } else {
            AccesoTask task = new WTask(user, drive, true);
            task.showErrorMessage(false);
            LoadingStage stage = new LoadingStage(task);
            stage.showAndWait();
            succeeded = stage.isSucceeded();
            if (succeeded) {
                connectedUser = user;
                connectedDrive = drive;
            } else {
                if (task.getException() instanceof IllegalArgumentException) user = null; 
                task.getErrorAlert().showAndWait();
            }
        }
        return succeeded;
    }
    
    public boolean shutdown() {
        LoadingStage stage = new LoadingStage();
        if (isWConnected()) stage.getQueue().add(new WTask(connectedUser, connectedDrive, false));
        if (isVPNConnected()) {
            AccesoTask vpnTask = new VPNTask(connectedVPN, false);
            vpnTask.setExitOnFailed(true);
            stage.getQueue().add(vpnTask);
        }
        //Si la cola no está vacía (es decir, tiene que realizar alguna tarea), la/las realiza
        boolean succeeded = stage.getQueue().isEmpty();
        if (!succeeded) {
            stage.showAndWait();
            succeeded = stage.isSucceeded();
        }
        return succeeded;
    }
    
    public boolean disconnectW() {
        boolean succeeded = true;
        if (isWConnected()) {
            AccesoTask task = new WTask(connectedUser, connectedDrive, false);
            LoadingStage stage = new LoadingStage(task);
            stage.showAndWait();
            succeeded = stage.isSucceeded();
            if (succeeded) connectedDrive = null;
        }
        return succeeded;
    }
    
    public boolean disconnectVPN() {
        boolean succeeded = true;
        if (isVPNConnected()) {
            AccesoTask task = new VPNTask(connectedVPN, false);
            task.setExitOnFailed(true);
            LoadingStage stage = new LoadingStage(task);
            stage.showAndWait();
            succeeded = stage.isSucceeded();
        }
        return succeeded;
    }
}
