/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package accesoupv.model;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.prefs.Preferences;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.control.Alert;

/**
 *
 * @author Alejandro
 */
public class AccesoUPV {
    
    //Variables
    private String drive, vpn, user;
    public final BooleanProperty isWConnected = new SimpleBooleanProperty();
    //Servers
    public static final String LINUX_DSIC = "linuxdesktop.dsic.upv.es";
    public static final String WIN_DSIC = "windesktop.dsic.upv.es";
    public static final String UPV_SERVER = "www.upv.es";
    
    
    public AccesoUPV() {
        Preferences prefs = Preferences.userNodeForPackage(AccesoUPV.class);
        drive = prefs.get("drive", null);
        vpn = prefs.get("vpn", null);
        user = prefs.get("user", null);
        isWConnected.set(false);
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
    
    //Checks if any variable is empty
    public boolean isIncomplete() { return drive == null || vpn == null || user == null; }
    //Updates the isWConnected property and returns the result
    public boolean isWConnected() {
        boolean res = new File(drive).exists();
        isWConnected.set(res);
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
    //Guardar las preferencias
    public void savePrefs() {
        Preferences prefs = Preferences.userNodeForPackage(AccesoUPV.class);
        prefs.put("drive", drive);
        prefs.put("vpn", vpn);
        prefs.put("user", user);
    }
    
    //Desconectar Disco W (si estaba conectado)
    public void disconnectW() {
        try {
            Process p = new ProcessBuilder("cmd.exe", "/c", "net use " + drive + " /delete").start();
            p.waitFor();
            isWConnected.set(false);
        } catch (IOException | InterruptedException ex) {
            new Alert(Alert.AlertType.ERROR, "Ha habido un error al desconectar el disco W. Deberá desconectarlo manualmente.").showAndWait();
        }
    }
    //Desconectar VPN
    public void disconnectVPN() {
        try {
            Process p = new ProcessBuilder("cmd.exe", "/c", "rasdial " + vpn + " /DISCONNECT").start();
            p.waitFor();
        } catch (IOException | InterruptedException ex) {
            new Alert(Alert.AlertType.ERROR, "No ha podido desconectarse la VPN. Deberá desconectarla manualmente.").showAndWait();
        }
    }
}
