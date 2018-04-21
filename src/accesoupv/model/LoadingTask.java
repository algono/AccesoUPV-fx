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
    //Timeout
    public static final int TIMEOUT = 3000;
    //Callable method
    private final List<Callable<Void>> callables;
    private String errorMsg;
    private boolean exitOnFailed;
    
    public LoadingTask(Callable<Void>... c) {
        callables = new ArrayList<>(Arrays.asList(c));
        exitOnFailed = false;
        errorMsg = "";
        setOnFailed((e) -> {
            getErrorAlert().showAndWait();
            if (exitOnFailed) {
                Platform.exit();
                System.exit(-1);
            }
        });
    }
    //Getters
    public List<Callable<Void>> getCallables() { return callables; }
    public String getErrorMessage() { return errorMsg; }
    public Alert getErrorAlert() {
        Alert errorAlert = new Alert(Alert.AlertType.ERROR, errorMsg);
        errorAlert.setHeaderText(null);
        return errorAlert;
    }
    public boolean getExitOnFailed() { return exitOnFailed; }
    //Setters
    public void addCallable(Callable<Void> c) { callables.add(c); }
    public void addCallables(Callable<Void>... c) { callables.addAll(Arrays.asList(c)); }
    public void setErrorMessage(String msg) { errorMsg = msg; }
    
    protected void waitAndCheck(Process p) throws Exception {
        Thread.sleep(1000);
        p.waitFor();
        checkError(p);
    }
    protected void checkError(Process p) throws IOException {
        try (Scanner sc = new Scanner(p.getErrorStream())) {
            String s = "";
            while (sc.hasNext()) {
                s += sc.nextLine() + "\n";
            }
            if (!s.isEmpty()) throw new IOException(s);
        }
    }
    
    @Override
    protected Void call() throws Exception {
        for (Callable c : callables) { c.call(); }
        return null;
    }
    //Tareas posibles para LoadingTask
    public Void connectVPN() throws Exception {
        setErrorMessage(ERROR_VPN);
        exitOnFailed = true;
        updateMessage("Conectando con la UPV...");
        Process p = new ProcessBuilder("cmd.exe", "/c", "rasdial " + acceso.getVPN()).start();
        waitAndCheck(p);
        if (!InetAddress.getByName("www.upv.es").isReachable(TIMEOUT)) {
            throw new IOException();
        }
        return null;
    }
    public Void accessW() throws Exception {
        setErrorMessage(ERROR_W);
        exitOnFailed = false;
        updateMessage("Accediendo al disco W...");
        String drive = acceso.getDrive();
        Process p = new ProcessBuilder("cmd.exe", "/c", "net use " + drive + " " + acceso.getDirW()).start();
        waitAndCheck(p);
        if (!acceso.isWConnected()) throw new IOException();
        return null;
    }
    //Desconectar Disco W (si estaba conectado)
    public Void disconnectW() throws Exception {
        setErrorMessage(ERROR_DIS_W);
        exitOnFailed = false;
        if (acceso.isWConnected()) {
            updateMessage("Desconectando Disco W...");
            Process p = new ProcessBuilder("cmd.exe", "/c", "net use " + acceso.getDrive() + " /delete").start();
            Thread.sleep(1000);
            try(Scanner sc = new Scanner(p.getInputStream())) {
                if (sc.hasNext() && acceso.isWConnected()) {
                    setErrorMessage(ERROR_OPENED_DIS_W);
                    throw new IOException();
                }
            }
            p.waitFor();
            checkError(p);
            if (acceso.isWConnected()) throw new IOException();
        }
        return null;
    }
    //Desconectar VPN
    public Void disconnectVPN() throws Exception {
        setErrorMessage(ERROR_DIS_VPN);
        exitOnFailed = true;
        updateMessage("Desconectando de la UPV...");
        Process p = new ProcessBuilder("cmd.exe", "/c", "rasdial " + acceso.getVPN() + " /DISCONNECT").start();
        waitAndCheck(p);
        return null;
    }
}