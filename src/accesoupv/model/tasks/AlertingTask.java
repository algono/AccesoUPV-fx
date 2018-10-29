/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package accesoupv.model.tasks;

import java.util.concurrent.CountDownLatch;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;

/**
 *
 * @author Alejandro
 */
public abstract class AlertingTask extends Task<Void> {
      
    private String errorMsg;
    protected boolean showError;
    
    public AlertingTask() { this(null); }
    public AlertingTask(String errMsg) {
        super();
        if (errMsg != null) {
            showError = true;
            errorMsg = errMsg;
        }
    }
    
    @Override
    protected final Void call() throws Exception {
        try {
            doTask();
        } catch (Exception ex) {
            //Antes de que la task termine y se considere como 'failed', muestra el mensaje de error si su flag (showError) lo habilita,
            //y después vuelve a lanzar la excepción para que los demás la traten debidamente
            if (showError) {
                CountDownLatch latch = new CountDownLatch(1);
                Platform.runLater(() -> {
                    getErrorAlert(ex).showAndWait();
                    latch.countDown();
                });
                try {
                    latch.await();
                } catch (InterruptedException iEx) {
                    throw ex; //Evitamos que la InterruptedException del await tape la excepción que lanzó la propia task
                }
            }
            throw ex;
        }
        return null;
    }
    
    protected abstract void doTask() throws Exception;
            
    //Getters
    public String getErrorMessage() { return errorMsg; }   
    
    public Alert getErrorAlert() {
        return getErrorAlert(getException());
    }
    protected final Alert getErrorAlert(Throwable exception) {
        Alert errorAlert = new Alert(Alert.AlertType.ERROR, errorMsg);
        errorAlert.setHeaderText(null);
        String errorOutput = (exception == null) ? null : exception.getMessage();
        if (errorOutput != null && !errorOutput.isEmpty()) {
            TextArea errorContent = new TextArea(errorOutput);
            errorContent.setEditable(false);
            errorAlert.getDialogPane().setExpandableContent(new VBox(errorContent));
        }
        return errorAlert;
    }
    
    //Setters
    public void showErrorMessage(boolean show) { showError = show; } 
    protected void updateErrorMsg(String errMsg) { errorMsg = errMsg; }
          
}