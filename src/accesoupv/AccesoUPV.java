/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package accesoupv;

import accesoupv.controller.PrincipalController;
import java.util.prefs.Preferences;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

/**
 *
 * @author Alejandro
 */
public class AccesoUPV extends Application {
    
    @Override
    public void start(Stage stage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/accesoupv/view/PrincipalView.fxml"));
        
        Scene scene = new Scene(root);
        stage.setTitle("Acceso UPV");
        stage.setScene(scene);
        stage.show();
        stage.setOnCloseRequest((WindowEvent we) -> {
            //Guardar las preferencias
            Preferences prefs = Preferences.userNodeForPackage(AccesoUPV.class);
            prefs.put("drive", PrincipalController.drive);
            prefs.put("vpn", PrincipalController.vpn);
            prefs.put("user", PrincipalController.user);
            //Desconectar Disco W (si estaba conectado)
            
            //Desconectar VPN
            
        });
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
    
}
