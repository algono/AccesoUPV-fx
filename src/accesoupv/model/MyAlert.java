/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package accesoupv.model;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.stage.Stage;

/**
 * Just a normal alert which uses my app icon.
 * @author Alejandro
 */
public class MyAlert extends Alert {
    
    public MyAlert(AlertType alertType) {
        super(alertType);
        addAppImage();
    }
    public MyAlert(AlertType alertType, String string, ButtonType... buttonTypes) {
        super(alertType, string, buttonTypes);
        addAppImage();
    }
    
    private void addAppImage() {
        // Get the Stage.
        Stage stage = (Stage) getDialogPane().getScene().getWindow();
        // Add a custom icon.
        stage.getIcons().add(new Image(getClass().getResourceAsStream("/accesoupv/resources/app-icon.png")));
    }
}
