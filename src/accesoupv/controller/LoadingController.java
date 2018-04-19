/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package accesoupv.controller;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * FXML Controller class
 *
 * @author Alejandro
 */
public class LoadingController implements Initializable {

    @FXML
    private Label textState;
    
    //Stage
    private Stage primaryStage;
    //Task this loading screen is for
    private Task task;
    
    public void init(Stage stage, Task t) {
        primaryStage = stage;
        task = t;
        primaryStage.setTitle("Acceso UPV");
        //remove window decoration
        primaryStage.initStyle(StageStyle.UNDECORATED);
        textState.textProperty().bind(task.messageProperty());
        task.runningProperty().addListener((obs, oldValue, newValue) -> {
            if (!newValue) primaryStage.hide();
        });
        new Thread(task).start();
    }
    
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        //TODO
    }    
    
}