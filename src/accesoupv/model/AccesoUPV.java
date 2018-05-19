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
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import lib.LoadingScreen;

/**
 *
 * @author Alejandro
 */
public final class AccesoUPV {
    
    //Variables
    private String drive, vpn, user;
    private String connectedVPN, connectedUser, connectedDrive;
    //Preferences
    private final Preferences prefs = Preferences.userNodeForPackage(this.getClass());
    //Servers
    public static final String LINUX_DSIC = "linuxdesktop.dsic.upv.es";
    public static final String WIN_DSIC = "windesktop.dsic.upv.es";
    public static final String UPV_SERVER = "www.upv.es";
    //Timeout
    public static final int PING_TIMEOUT = 500; //500 miliseconds
    
    // Singleton patron (only 1 instance of this object stored at a time)
    private static AccesoUPV acceso;
    
    //Creating a new object loads again all prefs
    public AccesoUPV() {
        loadPrefs();
        //Stores this object
        acceso = this;
    }
    // Returns the object instance stored here
    public static AccesoUPV getInstance() {
        if (acceso == null) acceso = new AccesoUPV();
        return acceso;
    }
    //Load and save variables (prefs)
    public void loadPrefs() {
        drive = prefs.get("drive", null);
        vpn = prefs.get("vpn", null);
        user = prefs.get("user", null);
    }
    
    public void savePrefs() {
        prefs.put("drive", drive);
        prefs.put("vpn", vpn);
        prefs.put("user", user);
    }
    
    //Getters
    public String getDrive() { return drive; }
    public String getVPN() { return vpn; }
    public String getUser() { return user; }
    //Setters
    public void setDrive(String d) { drive = d; }
    public void setVPN(String v) { vpn = v; }
    public void setUser(String u) { user = u; }
    
    //Checks if any variable is not defined
    public boolean isIncomplete() { return drive == null || vpn == null || user == null; }
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
            if (InetAddress.getByName("www.upv.es").isReachable(PING_TIMEOUT)) {
                return true;
            }
        } catch (IOException ex) {}
        //Si no, trata de conectarse a la VPN
        return connectVPN();
    }
    
    /**
     * 
     * @return If the operation completed successfully.
     */
    private boolean connectVPN() {
        if (vpn == null) return false;
        AccesoTask task = new VPNTask(vpn, true);
        task.showErrorMessage(false);
        boolean VPNConnected = new LoadingScreen(task).load();
        if (VPNConnected) {
            //Si desde la VPN a la que se conectó no se puede acceder a la UPV, se entiende que ha elegido una incorrecta.
            try {
                if (!InetAddress.getByName("www.upv.es").isReachable(PING_TIMEOUT)) {
                    disconnectVPN();
                    return false;
                }
            } catch (IOException ex) {}
            //Almacena el nombre de la VPN a la que se ha conectado
            connectedVPN = vpn;
        } else {
            //Al tratarse claramente de un error, obtiene su mensaje de error (lo siguiente decidirá su contenido exacto)
            Alert errorAlert = task.getErrorAlert();
            Throwable exception = task.getException();
            /**
             * Si el error se debió a algo conocido, no hace falta que muestre un link a posibles errores
             * (puesto que ya se sabe a qué se debe).
             */
            //Si el error se debió a que la VPN fue inválida, borra el valor asociado a esta.
            if (exception instanceof IllegalArgumentException) {
                vpn = null;
            } else if (exception instanceof IllegalStateException) {
            } else {
                /** Si se trata de un error no conocido por el programa,
                 * muestra también un link a una web de la UPV
                 * donde muestra posibles errores y cómo solucionarlos.
                 */
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
                TextArea errorContent = new TextArea(task.getException().getMessage());
                errorContent.setEditable(false);
                VBox errorVPNBox = new VBox(helpLink, errorContent);
                errorAlert.getDialogPane().setExpandableContent(errorVPNBox);
            }
            errorAlert.showAndWait();
        }
        return VPNConnected;
    }
    
    public boolean connectW() {
        if (user == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "No ha especificado ningún usuario. Establezca uno.");
            alert.setHeaderText(null);
            alert.showAndWait();
            return false;
        }
        AccesoTask task = new WTask(user, drive, true);
        task.showErrorMessage(false);
        boolean succeeded = new LoadingScreen(task).load();
        if (succeeded) {
            connectedUser = user;
            connectedDrive = drive;
        } else {
            if (task.getException() instanceof IllegalArgumentException) user = null; 
            task.getErrorAlert().showAndWait();
        }
        return succeeded;
    }
    
    public boolean shutdown() {
        LoadingScreen screen = new LoadingScreen();
        if (isWConnected()) screen.getQueue().add(new WTask(connectedUser, connectedDrive, false));
        if (isVPNConnected()) {
            AccesoTask vpnTask = new VPNTask(connectedVPN, false);
            vpnTask.setExitOnFailed(true);
            screen.getQueue().add(vpnTask);
        }
        return screen.load();
    }
    
    public boolean disconnectW() {
        boolean succeeded = true;
        if (isWConnected()) {
            AccesoTask task = new WTask(connectedUser, connectedDrive, false);
            succeeded = new LoadingScreen(task).load();
            if (succeeded) connectedDrive = null;
        }
        return succeeded;
    }
    
    public boolean disconnectVPN() {
        boolean succeeded = true;
        if (isVPNConnected()) {
            AccesoTask task = new VPNTask(connectedVPN, false);
            task.setExitOnFailed(true);
            succeeded = new LoadingScreen(task).load();
        }
        return succeeded;
    }
}
