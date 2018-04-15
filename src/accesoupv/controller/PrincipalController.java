/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package accesoupv.controller;

import accesoupv.AccesoUPV;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.prefs.Preferences;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 *
 * @author Alejandro
 */
public class PrincipalController implements Initializable {
    
    @FXML
    private Label labelState;
    @FXML
    private VBox loadingScreen;
    @FXML
    private MenuItem menuAccessW;
    @FXML
    private MenuItem menuAccessDSIC;
    @FXML
    private MenuItem menuAjustes;
    @FXML
    private MenuItem menuAyuda;
    @FXML
    private MenuItem ayudaVPN;
    @FXML
    private MenuItem ayudaDSIC;
    @FXML
    private MenuItem menuDisconnectW;
    @FXML
    private MenuItem menuDisconnectUPV;
    
    public static String drive, vpn, user;
    
    private void loadPrefs() {
        Preferences prefs = Preferences.userNodeForPackage(AccesoUPV.class);
        drive = prefs.get("drive", "");
        vpn = prefs.get("vpn", "");
        user = prefs.get("user", "");
        if (drive.isEmpty() || vpn.isEmpty() || user.isEmpty()) { gotoAjustes(true); }
    }
    
    private void gotoAjustes(boolean noPrefs) {
        try {    
            Stage stage = new Stage();
            FXMLLoader myLoader = new FXMLLoader(getClass().getResource("/accesoupv/view/AjustesView.fxml"));
            Parent root = (Parent) myLoader.load();
            AjustesController dialogue = myLoader.<AjustesController>getController();
            dialogue.init(stage, noPrefs);
            Scene scene = new Scene(root);

            stage.setScene(scene);
            stage.initModality(Modality.APPLICATION_MODAL);
            if (noPrefs) stage.showAndWait();
            else stage.show();
        } catch (IOException e) {}
    }
    
    private void gotoAyuda(String page) {
        try {
            Stage stage = new Stage();
            FXMLLoader myLoader = new FXMLLoader(getClass().getResource("/accesoupv/view/AyudaView.fxml"));
            Parent root = (Parent) myLoader.load();
            AyudaController dialogue = myLoader.<AyudaController>getController();
            dialogue.init(stage, page);
            Scene scene = new Scene(root);

            stage.setScene(scene);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.show();
        } catch (IOException ex) {
        }
    }
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        loadPrefs();
        menuAyuda.setOnAction((e) -> gotoAyuda(""));
        ayudaDSIC.setOnAction((e) -> gotoAyuda("DSIC"));
        ayudaVPN.setOnAction((e) -> gotoAyuda("VPN"));
        menuAjustes.setOnAction((e) -> gotoAjustes(false));
    }    
    
}
