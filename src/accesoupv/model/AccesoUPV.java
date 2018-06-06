/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package accesoupv.model;

import accesoupv.controller.AjustesController;
import accesoupv.model.tasks.*;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.prefs.Preferences;
import myLibrary.javafx.LoadingUtils.LoadingStage;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextInputDialog;

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
    //Timeout for checking if the user is already connected to the UPV (in ms)
    public static final int PING_TIMEOUT = 500;
    
    
    // Singleton patron (only 1 instance of this object can be constructed)
    private static AccesoUPV acceso;
    
    //Creating a new object loads all prefs
    private AccesoUPV() {
        loadPrefs();
        //Whenever the application is going to exit, saves prefs
        Runtime.getRuntime().addShutdownHook(new Thread(() -> savePrefs()));
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
    
    public boolean isConnectedToUPV() {
        //Si ya es posible acceder a la UPV, estamos conectados
        try {
            if (InetAddress.getByName("www.upv.es").isReachable(PING_TIMEOUT)) {
                return true;
            }
        } catch (IOException ex) {}
        return false;
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
        //Si el usuario cancela el proceso de conexión, sale del programa
        task.setOnCancelled((evt) -> System.exit(0));
        LoadingStage stage = new LoadingStage(task);
        stage.showAndWait();
        boolean succeeded = stage.isSucceeded();
        if (succeeded) connectedVPN = vpn;
        else if (task.getException() instanceof IllegalArgumentException) {
            if (setVPNDialog(false)) return connectVPN(); //Si la VPN no era válida, permite cambiarla, y si la cambió, vuelve a intentarlo.
        }
        return succeeded;
    }
    
    public boolean setVPNDialog(boolean isNew) {
        String inputContentText = (isNew)
                        ? "Introduzca el nombre de la nueva conexión VPN a la UPV: " 
                        : "Introduzca el nombre de la conexión VPN existente a la UPV: ";
        TextInputDialog dialog = new TextInputDialog(vpn);
        dialog.setTitle("Introduzca nombre VPN");
        dialog.setHeaderText(null);
        dialog.setContentText(inputContentText);
        Optional<String> res = dialog.showAndWait();
        res.ifPresent((newVPN) -> vpn = newVPN);
        return res.isPresent();
    }
    
    public boolean setUserDialog() {
        TextInputDialog dialog = new TextInputDialog(user);
        dialog.setTitle("Introduzca usuario");
        dialog.setHeaderText(null);
        dialog.setContentText("Introduzca su usuario de la UPV: ");
        dialog.getDialogPane().setExpandableContent(new Label(AjustesController.HELP_USER_TOOLTIP));
        Optional<String> res = dialog.showAndWait();
        res.ifPresent((newUser) -> user = newUser);
        return res.isPresent();
    }
    public boolean setDriveDialog() {
        List<String> drives = getAvailableDrives();
        String def = drives.contains("W:") ? "W:" : null;
        ChoiceDialog<String> dialog = new ChoiceDialog<>(def, drives);
        dialog.setTitle("Elegir unidad Disco W");
        dialog.setHeaderText(null);
        dialog.setContentText("Elija una unidad para su Disco W: ");
        Optional<String> res = dialog.showAndWait();
        res.ifPresent((newDrive) -> drive = newDrive);
        return res.isPresent();
    }
    
    public boolean connectW() {
        while (user == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "No ha especificado ningún usuario. Establezca uno.");
            alert.setHeaderText(null);
            alert.getButtonTypes().add(ButtonType.CANCEL);
            Optional<ButtonType> res = alert.showAndWait();
            if (!res.isPresent() || res.get() != ButtonType.OK) return false;
            setUserDialog();
        }
        AccesoTask task = new WTask(user, drive, true);
        LoadingStage stage = new LoadingStage(task);
        stage.showAndWait();
        boolean succeeded = stage.isSucceeded();
        if (succeeded) {
            connectedUser = user;
            connectedDrive = drive;
        } else if (task.getException() instanceof IllegalArgumentException) {
            if (setUserDialog()) return connectW(); //Si el usuario no era válido, permite cambiarlo, y si lo cambió, vuelve a intentarlo.
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
