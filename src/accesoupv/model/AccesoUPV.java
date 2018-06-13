/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package accesoupv.model;

import accesoupv.model.tasks.AccesoWService;
import accesoupv.model.tasks.CreateVPNTask;
import accesoupv.model.tasks.AccesoVPNService;
import accesoupv.controller.AjustesController;
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

/**
 *
 * @author Alejandro
 */
public final class AccesoUPV {
    
    //Variables
    private String VPN, user, drive;
    private Dominio domain; //alumnos o upvnet, depending whether it is a student or not
    
    //Preferences
    private final Preferences prefs = Preferences.userNodeForPackage(this.getClass());
    //Services
    private final AccesoVPNService VPNService;
    private final AccesoWService WService;
    //Servers
    public static final String LINUX_DSIC = "linuxdesktop.dsic.upv.es";
    public static final String WIN_DSIC = "windesktop.dsic.upv.es";
    public static final String UPV_SERVER = "www.upv.es";
    //Messages
    public static final String WARNING_FOLDER_W_MSG = 
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
        VPNService.setOnFailed((evt) -> System.exit(-1));
        WService = new AccesoWService(user, drive, domain);
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
        VPN = prefs.get("VPN", null);
        user = prefs.get("user", null);
        drive = prefs.get("drive", "W:"); //Drive by default is W:
        domain = prefs.getBoolean("non-student", false) ? Dominio.UPVNET : Dominio.ALUMNOS;
    }
    
    public void savePrefs() {
        if (VPN == null) prefs.remove("VPN");
        else prefs.put("VPN", VPN);
        if (user == null) prefs.remove("user");
        else prefs.put("user", user);
        if (drive == null || drive.equals("W:")) prefs.remove("drive");
        else prefs.put("drive", drive);
        if (domain == Dominio.ALUMNOS) prefs.remove("non-student");
        else prefs.putBoolean("non-student", true);
    }
    
    protected void clearPrefs() {
        prefs.remove("VPN");
        prefs.remove("user");
        prefs.remove("drive");
        prefs.remove("non-student");
    }
    public void reset() {
        VPN = null; user = null; drive = null; domain = Dominio.ALUMNOS;
        clearPrefs();
    }
    
    //Getters
    public String getVPN() { return VPN; }
    public String getUser() { return user; }
    public String getDrive() { return drive; }
    public Dominio getDomain() { return domain; }
    
    public boolean isVPNConnected() { return VPNService.isConnected(); }
    public boolean isWConnected() { return WService.isConnected(); }
    
    protected void checkVPN() {
        if (isVPNConnected()) {
            throw new IllegalStateException("You are not allowed to change variables related to the VPN while it is connected.");
        }
    }
    //If the W drive is connected, throw an Exception
    protected void checkW() {
        if (isWConnected()) {
            throw new IllegalStateException("You are not allowed to change variables related to the W drive while it is connected.");
        }
    }
    
    //Setters
    public void setVPN(String v) {
        checkVPN();
        v = v.trim();
        VPN = v.isEmpty() ? null : v;
        VPNService.setVPN(VPN);
    }
    public void setUser(String u) {
        checkW();
        u = u.trim();
        user = u.isEmpty() ? null : u;
        WService.setUser(user);
    }
    public void setDrive(String d) {
        checkW();
        drive = d;
        WService.setDrive(drive);
    }
    public void setDomain(Dominio d) {
        checkW();
        domain = d;
        WService.setDomain(domain);
    }
    
    //Properties
    public ReadOnlyBooleanProperty connectedVPNProperty() { return VPNService.connectedProperty(); }
    public ReadOnlyBooleanProperty connectedWProperty() { return WService.connectedProperty(); }
    
    //Checks if the drive letter is currently being used (if it is "*", it is never being used, because the system picks the first available drive)
    public boolean isDriveUsed() { return drive.equals("*") ? false : new File(drive).exists(); }
    
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
        if (isVPNConnected()) return true; //If the VPN is already connected, it's not necessary to connect it again
        if (VPN == null) return false;   
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
    public boolean setDriveDialog() {
        List<String> drives = getAvailableDrives();
        String def = drives.contains("W:") ? "W:" : null;
        ChoiceDialog<String> dialog = new ChoiceDialog<>(def, drives);
        dialog.setTitle("Elegir unidad Disco W");
        dialog.setHeaderText(null);
        dialog.setContentText("Elija una unidad para su Disco W: ");
        Optional<String> res = dialog.showAndWait();
        res.ifPresent((newDrive) -> setDrive(newDrive));
        return res.isPresent();
    }
    // DISCO W RELATED
    /**
     * Checks if the drive where W should be is set up before this program did it.
     * @return Whether the execution should continue or not.
     */
    public boolean checkDrive() {
        if (!isWConnected() && isDriveUsed()) {
            String WARNING_W = 
                    "La unidad definida para el disco W (" + drive + ") ya contiene un disco asociado.\n\n"
                    + "Antes de continuar, desconecte el disco asociado, o cambie la unidad utilizada para el disco W.\n ";
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
                if (!setDriveDialog()) { return checkDrive(); }
            }
            else {
                if (isDriveUsed()) {
                    Alert error = new Alert(Alert.AlertType.ERROR, "El disco aún no ha sido desconectado.\n"
                            + "Desconéctelo y vuelva a intentarlo.");
                    error.setHeaderText(null);
                    error.showAndWait();
                    return checkDrive();
                }
            }
        }
        return true;
    }
    
    public boolean accessW() {
        boolean succeeded = connectW();
        if (succeeded) {
            try {
                Desktop.getDesktop().open(new File(WService.getDrive()));
            } catch (IOException ex) {
                new Alert(Alert.AlertType.WARNING, WARNING_FOLDER_W_MSG).show();
            }
        }
        return succeeded;
    }
    
    public boolean connectW() {
        if (isWConnected()) return true; //If W is already connected, it's not necessary to connect it again
        else if (!checkDrive()) return false; //If the drive isn't available, abort the process
        while (user == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "No ha especificado ningún usuario. Establezca uno.");
            alert.setHeaderText(null);
            alert.getButtonTypes().add(ButtonType.CANCEL);
            Optional<ButtonType> res = alert.showAndWait();
            if (!res.isPresent() || res.get() != ButtonType.OK) return false;
            setUserDialog();
        }
        LoadingStage stage = new LoadingStage(WService);
        stage.showAndWait();
        boolean succeeded = stage.isSucceeded();
        if (!succeeded && WService.getException() instanceof IllegalArgumentException) {
            if (setUserDialog()) return connectW(); //Si el usuario no era válido, permite cambiarlo, y si lo cambió, vuelve a intentarlo.
        }
        return succeeded;
    }
    //DISCONNECTING METHODS
    public boolean shutdown() {
        LoadingStage stage = new LoadingStage();
        if (isWConnected()) stage.getQueue().add(WService);
        if (isVPNConnected()) { stage.getQueue().add(VPNService); }
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
            LoadingStage stage = new LoadingStage(WService);
            stage.showAndWait();
            succeeded = stage.isSucceeded();
        }
        return succeeded;
    }
    
    public boolean disconnectVPN() {
        boolean succeeded = true;
        if (isVPNConnected()) { 
            LoadingStage stage = new LoadingStage(VPNService);
            stage.showAndWait();
            succeeded = stage.isSucceeded();
        }
        return succeeded;
    }
}
