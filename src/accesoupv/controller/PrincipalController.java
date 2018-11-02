/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package accesoupv.controller;

import accesoupv.model.AccesoUPV;
import java.io.IOException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;
import javafx.scene.web.WebView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 *
 * @author Alejandro
 */
public class PrincipalController implements Initializable {
    
    
    @FXML
    private MenuItem menuAccessW;
    @FXML
    private MenuItem menuDisconnectW;
    @FXML
    private MenuItem menuAccessDSIC;
    @FXML
    private MenuItem menuDisconnectDSIC;
    @FXML
    private MenuItem menuLinuxDSIC;
    @FXML
    private MenuItem menuWinDSIC;
    @FXML
    private MenuItem menuAjustes;
    @FXML
    private MenuItem menuAyuda;
    @FXML
    private MenuItem menuAyudaVpnUPV;
    @FXML
    private MenuItem menuAyudaVpnDSIC;
    @FXML
    private MenuItem menuAyudaDSIC;
    @FXML
    private MenuItem menuPortalDSIC;
    @FXML
    private Button buttonAccessW;
    @FXML
    private Button buttonDisconnectW;
    @FXML
    private Button buttonAccessDSIC;
    @FXML
    private Button buttonDisconnectDSIC;
    @FXML
    private Button buttonLinuxDSIC;
    @FXML
    private Button buttonWinDSIC;
    @FXML
    private Button buttonPortalDSIC;
    
    //Messages
    public static final String ERROR_DSIC_MSG = "No se ha podido acceder al servidor DSIC.";
    //AccesoUPV Instance
    private static final AccesoUPV acceso = AccesoUPV.getInstance();
    
    private void showAjustes() {
        try {
            Stage stage = new Stage();
            FXMLLoader myLoader = new FXMLLoader(getClass().getResource("/accesoupv/view/AjustesView.fxml"));
            Parent root = (Parent) myLoader.load();
            stage.setTitle("Preferencias");
            Scene scene = new Scene(root);

            stage.setScene(scene);
            stage.getIcons().add(new Image(getClass().getResourceAsStream("/accesoupv/resources/icons/preferences-icon.png")));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
        } catch (IOException ex) {}
    }
    
    public static void showAyuda(String page) {
        try {
            Stage stage = new Stage();
            FXMLLoader myLoader = new FXMLLoader(PrincipalController.class.getResource("/accesoupv/view/AyudaView.fxml"));
            Parent root = (Parent) myLoader.load();
            AyudaController dialogue = myLoader.<AyudaController>getController();
            dialogue.init(stage, page);
            Scene scene = new Scene(root);

            stage.setScene(scene);
            stage.getIcons().add(new Image(PrincipalController.class.getResourceAsStream("/accesoupv/resources/icons/help-icon.png")));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.show();
        } catch (IOException ex) {
            new Alert(Alert.AlertType.ERROR, "Ha habido un error inesperado al tratar de abrir la ventana.").show();
        }
    }
    
    private void accessDSIC(String server) {
        try {
            new ProcessBuilder("cmd.exe", "/c", "mstsc /v:" + server).start();
        } catch (IOException ex) {
            new Alert(Alert.AlertType.ERROR, ERROR_DSIC_MSG).show();
        }
    }
    
    private void accessPortal() {
        // Create a trust manager that does not validate certificate chains
        TrustManager[] trustAllCerts = new TrustManager[] { 
            new X509TrustManager() {     
                @Override
                public java.security.cert.X509Certificate[] getAcceptedIssuers() { 
                    return null;
                } 
                @Override
                public void checkClientTrusted( 
                    java.security.cert.X509Certificate[] certs, String authType) {
                    } 
                @Override
                public void checkServerTrusted( 
                    java.security.cert.X509Certificate[] certs, String authType) {
                }
            } 
        }; 
        SSLSocketFactory defaultSSLSocketFactory = HttpsURLConnection.getDefaultSSLSocketFactory();
        // Install the all-trusting trust manager
        try {
            SSLContext sc = SSLContext.getInstance("SSL"); 
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        } catch (GeneralSecurityException e) {
        } 
        
        WebView webView = new WebView();

        StackPane root = new StackPane();
        root.getChildren().add(webView);

        Scene scene = new Scene(root, 300, 250);
        Stage portal = new Stage();
        portal.setTitle("Portal DSIC (portal-ng.dsic.upv.es)");
        portal.setScene(scene);
        
        if (acceso.connectVpnDSIC()) {
            portal.setOnShown((evt) -> {
                Platform.runLater(() -> {
                    webView.getEngine().load(AccesoUPV.PORTAL_DSIC_WEB);
                    portal.setMaximized(true);
                });
            });
            portal.showAndWait();
            acceso.disconnectVpnDSIC();
        }
        HttpsURLConnection.setDefaultSSLSocketFactory(defaultSSLSocketFactory);
    }
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        //Assigns actions to buttons and menu items
        menuLinuxDSIC.setOnAction(e -> accessDSIC(AccesoUPV.LINUX_DSIC));
        buttonLinuxDSIC.setOnAction(e -> accessDSIC(AccesoUPV.LINUX_DSIC));
        
        menuWinDSIC.setOnAction(e -> accessDSIC(AccesoUPV.WIN_DSIC));
        buttonWinDSIC.setOnAction(e -> accessDSIC(AccesoUPV.WIN_DSIC));
        
        menuAyuda.setOnAction(e -> showAyuda(""));
        menuAyudaDSIC.setOnAction(e -> showAyuda("DSIC"));
        menuAyudaVpnUPV.setOnAction(e -> showAyuda("VPN"));
        menuAyudaVpnDSIC.setOnAction(e -> showAyuda("VPN-dsic"));
        
        menuAjustes.setOnAction(e -> showAjustes());
        
        buttonAccessW.setOnAction(e -> acceso.connectW());
        menuAccessW.setOnAction(e -> acceso.connectW());
        
        buttonAccessDSIC.setOnAction(e -> acceso.connectDSIC());
        menuAccessDSIC.setOnAction(e -> acceso.connectDSIC());
        
        buttonDisconnectW.setOnAction(e -> acceso.disconnectW());
        menuDisconnectW.setOnAction(e -> acceso.disconnectW());
        
        buttonDisconnectDSIC.setOnAction(e -> acceso.disconnectDSIC());
        menuDisconnectDSIC.setOnAction(e -> acceso.disconnectDSIC());
        
        buttonPortalDSIC.setOnAction(e -> accessPortal());
        menuPortalDSIC.setOnAction(e -> accessPortal());
        
        //Setting bindings for buttons
        buttonDisconnectW.disableProperty().bind(Bindings.not(acceso.connectedWProperty()));
        menuDisconnectW.disableProperty().bind(Bindings.not(acceso.connectedWProperty()));
        buttonDisconnectDSIC.disableProperty().bind(Bindings.not(acceso.connectedDSICProperty()));
        menuDisconnectDSIC.disableProperty().bind(Bindings.not(acceso.connectedDSICProperty()));
        
        //Si no está ya conectado a la UPV, trata de conectarse a la VPN
        Platform.setImplicitExit(false); //Se asegura de que el programa no se cierre solo
        if (!acceso.isConnectedToUPV()) acceso.connectVpnUPV();
        Platform.runLater(() -> Platform.setImplicitExit(true));
    }
}
