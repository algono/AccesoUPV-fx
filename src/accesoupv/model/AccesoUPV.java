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
import java.util.HashMap;
import java.util.Map;
import java.util.prefs.Preferences;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.control.Alert;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;

/**
 *
 * @author Alejandro
 */
public class AccesoUPV {
    
    //Variables
    private String drive, vpn, user;
    private boolean VPNConnected;
    private final BooleanProperty WConnected = new SimpleBooleanProperty();
    //Servers
    public static final String LINUX_DSIC = "linuxdesktop.dsic.upv.es";
    public static final String WIN_DSIC = "windesktop.dsic.upv.es";
    public static final String UPV_SERVER = "www.upv.es";
    
    
    public AccesoUPV() {
        //Loads prefs
        Preferences prefs = Preferences.userNodeForPackage(AccesoUPV.class);
        drive = prefs.get("drive", "");
        vpn = prefs.get("vpn", "");
        user = prefs.get("user", "");
        WConnected.set(false);
        VPNConnected = false;
    }
    //Getters
    public String getDrive() { return drive; }
    public String getVPN() { return vpn; }
    public String getUser() { return user; }
    public String getDirW() {
        return "\\\\nasupv.upv.es\\alumnos\\" + user.charAt(0) + "\\" + user;
    }
    //Setters
    public void setDrive(String d) { drive = d; }
    public void setVPN(String v) { vpn = v; }
    public void setUser(String u) { user = u; }
    
    //Checks if any variable is not defined
    public boolean isIncomplete() { return drive.isEmpty() || vpn.isEmpty() || user.isEmpty(); }
    //Checks if the drive letter is currently being used
    public boolean isDriveUsed() { return new File(drive).exists(); }
    //Gets the boolean values
    public boolean isVPNConnected() { return VPNConnected; }
    public boolean isWConnected() { return WConnected.get(); }
    public BooleanProperty WConnectedProperty() { return WConnected; }
    
    public Map<String,String> createMap() {
        Map<String,String> map = new HashMap<>();
        map.put("drive", drive);
        map.put("vpn", vpn);
        map.put("letter", user.charAt(0)+ "");
        map.put("user", user);
        return map;
    }
    /**
     * 
     * @return If the operation completed successfully.
     */
    public boolean connectVPN() {
        AccesoTask task = new ConnectTask("VPN", false);
        VPNConnected = new LoadingScreen(task).load();
        if (!VPNConnected) {
            //Si no se pudo hacer la VPN y aun así es posible acceder a la UPV, entendemos que estamos en la UPVNET
            try {
                if (InetAddress.getByName("www.upv.es").isReachable(AccesoTask.PING_TIMEOUT)) {
                    VPNConnected = false;
                    return true;
                }
            } catch (IOException ex) {}
            //Al tratarse claramente de un error, obtiene su mensaje de error (lo siguiente decidirá su contenido exacto)
            Alert errorAlert = task.getErrorAlert();
            Throwable exception = task.getException();
            /**
             * Si el error se debió a algo conocido, no hace falta que muestre un link a posibles errores
             * (puesto que ya se sabe a qué se debe).
             */
            
            //Si el error se debió a que la VPN fue inválida, borra el valor asociado a esta.
            if (exception instanceof IllegalArgumentException) {
                vpn = "";
            
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
                        URI oURL = new URI(ConnectTask.WEB_ERROR_VPN);
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
    
    public boolean accessW() {
        AccesoTask task = new ConnectTask("W");
        boolean succeeded = new LoadingScreen(task).load();
        WConnected.set(succeeded);
        if (!succeeded && task.getException() instanceof IllegalArgumentException) user = "";
        return succeeded;
    }
    
    public boolean disconnectAll() {
        boolean succeeded;
        if (isVPNConnected() && isWConnected()) {
            AccesoTask task = new DisconnectTask("ALL");
            succeeded = new LoadingScreen(task).load();
        } else {
            succeeded = disconnectW();
            if (succeeded) succeeded = disconnectVPN();
        }
        return succeeded;
    }
    public boolean disconnectW() {
        boolean succeeded = true;
        if (isWConnected()) {
            AccesoTask task = new DisconnectTask("W");
            succeeded = new LoadingScreen(task).load();
            WConnected.set(!succeeded); //Si lo hizo bien, lo pone a false. Si no, a true
        }
        return succeeded;
    }
    
    public boolean disconnectVPN() {
        boolean succeeded = true;
        if (isVPNConnected()) {
            AccesoTask task = new DisconnectTask("VPN");
            succeeded = new LoadingScreen(task).load();
        }
        return succeeded;
    }
    
    //Guardar las preferencias
    public void savePrefs() {
        Preferences prefs = Preferences.userNodeForPackage(AccesoUPV.class);
        prefs.put("drive", drive);
        prefs.put("vpn", vpn);
        prefs.put("user", user);
    }
}
