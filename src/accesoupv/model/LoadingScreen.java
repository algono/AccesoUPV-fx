/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package accesoupv.model;

import javafx.concurrent.Task;
import javafx.concurrent.Worker;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 *
 * @author Alejandro
 */
public class LoadingScreen extends Dialog<Boolean> {
    /**
     * 
     * @param task
     * @return If the task was completed successfully
     */
    public static boolean loadTask(Task task) {
        //Creates stage and elements
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setWidth(300);
        stage.setHeight(175);
        stage.initStyle(StageStyle.UNDECORATED);
        VBox root = new VBox();
        root.setAlignment(Pos.CENTER);
        Label state = new Label();
        state.setTextFill(Color.web("#3d81e3")); //Pone el texto de color azulado
        ProgressIndicator progress = new ProgressIndicator();
        root.getChildren().addAll(state, progress);
        root.setSpacing(10);
        //Sets bindings and listeners
        state.textProperty().bind(task.messageProperty());
        task.runningProperty().addListener((obs, oldValue, newValue) -> {
            if (!newValue) stage.hide();
        });
        //Starts the task
        new Thread(task).start();
        stage.setScene(new Scene(root, 300, 175));
        stage.showAndWait();
        //Returns if the task succeeded or not
        return task.getState() == Worker.State.SUCCEEDED;
    }
}
