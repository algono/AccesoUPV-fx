/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package accesoupv.model;

import accesoupv.model.tasks.*;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.prefs.Preferences;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

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
    //Updates the isWConnected property and returns the result
    public boolean isDriveUsed() {
        boolean res = new File(drive).exists();
        return res;
    }
    
    public BooleanProperty WConnectedProperty() { return WConnected; }
    
    public boolean isVPNConnected() { return VPNConnected; }
    public boolean isWConnected() {
        boolean res = new File(drive).exists();
        WConnected.set(res);
        return res;
    }
    
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
            //Si no se puede acceder a la UPV sin la VPN, se trata de un error, así que debe mostrarlo
            task.getErrorAlert().showAndWait();
            //Si el error se debió a que la VPN fue inválida, borra el valor asociado a esta.
            if (task.getException() instanceof IllegalArgumentException) {
                vpn = "";
            }
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
