/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package accesoupv.model;

import javafx.LoadingScreen;
import javafx.concurrent.Task;
import javafx.scene.image.Image;

/**
 * Just a normal LoadingScreen which uses the app icon.
 * @author Alejandro
 */
public class MyLoadingScreen extends LoadingScreen {
    
    public MyLoadingScreen(Task t) {
        super(t);
        addIcons(new Image(getClass().getResourceAsStream("/accesoupv/resources/app-icon.png")));
    }
    
}
