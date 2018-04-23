/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package accesoupv;

import accesoupv.controller.PrincipalController;
import accesoupv.model.AccesoUPV;
import accesoupv.model.LoadingTask;
import javafx.application.Application;
import javafx.concurrent.Worker;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

/**
 *
 * @author Alejandro
 */
public class Launcher extends Application {
    
    public static AccesoUPV acceso = new AccesoUPV();
    
    @Override
    public void start(Stage stage) throws Exception {
        
        FXMLLoader myLoader = new FXMLLoader(getClass().getResource("/accesoupv/view/PrincipalView.fxml"));
        Parent root = (Parent) myLoader.load();
        Scene scene = new Scene(root);
        stage.setTitle("Acceso UPV");
        stage.setScene(scene);
        stage.setOnCloseRequest((WindowEvent we) -> {
            PrincipalController principal = myLoader.<PrincipalController>getController();
            LoadingTask task = new LoadingTask();
            if (acceso.isWConnected.get()) task.addCallable(task::disconnectW);
            task.addCallable(task::disconnectVPN);
            task.setExitOnFailed(true);
            principal.gotoLoadingScreen(task);
            if (task.getState() != Worker.State.SUCCEEDED) we.consume();
        });
        stage.show();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
    
}
