/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package accesoupv.model;

import accesoupv.model.tasks.*;
import java.awt.Desktop;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.prefs.Preferences;
import javafx.concurrent.Task;
import javafx.scene.control.Alert;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
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
        Task<Void> createTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                updateMessage("Creando conexión VPN...");
                InputStream scriptIn = getClass().getResourceAsStream("/accesoupv/resources/CreateVPN.ps1");
                InputStream xmlIn = getClass().getResourceAsStream("/accesoupv/resources/My_VPN_config.xml");
                File temp = File.createTempFile("temp", ".ps1");
                File tempXml = File.createTempFile("temp", ".xml");
                //Just in case the task throws an exception, it ensures the temp files are deleted
                temp.deleteOnExit(); tempXml.deleteOnExit();
                //Copies both files
                try (Scanner sc = new Scanner(scriptIn); PrintWriter pw = new PrintWriter(new FileOutputStream(temp), true)) {
                    while (sc.hasNext()) {
                        pw.println(sc.nextLine().replaceAll("VPNNAME", vpn).replaceAll("XMLNAME", tempXml.getName()));
                    }
                }
                try (Scanner sc = new Scanner(xmlIn); PrintWriter pw = new PrintWriter(new FileOutputStream(tempXml), true)) {
                    while (sc.hasNext()) { pw.println(sc.nextLine()); }
                }
                //Runs the script and waits for its completion               
                Process p = new ProcessBuilder("powershell.exe", "-ExecutionPolicy", "ByPass", "-Command", temp.getAbsolutePath()).start();
                p.waitFor();
                //Deletes the temp files
                temp.delete(); tempXml.delete();
                return null;
            }
        };
        LoadingStage stage = new LoadingStage(createTask);
        stage.showAndWait();
        return stage.isSucceeded() && connectVPN();
    }
    
    /**
     * @return If the operation completed successfully.
     */
    public boolean connectVPN() {
        if (vpn == null) return false;
        AccesoTask task = new VPNTask(vpn, true);
        task.showErrorMessage(false);
        LoadingStage stage = new LoadingStage(task);
        stage.showAndWait();
        boolean VPNConnected = stage.isSucceeded();
        if (VPNConnected) {
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
                if (exception != null && !exception.getMessage().isEmpty()) {
                    TextArea errorContent = new TextArea(exception.getMessage());
                    errorContent.setEditable(false);
                    VBox errorVPNBox = new VBox(helpLink, errorContent);
                    errorAlert.getDialogPane().setExpandableContent(errorVPNBox);
                }
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
        stage.showAndWait();
        boolean succeeded = stage.isSucceeded();
        if (succeeded) savePrefs();
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
