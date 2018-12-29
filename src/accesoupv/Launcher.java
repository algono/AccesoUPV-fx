/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package accesoupv;

import accesoupv.controller.PrincipalController;
import accesoupv.model.AccesoUPV;
import javafx.application.Application;
import javafx.application.Platform;
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
    
    @Override
    public void start(Stage stage) throws Exception {
        
        FXMLLoader myLoader = new FXMLLoader(getClass().getResource("/accesoupv/view/PrincipalView.fxml"));
        Parent root = (Parent) myLoader.load();
        stage.setResizable(false);
        Scene scene = new Scene(root);
        
        stage.setTitle("Acceso UPV");
        stage.getIcons().add(new Image(getClass().getResourceAsStream("/accesoupv/resources/icons/app-icon.png")));       
        stage.setScene(scene);
        
        stage.setOnCloseRequest((WindowEvent we) -> {
            if (AccesoUPV.getInstance().shutdown()) Platform.exit(); 
            else we.consume();
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
