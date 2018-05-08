/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package accesoupv.model.tasks;

import java.io.IOException;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;
import javafx.application.Platform;
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
    //Whether the thing it is accessing to should be connected or disconnected after the execution of the task.
    protected final boolean newState;
    
    public AccesoTask(boolean nState) {
        this(nState, true);
    }
    public AccesoTask(boolean nState, boolean showErr) {
        newState = nState;
        showError = showErr;
        setOnFailed((e) -> {
            //Platform.runLater() ensures that the Alert is being shown by the JavaFX Application Thread (avoiding possible errors).
            if (showError) Platform.runLater(() -> getErrorAlert().showAndWait());
            if (exitOnFailed) System.exit(-1);
        });
    }
    
    @Override
    protected Void call() throws Exception {
        if (newState) connect();
        else disconnect();
        return null;
    }
    
    protected abstract void connect() throws Exception;
    protected abstract void disconnect() throws Exception;
    
    //Getters
    public String getErrorMessage() { return errorMsg; }
    public Alert getErrorAlert() {
        Alert errorAlert = new Alert(Alert.AlertType.ERROR, errorMsg);
        errorAlert.setHeaderText(null);
        String errorOutput = getException().getMessage();
        if (!errorOutput.isEmpty()) {
            TextArea errorContent = new TextArea();
            errorContent.setEditable(false);
            errorAlert.getDialogPane().setExpandableContent(new VBox(errorContent));
        }
        return errorAlert;
    }
    public boolean getExitOnFailed() { return exitOnFailed; }
    
    protected void setErrorMessage(String errMsg) { errorMsg = errMsg; }
    
    //Métodos estáticos de utilidad para tratar procesos
    
    protected static void waitAndCheck(Process p, int tMin) throws Exception {
        waitAndCheck(p, tMin, true);
    }
    protected static void waitAndCheck(Process p, int tMin, boolean showExMsg) throws Exception {
        Thread.sleep(tMin);
        boolean terminated = p.waitFor(PROCESS_TIMEOUT, TimeUnit.MILLISECONDS);
        if (!terminated || p.exitValue() != 0) {
            String msg = showExMsg ? getOutput(p) : "";
            throw new IOException(msg);
        }
    }
    protected static String getOutput(Process p) throws IOException {
        String res = "";
        try (Scanner out = new Scanner(p.getInputStream())) {
            while (out.hasNext()) { res += out.nextLine() + "\n"; }
        }
        return res;
    }
    protected static Process startProcess(String... args) throws IOException {
        ProcessBuilder builder = new ProcessBuilder(args);
        builder.redirectErrorStream();
        return builder.start();
    }
}