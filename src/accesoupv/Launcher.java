/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package accesoupv;

import accesoupv.model.AccesoUPV;
import javafx.LoadingScreen;
import accesoupv.model.LoadingTask;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
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
        stage.getIcons().add(new Image(getClass().getResourceAsStream("/accesoupv/resources/icon.png")));
        stage.setScene(scene);
        stage.setOnCloseRequest((WindowEvent we) -> {
            LoadingTask task = new LoadingTask();
            if (acceso.isWConnected()) task.addCallable(task::disconnectW);
            if (acceso.isVPNConnected()) task.addCallable(task::disconnectVPN);
            if (!task.getCallables().isEmpty()) {
                task.setExitOnFailed(true);
                boolean succeeded = new LoadingScreen(task).load();
                if (!succeeded) we.consume();
            }
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
