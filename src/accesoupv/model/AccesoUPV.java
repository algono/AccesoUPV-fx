/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package accesoupv.model;

import accesoupv.model.services.vpn.AccesoVpnDSICService;
import accesoupv.model.services.vpn.AccesoVpnUPVService;
import accesoupv.model.services.vpn.AccesoVPNService;
import accesoupv.Launcher;
import accesoupv.model.services.*;
import accesoupv.model.services.drives.*;
import accesoupv.controller.AjustesController;
import accesoupv.controller.AyudaController;
import java.awt.Desktop;
import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.concurrent.Worker;
import myLibrary.javafx.Loading.LoadingDialog;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.VBox;
import lib.PasswordDialog;
import myLibrary.javafx.AlertingTask;

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
    public static final String DISCA_SSH = "home-labs.disca.upv.es", KAHAN_SSH = "kahan.dsic.upv.es";
    
    //Timeout for checking if the user is already connected to a server (in ms)
    public static final int PING_TIMEOUT = 600;
    
    
    // Singleton patron (only 1 instance of this object can be constructed)
    private static AccesoUPV acceso;
    
    //Creating a new object loads all prefs
    private AccesoUPV() {
        VpnUPVService = new AccesoVpnUPVService(prefs.get("vpn-upv", ""));
        VpnDSICService = new AccesoVpnDSICService(prefs.get("vpn-dsic", ""));
        user = prefs.get("user", "");
        WService = new AccesoDriveWService(user, prefs.get("drive-w", "*"), prefs.getBoolean("non-student", false) ? Dominio.UPVNET : Dominio.ALUMNO);
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
        setDomain(prefs.getBoolean("non-student", false) ? Dominio.UPVNET : Dominio.ALUMNO);
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
        if (getDomain() == Dominio.ALUMNO) prefs.remove("non-student");
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
    
    public List<String> getAvailableDrivesW() { return WService.getAvailableDrives(); }
    public List<String> getAvailableDrivesDSIC() { return DSICService.getAvailableDrives(); }
    
    
    public void init() {
        AccesoVPNService serv = VpnUPVService;
        switch(checkVPN(serv)) {
            case CANCEL: return;
            case ABORT: System.exit(0);
            default : break;
        }
        serv.setOnCancelled((evt) -> System.exit(0));
        
        LoadingDialog dialog = new LoadingDialog(serv);
        dialog.showAndWait();
        boolean succeeded = dialog.isSucceeded();
        
        serv.setOnCancelled(null);
        //Si la ejecucion fall, permite cambiar el valor de la VPN por si lo puso mal
        if (!succeeded && serv.getState() == Worker.State.FAILED) { 
            switch(onFailedVPN(serv)) {
                case RETRY: init(); break;
                case ABORT: System.exit(-1);
                default: break;
            }
        }
    }
    
    //CREATING METHODS
    public boolean createVpnUPV() { return createVPN(VpnUPVService); }
    public boolean createVpnDSIC() { return createVPN(VpnDSICService); }
    protected static boolean createVPN(AccesoVPNService serv) {
        LoadingDialog dialog = new LoadingDialog(serv.getCreateTask());
        dialog.showAndWait();
        boolean succeeded = dialog.isSucceeded();
        if (succeeded) {
            Alert successAlert = new Alert(Alert.AlertType.INFORMATION, "La conexion ha sido creada con Ã©xito.");
            successAlert.setHeaderText(null);
            successAlert.showAndWait();
        }
        return succeeded;
    }
    //CONNECTING METHODS
    //VPN RELATED
    protected Operation checkVPN(AccesoVPNService serv) {
        //If the VPN is already connected (or is reachable), it's not necessary to connect it.
        if (serv.isConnected() || serv.isReachable()) return Operation.CANCEL;
        //If the VPN is not set and the establishVPN dialogue doesn't set it, it aborts.
        else if (serv.getVPN().isEmpty() && !establishVPN(serv)) return Operation.ABORT;
        else return Operation.CONTINUE;
    }
    protected Operation onFailedVPN(AccesoVPNService serv) {
        //Si la VPN no era vÃ¡lida, permite cambiarla, y si la cambio, vuelve a intentarlo.
        if (setVPNDialog(serv, false)) return Operation.RETRY;
        else {
            if (establishVPN(serv)) return Operation.RETRY;
            else return Operation.ABORT;
        }
    }
    protected boolean connectVPN(AccesoVPNService serv) {
        switch(checkVPN(serv)) {
            case CANCEL: return true;
            case ABORT: return false;
            default : break;
        }
        LoadingDialog dialog = new LoadingDialog(serv);
        dialog.showAndWait();
        boolean succeeded = dialog.isSucceeded();        
        //Si la ejecucion fallo, permite cambiar el valor de la VPN por si lo puso mal
        if (!succeeded && serv.getState() == Worker.State.FAILED) { 
            switch(onFailedVPN(serv)) {
                case RETRY: return connectVPN(serv);
                case ABORT: return false;
                default: break;
            }
        }
        return succeeded;
    }
    public boolean connectVpnUPV() {
        return connectVPN(VpnUPVService);
    }
    public boolean connectVpnDSIC() {
        if (WService.isConnected()) {
            Alert a = new Alert(Alert.AlertType.WARNING, 
                "No se puede acceder a la VPN del DSIC teniendo el Disco W conectado.\n"
                + "Si continÃºa, este serÃ¡ desconectado automÃ¡ticamente.\n\n"
                + "Â¿Desea continuar?", ButtonType.OK, ButtonType.CANCEL);
            a.setHeaderText(null);
            Optional<ButtonType> res = a.showAndWait();
            if (res.isPresent() && res.get() == ButtonType.OK) {
                if (!disconnectW()) return false;
            } else return false;
        }
        return connectVPN(VpnDSICService);
    }

    // DRIVE RELATED
    /**
     * Checks if the drive letter where the drive is going to be is already set up before this program did it.
     * @param serv
     * @return Whether the execution should continue or not.
     */
    public boolean checkDrive(AccesoDriveService serv) {
        String drive = serv.getDrive();
        if (!serv.isConnected() && isDriveUsed(drive)) {
            String WARNING_W = 
                    "La unidad definida para el disco (" + drive + ") ya contiene un disco asociado.\n\n"
                    + "Antes de continuar, desconecte el disco asociado, o cambie la unidad utilizada para el disco.\n "
                    + "(Si prefiere que se elija la primera unidad disponible solo durante esta conexion, continÃºe).\n ";
            Alert warning = new Alert(Alert.AlertType.WARNING);
            String actDrive = drive;
            warning.setHeaderText("Unidad " + actDrive + " contiene disco");
            warning.setContentText(WARNING_W);
            ButtonType retry = new ButtonType("Continuar");
            ButtonType change = new ButtonType("Cambiar", ButtonBar.ButtonData.LEFT);
            warning.getButtonTypes().setAll(retry, change, ButtonType.CANCEL);
            Optional<ButtonType> result = warning.showAndWait();
            if (!result.isPresent() || result.get() == ButtonType.CANCEL) { return false; }
            else if (result.get() == change) { 
                if (!setDriveDialog(serv)) { return checkDrive(serv); }
            }
        }
        return true;
    }
    
    public boolean connectW() { 
        if (!checkUser()) return false;
        if (VpnDSICService.isConnected()) {
            Alert a = new Alert(Alert.AlertType.WARNING, 
                "No se puede acceder al disco W desde la VPN del DSIC.\n"
                + "Si continÃºa, esta serÃ¡ desconectada automÃ¡ticamente.\n\n"
                + "Â¿Desea continuar?", ButtonType.OK, ButtonType.CANCEL);
            a.setHeaderText(null);
            Optional<ButtonType> res = a.showAndWait();
            if (res.isPresent() && res.get() == ButtonType.OK) {
                if (!disconnectVpnDSIC()) return false;
            } else return false;
        }
        boolean succeeded = connectDrive(WService);
        if (!succeeded) {
            Throwable ex = WService.getException();
            if (ex != null) {
                ex = ex.getCause();
                if (ex instanceof IllegalArgumentException) {
                    if (setUserDialog()) return connectW(); //Si el usuario no era vÃ¡lido, permite cambiarlo, y si puso alguno, vuelve a intentarlo.
                } else if (ex.getMessage().equals("bug") && VpnUPVService.isConnected()) {
                    if (disconnectVpnUPV()) {
                        boolean rec = false;
                        while (!rec) {
                            rec = connectVpnUPV();
                            if (!rec) {
                                if (VpnUPVService.getState() == Worker.State.CANCELLED) {
                                    Alert a = new Alert(Alert.AlertType.WARNING, "No puede continuar con la VPN desconectada.\n"
                                            + "Â¿Desea salir del programa?", ButtonType.OK, ButtonType.CANCEL);
                                    a.setHeaderText(null);
                                    Optional<ButtonType> res = a.showAndWait();
                                    if (!res.isPresent() || res.get() != ButtonType.OK) System.exit(0);
                                }
                            }
                        }
                        connectW();
                    }
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
                    //Si algÃºn dato no era vÃ¡lido, permite cambiarlo, y si puso los datos, vuelve a intentarlo.
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
        String drive = serv.getDrive();
        LoadingDialog dialog = new LoadingDialog();
        List<Worker<?>> workerList = dialog.getLoadingService().getWorkerList();
        //If drive is already connected, it's not necessary to connect it again
        if (!connected) {
            //If the drive isn't available, abort the process
            if (!checkDrive(serv)) return false;
            //If after checking the drive is still used, just use the first available
            if (isDriveUsed(drive)) serv.setDrive("*");
            
            workerList.add(serv);  
        }
        //Opening drive folder task
        AlertingTask<Boolean> openTask = new AlertingTask<Boolean>(
                                "El disco ha sido conectado correctamente, pero no se ha podido abrir.\n"
                                + "Abralo manualmente desde el Explorador de Archivos.") {
            @Override
            protected Boolean call() throws Exception {
                updateMessage("Abriendo disco...");
                Desktop.getDesktop().open(new File(serv.getConnectedDrive()));
                return true;
            }
            @Override
            protected Alert getErrorAlert(Throwable ex) {
                Logger.getLogger(AccesoUPV.class.getName()).log(Level.SEVERE, null, ex);
                return new Alert(Alert.AlertType.WARNING, getErrorMessage());
            }
        };
        workerList.add(openTask);
        dialog.showAndWait();
        
        serv.setDrive(drive); //If the drive was changed while connecting, change it back
        return dialog.isSucceeded();
    }
    //DISCONNECTING METHODS
    public boolean shutdown() {
        LoadingDialog dialog = new LoadingDialog();
        dialog.setProgressBar(true);
        dialog.showProgressNumber(true);
        List<Worker<?>> workerList = dialog.getLoadingService().getWorkerList();
        AccesoService[] services = {WService, DSICService, VpnDSICService, VpnUPVService};
        for (AccesoService serv : services) if (serv.isConnected()) workerList.add(serv);
        //Si tiene que realizar alguna tarea, la realiza (si no, devuelve true)
        boolean succeeded = true;
        if (!workerList.isEmpty()) {
            dialog.showAndWait();
            succeeded = dialog.isSucceeded();
        }
        if (succeeded && savePrefsOnExit) savePrefs();
        return succeeded;
    }
    
    public boolean disconnectW() { return disconnect(WService); }
    public boolean disconnectDSIC() { return disconnect(DSICService); }
    public boolean disconnectVpnUPV() { return disconnect(VpnUPVService); }
    public boolean disconnectVpnDSIC() { return disconnect(VpnDSICService); }
    
    protected boolean disconnect(AccesoService serv) {
        boolean succeeded = true;
        if (serv.isConnected()) { 
            LoadingDialog dialog = new LoadingDialog(serv);
            dialog.showAndWait();
            succeeded = dialog.isSucceeded();
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
                    + "Â¿Desea que se cree la VPN automÃ¡ticamente?\n\n"
                    + "Si ya tiene una creada o prefiere crearla manualmente, elija \"No\".");
            Hyperlink help = new Hyperlink("Para saber como crear una VPN manualmente, pulse aquÃ­");
            help.setOnAction((evt) -> {
                AyudaController ayuda = AyudaController.getInstance();
                ayuda.getStage().show();
                for (TitledPane t : ayuda.getIndex().getPanes()) {
                    //Comprueba si contiene "crear" y "vpn" en ese orden, con lo que sea en medio
                    // (. = Cualquier carÃ¡cter) (* = Cualquier nÃºmero de veces)
                    if (t.getText().toLowerCase().matches(".*crear.*vpn.*")) {
                        t.setExpanded(true); break; //En cuanto lo encuentra lo maximiza y sale del bucle
                    }
                }
            });
            alert.getDialogPane().setContent(new VBox(content, help));
            alert.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO,ButtonType.CANCEL);
            Optional<ButtonType> alertRes = alert.showAndWait();

            //Si el usuario cancelo o cerro el diÃ¡logo, sale
            if (!alertRes.isPresent() || alertRes.get() == ButtonType.CANCEL) return false;

            //Si pulso que sÃ­, se debe crear una VPN nueva. Si pulso que no, se debe introducir una existente.
            boolean setNewVPN = alertRes.get() == ButtonType.YES;
            hasNewVPN = setVPNDialog(serv, setNewVPN);

            //Si la VPN ha sido cambiada (y es nueva), la crea.
            if (hasNewVPN && setNewVPN) createVPN(serv);
        }
        return true;
    }    
    public boolean setVPNDialog(AccesoVPNService serv, boolean isNew) {
        String inputContentText = (isNew)
                        ? "Introduzca el nombre de la nueva conexion VPN: " 
                        : "Introduzca el nombre de la conexion VPN existente: ";
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
    	// \u00f1 = Letter of 'espaNa'
        PasswordDialog dialog = new PasswordDialog(getPassDSIC());
        dialog.setTitle("Introduzca contrase\u00f1a");
        dialog.setHeaderText(null);
        dialog.setContentText("Introduzca su contrase\u00f1a del DSIC: ");
        
        Optional<String> res = dialog.showAndWait();
        res.ifPresent((newPass) -> setPassDSIC(newPass));
        return res.isPresent();
    }
    public boolean setDriveDialogW() { return setDriveDialog(WService); }
    public boolean setDriveDialogDSIC() { return setDriveDialog(DSICService); }
    protected boolean setDriveDialog(AccesoDriveService serv) {
        List<String> drives = serv.getAvailableDrives();
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
