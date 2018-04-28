/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package accesoupv.model;

import static accesoupv.Launcher.acceso;
import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.Callable;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.control.Alert;

/**
 *
 * @author Alejandro
 */
public class LoadingTask extends Task<Void> {
    //Messages
    public static final String ERROR_VPN = "Ha habido un error al tratar de conectarse a la UPV. Inténtelo de nuevo más tarde.";
    public static final String ERROR_DIS_VPN = "No ha podido desconectarse la VPN. Deberá desconectarla manualmente.";
    public static final String ERROR_W = "Ha habido un error al tratar de conectarse al disco W. Inténtelo de nuevo más tarde.";
    public static final String ERROR_DIS_W = "Ha habido un error al desconectar el disco W. Deberá desconectarlo manualmente.";
    public static final String ERROR_OPENED_DIS_W = "Tiene abierto un archivo/carpeta del disco W. Cierre todos los archivos e inténtelo de nuevo.";
    public static final String ERROR_INVALID_VPN = "No existe ninguna VPN creada con el nombre registrado.\nDebe establecer un nombre válido.";
    public static final String ERROR_INVALID_USER = "No existe el usuario especificado.\nDebe establecer un usuario válido.";
    //Timeout
    public static final int TIMEOUT = 500;
    //Callable method
    private final List<Callable<Void>> callables;
    private String errorMsg;
    private boolean exitOnFailed;
    
    public LoadingTask(Callable<Void>... c) {
        callables = new ArrayList<>(Arrays.asList(c));
        exitOnFailed = false;
        errorMsg = "";
        setOnFailed((e) -> {
            getOutputAlert().showAndWait();
            if (exitOnFailed) {
                Platform.exit();
                System.exit(-1);
            }
        });
    }
    //Getters
    public synchronized List<Callable<Void>> getCallables() { return callables; }
    public synchronized String getOutputMessage() { return errorMsg; }
    public synchronized Alert getOutputAlert() {
        Alert errorAlert = new Alert(Alert.AlertType.ERROR, errorMsg);
        errorAlert.setHeaderText(null);
        return errorAlert;
    }
    public synchronized boolean getExitOnFailed() { return exitOnFailed; }
    //Setters
    public synchronized void addCallable(Callable<Void> c) { callables.add(c); }
    public synchronized void addCallables(Callable<Void>... c) { callables.addAll(Arrays.asList(c)); }
    public synchronized void setErrorMessage(String msg) { errorMsg = msg; }
    public synchronized void setExitOnFailed(boolean b) { exitOnFailed = b; }
    
    @Override
    protected synchronized Void call() throws Exception {
        for (Callable c : callables) { c.call(); }
        return null;
    }
    
    protected void waitAndCheck(Process p, int tMin) throws Exception {
        Thread.sleep(tMin);
        int exitValue = p.waitFor();
        if (exitValue != 0) {
            throw new IOException(getOutput(p));
        }
    }
    protected String getOutput(Process p) throws IOException {
        String res = "";
        try (Scanner out = new Scanner(p.getInputStream());
                Scanner err = new Scanner(p.getErrorStream())) {
            if (out.hasNext()) res += "Output:\n";
            while (out.hasNext()) { res += out.nextLine() + "\n"; }
            if (err.hasNext()) res += "Error:\n";
            while (err.hasNext()) { res += err.nextLine() + "\n"; }
        }
        return res;
    }
    //Tareas posibles para LoadingTask
    public Void connectVPN() throws Exception {
        setErrorMessage(ERROR_VPN);
        updateMessage("Conectando con la UPV...");
        Process p = new ProcessBuilder("cmd.exe", "/c", "rasdial \"" + acceso.getVPN() + "\"").start();
        Thread.sleep(1000);
        int exitValue = p.waitFor();
        if (exitValue != 0) {
            String err = getOutput(p);
            //623 - Código de error "No se encontró una VPN con ese nombre".
            if (err.contains("623")) {
                setErrorMessage(ERROR_INVALID_VPN);
                throw new IllegalArgumentException();
            } else {
                throw new IOException();
            }
        } else {
            //Si desde la VPN a la que se conectó no se puede acceder a la UPV, se entiende que ha elegido una incorrecta.
            try {
                if (!InetAddress.getByName("www.upv.es").isReachable(TIMEOUT)) {
                    disconnectVPN();
                    throw new IllegalArgumentException();
                }
            } catch (IOException ex) {}
        }
        return null;
    }
    public Void accessW() throws Exception {
        setErrorMessage(ERROR_W);
        updateMessage("Accediendo al disco W...");
        String drive = acceso.getDrive();
        Process p = new ProcessBuilder("cmd.exe", "/c", "net use " + drive + " " + acceso.getDirW()).start();
        Thread.sleep(1000);
        int exitValue = p.waitFor();
        if (exitValue != 0) {
            String out = getOutput(p);
            // 55 - Error del sistema "El recurso no se encuentra disponible" (es decir, la dirW no existe, por tanto, el usuario no es válido).
            if (out.contains("55")) {
                setErrorMessage(ERROR_INVALID_USER);
                throw new IllegalArgumentException();
            } else {
                throw new IOException();
            }
        }
        return null;
    }
    //Desconectar Disco W (si estaba conectado)
    public Void disconnectW() throws Exception {
        setErrorMessage(ERROR_DIS_W);
        updateMessage("Desconectando Disco W...");
        Process p = new ProcessBuilder("cmd.exe", "/c", "net use " + acceso.getDrive() + " /delete").start();
        Thread.sleep(1000);
        String out = getOutput(p);
        if (out.contains("archivos abiertos")) {
            setErrorMessage(ERROR_OPENED_DIS_W);
            throw new IOException();
        }
        int exitValue = p.waitFor();
        if (exitValue != 0) throw new IOException(getOutput(p));
        return null;
    }
    //Desconectar VPN
    public Void disconnectVPN() throws Exception {
        setErrorMessage(ERROR_DIS_VPN);
        updateMessage("Desconectando de la UPV...");
        Process p = new ProcessBuilder("cmd.exe", "/c", "rasdial " + acceso.getVPN() + " /DISCONNECT").start();
        waitAndCheck(p, 1000);
        return null;
    }
}