/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package accesoupv.model;

import accesoupv.model.services.drives.AccesoDriveDSICService;
import accesoupv.model.services.drives.AccesoDriveWService;
import accesoupv.model.services.drives.AccesoDriveService;
import accesoupv.model.services.AccesoVPNService;
import accesoupv.controller.AjustesController;
import accesoupv.model.tasks.*;
import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.prefs.Preferences;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;
import myLibrary.javafx.Loading.LoadingStage;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextInputDialog;
import lib.PasswordDialog;

/**
 *
 * @author Alejandro
 */
public final class AccesoUPV {
   
    //Preferences
    private final Preferences prefs = Preferences.userNodeForPackage(this.getClass());
    //Variables
    private String VPN, user, driveW, driveDSIC, passDSIC;
    private Dominio domain;
    private boolean savePrefsOnExit = true;
    //Services
    private final AccesoVPNService VPNService;
    private final AccesoDriveWService WService;
    private final AccesoDriveDSICService DSICService;
    //Servers
    public static final String LINUX_DSIC = "linuxdesktop.dsic.upv.es";
    public static final String WIN_DSIC = "windesktop.dsic.upv.es";
    public static final String UPV_SERVER = "www.upv.es";
    //Messages
    public static final String WARNING_FOLDER_DRIVE_MSG = 
            "El disco ha sido conectado correctamente, pero no se ha podido abrir la carpeta.\n"
            + "Ábrala manualmente.";
    //Timeout for checking if the user is already connected to the UPV (in ms)
    public static final int PING_TIMEOUT = 500;
    
    
    // Singleton patron (only 1 instance of this object can be constructed)
    private static AccesoUPV acceso;
    
    //Creating a new object loads all prefs
    private AccesoUPV() {
        loadPrefs();
        VPNService = new AccesoVPNService(VPN);
        WService = new AccesoDriveWService(user, driveW, domain);
        DSICService = new AccesoDriveDSICService(user, driveDSIC);
    }
    // Returns the object instance stored here
    public static AccesoUPV getInstance() {
        if (acceso == null) acceso = new AccesoUPV();
        return acceso;
    }
    //Load and save variables (prefs)
    public void loadPrefs() {
        VPN = prefs.get("VPN", null);
        user = prefs.get("user", null);
        driveW = prefs.get("driveW", "*");
        driveDSIC = prefs.get("driveDSIC", "*");
        domain = prefs.getBoolean("non-student", false) ? Dominio.UPVNET : Dominio.ALUMNOS;
    }
    public void savePrefs() {
        if (VPN == null) prefs.remove("VPN");
        else prefs.put("VPN", VPN);
        if (user == null) prefs.remove("user");
        else prefs.put("user", user);
        if (driveW == null || driveW.equals("*")) prefs.remove("driveW");
        else prefs.put("driveW", driveW);
        if (driveDSIC == null || driveDSIC.equals("*")) prefs.remove("driveDSIC");
        else prefs.put("driveDSIC", driveDSIC);
        if (domain == Dominio.ALUMNOS) prefs.remove("non-student");
        else prefs.putBoolean("non-student", true);
    }
    
    public void clearPrefs() {
        prefs.remove("VPN");
        prefs.remove("user");
        prefs.remove("driveW");
        prefs.remove("driveDSIC");
        prefs.remove("non-student");
    }
    
    public void update() {
        if (!VPNService.isConnected()) {
            VPNService.setVPN(VPN);
        }
        if (!WService.isConnected()) {
            WService.setUser(user);
            WService.setDrive(driveW);
            WService.setDomain(domain);
        }
        if (!DSICService.isConnected()) {
            DSICService.setUser(user);
            DSICService.setDrive(driveDSIC);
            DSICService.setPass(passDSIC);
        }
    }
    
    //Getters
    public String getVPN() { return VPN; }
    public String getUser() { return user; }
    public String getDriveW() { return driveW; }
    public Dominio getDomain() { return domain; }
    public String getDriveDSIC() { return driveDSIC; }
    public String getPassDSIC() { return passDSIC; }
    public boolean getSavePrefsOnExit() { return savePrefsOnExit; }
    
    public boolean isVPNConnected() { return VPNService.isConnected(); }
    public boolean isWConnected() { return WService.isConnected(); }
    public boolean isDSICConnected() { return DSICService.isConnected(); }
    
    //Setters
    public void setVPN(String v) {
        v = v.trim();
        VPN = v.isEmpty() ? null : v;
    }
    public void setUser(String u) {
        u = u.trim();
        user = u.isEmpty() ? null : u;
    }
    protected void setDrive(AccesoDriveService serv, String d) {
        if (serv.equals(WService)) setDriveW(d);
        else if (serv.equals(DSICService)) setDriveDSIC(d);
        else throw new IllegalArgumentException("The drive service is not registered");
    }
    public void setDriveW(String d) { driveW = d; }
    public void setDriveDSIC(String d) { driveDSIC = d; }
    public void setPassDSIC(String p) {
        p = p.trim();
        passDSIC = p.isEmpty() ? null : p;
    }
    public void setDomain(Dominio d) { domain = d; }
    public void setSavePrefsOnExit(boolean b) { savePrefsOnExit = b; }
    
    //Properties
    public ReadOnlyBooleanProperty connectedVPNProperty() { return VPNService.connectedProperty(); }
    public ReadOnlyBooleanProperty connectedWProperty() { return WService.connectedProperty(); }
    public ReadOnlyBooleanProperty connectedDSICProperty() { return DSICService.connectedProperty(); }
    
    //Checks if the drive letter is currently being used (if it is "*", it is never being used, because the system picks the first available drive)
    public static boolean isDriveUsed(String drive) { return drive.equals("*") ? false : new File(drive).exists(); }
    public boolean isDriveWUsed() { return isDriveUsed(getDriveW()); }
    public boolean isDriveDSICUsed() { return isDriveUsed(getDriveDSIC()); }
    
    public List<String> getAvailableDrivesW() { return getAvailableDrives(WService); }
    public List<String> getAvailableDrivesDSIC() { return getAvailableDrives(DSICService); }
    protected List<String> getAvailableDrives(AccesoDriveService diskService) {
        List<String> drives = new ArrayList<>();
        char letter = 'Z';
        while (letter >= 'D') {
            String d = letter + ":";
            if (!new File(d).exists() 
                || (diskService.isConnected() && d.equals(diskService.getDrive()))
                ) drives.add(d);
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
    //VPN RELATED
    public boolean createVPN() {
        Task<Void> createTask = new CreateVPNTask(VPN);
        LoadingStage stage = new LoadingStage(createTask);
        stage.showAndWait();
        if (createTask.getState() == Worker.State.SUCCEEDED) {
            Alert successAlert = new Alert(Alert.AlertType.INFORMATION, "La conexión VPN (con nombre \"" + VPN + "\") ha sido creada con éxito.");
            successAlert.setHeaderText(null);
            successAlert.showAndWait();
        } else if (createTask.getState() == Worker.State.FAILED) {
            Alert failedAlert = new Alert(Alert.AlertType.ERROR, "Ha habido un error mientras se creaba la conexión VPN.");
            failedAlert.setHeaderText(null);
            failedAlert.showAndWait();
        }
        return stage.isSucceeded();
    }
    
    /**
     * @return If the operation completed successfully.
     */
    public boolean connectVPN() {
        if (VPNService.isConnected()) return true; //If the VPN is already connected, it's not necessary to connect it again
        if (VPN == null) return false;
        update();
        LoadingStage stage = new LoadingStage(VPNService);
        stage.showAndWait();
        boolean succeeded = stage.isSucceeded();
        //Si el usuario ha cancelado el proceso de conexión o desconexión a la VPN, sale del programa
        if (VPNService.getState() == Worker.State.CANCELLED) System.exit(0);
        if (!succeeded && VPNService.getException() instanceof IllegalArgumentException) {
            if (setVPNDialog(false)) return connectVPN(); //Si la VPN no era válida, permite cambiarla, y si la cambió, vuelve a intentarlo.
        }
        return succeeded;
    }
    //DIALOGS
    public boolean setVPNDialog(boolean isNew) {
        String inputContentText = (isNew)
                        ? "Introduzca el nombre de la nueva conexión VPN a la UPV: " 
                        : "Introduzca el nombre de la conexión VPN existente a la UPV: ";
        TextInputDialog dialog = new TextInputDialog(VPN);
        dialog.setTitle("Introduzca nombre VPN");
        dialog.setHeaderText(null);
        dialog.setContentText(inputContentText);
        Optional<String> res = dialog.showAndWait();
        res.ifPresent((newVPN) -> setVPN(newVPN));
        return res.isPresent();
    }
    
    public boolean setUserDialog() {
        TextInputDialog dialog = new TextInputDialog(user);
        dialog.setTitle("Introduzca usuario");
        dialog.setHeaderText(null);
        dialog.setContentText("Introduzca su usuario de la UPV: ");
        dialog.getDialogPane().setExpandableContent(new Label(AjustesController.HELP_USER_TOOLTIP));
        Optional<String> res = dialog.showAndWait();
        res.ifPresent((newUser) -> setUser(newUser));
        return res.isPresent();
    }
    public boolean setPassDialog() {
        PasswordDialog dialog = new PasswordDialog(passDSIC);
        dialog.setTitle("Introduzca contraseña");
        dialog.setHeaderText(null);
        dialog.setContentText("Introduzca su contraseña del DSIC: ");
        
        Optional<String> res = dialog.showAndWait();
        res.ifPresent((newPass) -> setPassDSIC(newPass));
        return res.isPresent();
    }
    public boolean setDriveDialogW() { return setDriveDialog(WService); }
    public boolean setDriveDialogDSIC() { return setDriveDialog(DSICService); }
    protected boolean setDriveDialog(AccesoDriveService serv) {
        List<String> drives = getAvailableDrives(serv);
        String def = drives.contains("W:") ? "W:" : null;
        ChoiceDialog<String> dialog = new ChoiceDialog<>(def, drives);
        dialog.setTitle("Elegir unidad Disco");
        dialog.setHeaderText(null);
        dialog.setContentText("Elija una unidad para su disco: ");
        Optional<String> res = dialog.showAndWait();
        res.ifPresent((newDrive) -> setDrive(serv, newDrive));
        return res.isPresent();
    }
    // DRIVE RELATED
    /**
     * Checks if the drive where W should be is already set up before this program did it.
     * @param serv
     * @return Whether the execution should continue or not.
     */
    public boolean checkDrive(AccesoDriveService serv) {
        String drive = serv.getDrive();
        if (!serv.isConnected() && isDriveUsed(drive)) {
            String WARNING_W = 
                    "La unidad definida para el disco (" + drive + ") ya contiene un disco asociado.\n\n"
                    + "Antes de continuar, desconecte el disco asociado, o cambie la unidad utilizada para el disco.\n ";
            Alert warning = new Alert(Alert.AlertType.WARNING);
            String actDrive = drive;
            warning.setHeaderText("Unidad " + actDrive + " contiene disco");
            warning.setContentText(WARNING_W);
            ButtonType retry = new ButtonType("Reintentar");
            ButtonType change = new ButtonType("Cambiar", ButtonBar.ButtonData.LEFT);
            warning.getButtonTypes().setAll(retry, change, ButtonType.CANCEL);
            Optional<ButtonType> result = warning.showAndWait();
            if (!result.isPresent() || result.get() == ButtonType.CANCEL) { return false; }
            else if (result.get() == change) { 
                if (!setDriveDialog(serv)) { return checkDrive(serv); }
            }
            else {
                if (isDriveUsed(drive)) {
                    Alert error = new Alert(Alert.AlertType.ERROR, "El disco aún no ha sido desconectado.\n"
                            + "Desconéctelo y vuelva a intentarlo.");
                    error.setHeaderText(null);
                    error.showAndWait();
                    return checkDrive(serv);
                }
            }
        }
        return true;
    }
    public boolean accessW() { return accessDrive(WService); }
    public boolean accessDSIC() {
        boolean passDefined = passDSIC != null;
        if (!passDefined) passDefined = setPassDialog();
        return passDefined ? accessDrive(DSICService) : false;
    }
    public boolean accessDrive(AccesoDriveService serv) {
        boolean succeeded = connectDrive(serv);
        if (succeeded) {
            try {
                Desktop.getDesktop().open(new File(serv.getDrive()));
            } catch (IOException ex) {
                new Alert(Alert.AlertType.WARNING, WARNING_FOLDER_DRIVE_MSG).show();
            }
        }
        return succeeded;
    }
    public boolean connectW() { return connectDrive(WService); }
    public boolean connectDSIC() {
        boolean passDefined = passDSIC == null;
        if (!passDefined) passDefined = setPassDialog();
        return passDefined ? connectDrive(DSICService) : false;
    }
    public boolean connectDrive(AccesoDriveService serv) {
        if (serv.isConnected()) return true; //If drive is already connected, it's not necessary to connect it again
        while (serv.getUser() == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "No ha especificado ningún usuario. Establezca uno.");
            alert.setHeaderText(null);
            alert.getButtonTypes().add(ButtonType.CANCEL);
            Optional<ButtonType> res = alert.showAndWait();
            if (!res.isPresent() || res.get() != ButtonType.OK) return false;
            setUserDialog();
        }
        update();
        if (!checkDrive(serv)) return false; //If the drive isn't available, abort the process
        LoadingStage stage = new LoadingStage(serv);
        stage.showAndWait();
        boolean succeeded = stage.isSucceeded();
        if (!succeeded && serv.getException() instanceof IllegalArgumentException) {
            if (setUserDialog()) return connectDrive(serv); //Si el usuario no era válido, permite cambiarlo, y si lo cambió, vuelve a intentarlo.
        }
        return succeeded;
    }
    //DISCONNECTING METHODS
    public boolean shutdown() {
        LoadingStage stage = new LoadingStage();
        List<Worker> workerList = stage.getLoadingService().getWorkerList();
        if (WService.isConnected()) { workerList.add(WService); WService.setDelay(1000); }
        if (DSICService.isConnected()) { workerList.add(DSICService); DSICService.setDelay(1000); }
        if (VPNService.isConnected()) { workerList.add(VPNService); }
        //Si tiene que realizar alguna tarea, la realiza (si no, devuelve true)
        boolean succeeded = true;
        if (!workerList.isEmpty()) {
            stage.showAndWait();
            succeeded = stage.isSucceeded();
        }
        if (succeeded && savePrefsOnExit) savePrefs();
        return succeeded;
    }
    
    public boolean disconnectW() {
        boolean succeeded = true;
        if (WService.isConnected()) {
            WService.setDelay(0);
            LoadingStage stage = new LoadingStage(WService);
            stage.showAndWait();
            succeeded = stage.isSucceeded();
        }
        return succeeded;
    }
    
    public boolean disconnectDSIC() {
        boolean succeeded = true;
        if (DSICService.isConnected()) {
            DSICService.setDelay(0);
            LoadingStage stage = new LoadingStage(DSICService);
            stage.showAndWait();
            succeeded = stage.isSucceeded();
        }
        return succeeded;
    }
    
    public boolean disconnectVPN() {
        boolean succeeded = true;
        if (VPNService.isConnected()) { 
            LoadingStage stage = new LoadingStage(VPNService);
            stage.showAndWait();
            succeeded = stage.isSucceeded();
        }
        return succeeded;
    }
}
