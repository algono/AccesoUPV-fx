/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package accesoupv.model;

import java.io.IOException;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;
import javafx.concurrent.Task;
import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;

/**
 *
 * @author Alejandro
 */
public abstract class AccesoTask extends Task<Void> {
    
    //Timeout
    public static final int PING_TIMEOUT = 500; //500 miliseconds
    public static final int PROCESS_TIMEOUT = 5000; //5 seconds
    
    private String errorMsg;
    private boolean showError;
    protected boolean exitOnFailed = false;
    
    public AccesoTask() {
        this(true);
    }
    public AccesoTask(boolean showErr) {
        showError = showErr;
        setOnFailed((e) -> {
            if (showError) getErrorAlert().showAndWait();
            if (exitOnFailed) System.exit(-1);
        });
    }
    //Getters
    public String getErrorMessage() { return errorMsg; }
    public Alert getErrorAlert() {
        Alert errorAlert = new Alert(Alert.AlertType.ERROR, errorMsg);
        errorAlert.setHeaderText(null);
        TextArea errorContent = new TextArea(getException().getMessage());
        errorContent.setEditable(false);
        errorAlert.getDialogPane().setExpandableContent(new VBox(errorContent));
        return errorAlert;
    }
    public boolean getExitOnFailed() { return exitOnFailed; }
    
    protected void setErrorMessage(String errMsg) { errorMsg = errMsg; }
    
    //Métodos estáticos de utilidad para tratar procesos
    protected static void waitAndCheck(Process p, int tMin) throws Exception {
        Thread.sleep(tMin);
        boolean terminated = p.waitFor(PROCESS_TIMEOUT, TimeUnit.MILLISECONDS);
        if (!terminated || p.exitValue() != 0) {
            throw new IOException(getOutput(p));
        }
    }
    protected static String getOutput(Process p) throws IOException {
        String res = "";
        try (Scanner out = new Scanner(p.getInputStream())) {
            while (out.hasNext()) { res += out.nextLine() + "\n"; }
        }
        return res;
    }
    protected static String getError(Process p) throws IOException {
        String res = "";
        try (Scanner err = new Scanner(p.getErrorStream())) {
            while (err.hasNext()) { res += err.nextLine() + "\n"; }
        }
        return res;
    }
    protected static Process startProcess(String... args) throws IOException {
        ProcessBuilder builder = new ProcessBuilder(args);
        builder.redirectErrorStream();
        return builder.start();
    }
}