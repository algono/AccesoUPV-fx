/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package accesoupv.model;

import accesoupv.Launcher;
import accesoupv.model.services.*;
import accesoupv.model.services.drives.*;
import accesoupv.controller.AjustesController;
import accesoupv.controller.AyudaController;
import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.concurrent.Worker;
import myLibrary.javafx.Loading.LoadingStage;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.VBox;
import lib.PasswordDialog;

/**
 *
 * @author Alejandro
 */
public final class AccesoUPV {
   
    //Preferences
    private final Preferences prefs = Preferences.userNodeForPackage(Launcher.class);
    //Variables
    private String user; //I also save the user here because it is the same for all Services
    private boolean savePrefsOnExit = true;
    //Services
    private final AccesoVPNService VpnUPVService, VpnDSICService;
    private final AccesoDriveWService WService;
    private final AccesoDriveDSICService DSICService;
    //Servers
    public static final String LINUX_DSIC = "linuxdesktop.dsic.upv.es";
    public static final String WIN_DSIC = "windesktop.dsic.upv.es";
    //Messages
    public static final String WARNING_FOLDER_DRIVE_MSG = 
            "El disco ha sido conectado correctamente, pero no se ha podido abrir la carpeta.\n"
            + "Ábrala manualmente.";
    //Timeout for checking if the user is already connected to a server (in ms)
    public static final int PING_TIMEOUT = 500;
    
    
    // Singleton patron (only 1 instance of this object can be constructed)
    private static AccesoUPV acceso;
    
    //Creating a new object loads all prefs
    private AccesoUPV() {
        VpnUPVService = new AccesoVpnUPVService(prefs.get("vpn-upv", ""));
        VpnDSICService = new AccesoVpnDSICService(prefs.get("vpn-dsic", ""));
        user = prefs.get("user", "");
        WService = new AccesoDriveWService(user, prefs.get("drive-w", "*"), prefs.getBoolean("non-student", false) ? Dominio.UPVNET : Dominio.ALUMNOS);
        DSICService = new AccesoDriveDSICService(user, prefs.get("drive-dsic", "*"));
    }
    // Returns the object instance stored here
    public static AccesoUPV getInstance() {
        if (acceso == null) acceso = new AccesoUPV();
        return acceso;
    }
    //Load and save variables (prefs)
    public void loadPrefs() {
        setVpnUPV(prefs.get("vpn-upv", ""));
        setVpnDSIC(prefs.get("vpn-dsic", ""));
        setUser(prefs.get("user", ""));
        setDriveW(prefs.get("drive-w", "*"));
        setDriveDSIC(prefs.get("drive-dsic", "*"));
        setDomain(prefs.getBoolean("non-student", false) ? Dominio.UPVNET : Dominio.ALUMNOS);
    }
    public void savePrefs() {
        String VpnUPV = getVpnUPV();
        if (VpnUPV.isEmpty()) prefs.remove("vpn-upv");
        else prefs.put("vpn-upv", VpnUPV);
        String VpnDSIC = getVpnDSIC();
        if (VpnDSIC.isEmpty()) prefs.remove("vpn-dsic");
        else prefs.put("vpn-dsic", VpnDSIC);
        if (user.isEmpty()) prefs.remove("user");
        else prefs.put("user", user);
        String driveW = getDriveW();
        if (driveW.equals("*")) prefs.remove("drive-w");
        else prefs.put("drive-w", driveW);
        String driveDSIC = getDriveDSIC();
        if (driveDSIC.equals("*")) prefs.remove("drive-dsic");
        else prefs.put("drive-dsic", driveDSIC);
        if (getDomain() == Dominio.ALUMNOS) prefs.remove("non-student");
        else prefs.putBoolean("non-student", true);
    }
    
    public void clearPrefs() throws BackingStoreException {
        prefs.removeNode(); prefs.flush();
    }
    
    //Getters
    public String getVpnUPV() { return VpnUPVService.getVPN(); }
    public String getVpnDSIC() { return VpnDSICService.getVPN(); }
    public String getUser() { return user; }
    public String getDriveW() { return WService.getDrive(); }
    public Dominio getDomain() { return WService.getDomain(); }
    public String getDriveDSIC() { return DSICService.getDrive(); }
    public String getPassDSIC() { return DSICService.getPass(); }
    public boolean getSavePrefsOnExit() { return savePrefsOnExit; }
    
    public boolean isVpnUPVConnected() { return VpnUPVService.isConnected(); }
    public boolean isVpnDSICConnected() { return VpnDSICService.isConnected(); }
    public boolean isDriveWConnected() { return WService.isConnected(); }
    public boolean isDriveDSICConnected() { return DSICService.isConnected(); }
    
    //Setters
    public void setVpnUPV(String v) {
        VpnUPVService.setVPN(v.trim());
    }
    public void setVpnDSIC(String v) {
        VpnDSICService.setVPN(v.trim());
    }
    public void setUser(String u) {
        user = u.trim();
        WService.setUser(user);
        DSICService.setUser(user);
    }
    public void setDriveW(String d) { WService.setDrive(d); }
    public void setDriveDSIC(String d) { DSICService.setDrive(d); }
    public void setPassDSIC(String p) {
        DSICService.setPass(p.trim());
    }
    public void setDomain(Dominio d) { WService.setDomain(d); }
    public void setSavePrefsOnExit(boolean b) { savePrefsOnExit = b; }
    
    //Properties
    public ReadOnlyBooleanProperty connectedVpnUPVProperty() { return VpnUPVService.connectedProperty(); }
    public ReadOnlyBooleanProperty connectedVpnDSICProperty() { return VpnDSICService.connectedProperty(); }
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
    
    public static boolean isReachable(String server) {
        //Si ya es posible acceder al servidor, estamos conectados
        try {
            return ProcessUtils.startProcess("ping", "-n", "1", "-w", String.valueOf(PING_TIMEOUT), server).waitFor() == 0;
        } catch (IOException | InterruptedException ex) {
            Logger.getLogger(AccesoUPV.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }
    
    public void init() {
        AccesoVPNService serv = VpnUPVService;
        boolean succeeded = isReachable(AccesoVpnUPVService.UPV_SERVER);
        if (!succeeded) {
            if (serv.getVPN().isEmpty()) {
                if (!establishVPN(serv)) System.exit(0);
            }
            serv.setOnCancelled((evt) -> System.exit(0));
            succeeded = connectVpnUPV();
            serv.setOnCancelled(null);
        }
        if (!succeeded) {
            System.exit(-1);
    }
    }
    //CREATING METHODS
    public boolean createVpnUPV() { return create(VpnUPVService); }
    public boolean createVpnDSIC() { return create(VpnDSICService); }
    protected boolean create(Creatable cr) {
        LoadingStage stage = new LoadingStage(cr.getCreateTask());
        stage.showAndWait();
        boolean succeeded = stage.isSucceeded();
        if (succeeded) {
            Alert successAlert = new Alert(Alert.AlertType.INFORMATION, "La conexión ha sido creada con éxito.");
            successAlert.setHeaderText(null);
            successAlert.showAndWait();
        }
        return succeeded;
    }
    //CONNECTING METHODS
    protected boolean connect(AccesoService serv) {
        LoadingStage stage = new LoadingStage(serv);
        stage.showAndWait();
        return stage.isSucceeded();
    }
    //VPN RELATED 
    /**
     * @param serv
     * @return If the operation completed successfully.
     */
    protected boolean connectVPN(AccesoVPNService serv) {
        if (serv.isConnected()) return true; //If the VPN is already connected, it's not necessary to connect it again
        if (serv.getVPN().isEmpty()) {
            if (!establishVPN(serv)) return false;
        }
        boolean succeeded = connect(serv);        
        //Si la ejecución falló, permite cambiar el valor de la VPN por si lo puso mal
        if (!succeeded) { 
            if (setVPNDialog(serv, false)) return connectVPN(serv); //Si la VPN no era válida, permite cambiarla, y si la cambió, vuelve a intentarlo.
            else {
                if (establishVPN(serv)) return connectVPN(serv);
                else return false;
            }
        }
        return succeeded;
    }
    public boolean connectVpnUPV() {
        return isReachable(AccesoVpnUPVService.UPV_SERVER) ? true : connectVPN(VpnUPVService);
    }
    public boolean connectVpnDSIC() {
        //Por algún extraño motivo, usar || no funciona bien en esta situación, así que la salvamos así
        return isReachable(AccesoVpnDSICService.PORTAL_DSIC_WEB) ? true : connectVPN(VpnDSICService); 
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
    
    public boolean connectW() { 
        if (!checkUser()) return false;
        boolean succeeded = connectDrive(WService);
        if (!succeeded) {
            Throwable ex = WService.getException();
            if (ex != null) {
                ex = ex.getCause();
                if (ex instanceof IllegalArgumentException) {
                    if (setUserDialog()) return connectW(); //Si el usuario no era válido, permite cambiarlo, y si puso alguno, vuelve a intentarlo.
                }
            }
        }
        return succeeded;
    }
    public boolean connectDSIC() {
        if (!checkUser()) return false;
        boolean passDefined = !getPassDSIC().isEmpty();
        if (!passDefined) passDefined = setPassDialog();
        if (!passDefined) return false;
        boolean succeeded = connectDrive(DSICService);
        if (!succeeded) {
            Throwable ex = DSICService.getException();
            if (ex != null) {
                ex = ex.getCause();
                if (ex instanceof IllegalArgumentException) {
                    //Si algún dato no era válido, permite cambiarlo, y si puso los datos, vuelve a intentarlo.
                    if (setUserDialog() && setPassDialog()) return connectDSIC();
                }
            }
        }
        return succeeded;
    }
    
    protected boolean checkUser() {
        boolean userDefined = !user.isEmpty();
        if (!userDefined) userDefined = setUserDialog();
        return userDefined;
    }
    
    protected boolean connectDrive(AccesoDriveService serv) {
        boolean connected = serv.isConnected();
        if (!connected) {//If drive is already connected, it's not necessary to connect it again
            if (!checkDrive(serv)) return false; //If the drive isn't available, abort the process
            connected = connect(serv);
        }
        if (connected) {
            try {
                Desktop.getDesktop().open(new File(serv.getDrive()));
            } catch (IOException ex) {
                new Alert(Alert.AlertType.WARNING, WARNING_FOLDER_DRIVE_MSG).show();
            }
        }
        
        return connected;
    }
    //DISCONNECTING METHODS
    public boolean shutdown() {
        LoadingStage stage = new LoadingStage();
        List<Worker> workerList = stage.getLoadingService().getWorkerList();
        AccesoService[] services = {WService, DSICService, VpnDSICService, VpnUPVService};
        for (AccesoService serv : services) if (serv.isConnected()) workerList.add(serv);
        if (WService.isConnected()) WService.setDelay(1000);
        if (DSICService.isConnected()) DSICService.setDelay(1000);
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
        if (WService.isConnected()) WService.setDelay(0);
        else return true;
        return disconnect(WService);
    }
    
    public boolean disconnectDSIC() {
        if (DSICService.isConnected()) DSICService.setDelay(0);
        else return true;
        return disconnect(DSICService);
    }
    public boolean disconnectVpnUPV() { return disconnect(VpnUPVService); }
    public boolean disconnectVpnDSIC() { return disconnect(VpnDSICService); }
    protected boolean disconnect(AccesoService serv) {
        boolean succeeded = true;
        if (serv.isConnected()) { 
            LoadingStage stage = new LoadingStage(serv);
            stage.showAndWait();
            succeeded = stage.isSucceeded();
        }
        return succeeded;
    }
    
    //DIALOGS
    protected boolean establishVPN(AccesoVPNService serv) {
        boolean hasNewVPN = false;
        while (!hasNewVPN) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setHeaderText(null);
            String warningText = "Debe establecer una red VPN para poder acceder a"
                    + (serv.equals(VpnUPVService) ? " la UPV desde fuera del campus." : "l portal DSIC.");
            Label content = new Label(warningText + "\n\n"
                    + "¿Desea que se cree la VPN automáticamente?\n\n"
                    + "Si ya tiene una creada o prefiere crearla manualmente, elija \"No\".");
            Hyperlink help = new Hyperlink("Para saber cómo crear una VPN manualmente, pulse aquí");
            help.setOnAction((evt) -> Platform.runLater(() -> AyudaController.getInstance().getStage().show()));
            alert.getDialogPane().setContent(new VBox(content, help));
            alert.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO,ButtonType.CANCEL);
            Optional<ButtonType> alertRes = alert.showAndWait();

            //Si el usuario canceló o cerró el diálogo, sale
            if (!alertRes.isPresent() || alertRes.get() == ButtonType.CANCEL) return false;

            //Si pulsó que sí, se debe crear una VPN nueva. Si pulsó que no, se debe introducir una existente.
            boolean setNewVPN = alertRes.get() == ButtonType.YES;
            hasNewVPN = setVPNDialog(serv, setNewVPN);

            //Si la VPN ha sido cambiada (y es nueva), la crea.
            if (hasNewVPN && setNewVPN) create(serv);
        }
        return true;
    }    
    public boolean setVPNDialog(AccesoVPNService serv, boolean isNew) {
        String inputContentText = (isNew)
                        ? "Introduzca el nombre de la nueva conexión VPN a la UPV: " 
                        : "Introduzca el nombre de la conexión VPN existente a la UPV: ";
        TextInputDialog dialog = new TextInputDialog(serv.getVPN());
        dialog.setTitle("Introduzca nombre VPN");
        dialog.setHeaderText(null);
        dialog.setContentText(inputContentText);
        Optional<String> res = dialog.showAndWait();
        res.ifPresent((newVPN) -> serv.setVPN(newVPN));
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
        PasswordDialog dialog = new PasswordDialog(getPassDSIC());
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
        res.ifPresent((newDrive) -> serv.setDrive(newDrive));
        return res.isPresent();
    }
}
