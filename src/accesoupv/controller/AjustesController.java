/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package accesoupv.controller;

import accesoupv.model.AccesoUPV;
import java.io.IOException;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * FXML Controller class
 *
 * @author Alejandro
 */
public class AjustesController implements Initializable {
    
    @FXML
    private Text textWarningVPN;
    @FXML
    private TextField textVPN;
    @FXML
    private ComboBox<String> comboDrive;
    @FXML
    private TextField textUser;
    @FXML
    private MenuItem menuAyuda;
    @FXML
    private MenuItem menuAyudaVPN;
    @FXML
    private MenuItem menuAyudaDSIC;
    @FXML
    private Hyperlink helpLinkVPN;
    @FXML
    private Hyperlink helpLinkUser;
    @FXML
    private Button buttonOK;
    
    //Constants (Messages)
    public static final String SUCCESS_MESSAGE = "El archivo ha sido creado con éxito.\n¿Desea abrir la carpeta en la cual ha sido guardado?";
    public static final String ERROR_MESSAGE = "Ha habido un error al crear el programa. Vuelva a intentarlo.";
    public static final String FOLDER_ERROR_MESSAGE = "Ha habido un error al abrir la carpeta. Ábrala manualmente.";
    public static final String HELP_TOOLTIP = 
            "Formato:\n"
            + "Siendo tu usuario completo: \"usuario@dominio.upv.es\"\n"
            + "Escriba: \"usuario\"";
    
    private Stage primaryStage;
    private boolean exitOnCancel;
    private ObservableList<String> dataDrives;
    //AccesoUPV Instance
    private static final AccesoUPV acceso = AccesoUPV.getInstance();
    
    public void init(Stage stage, boolean exOnCancel) {
        primaryStage = stage;
        primaryStage.setTitle("Preferencias");
        exitOnCancel = exOnCancel;
        if (exitOnCancel) {
            primaryStage.setOnCloseRequest((evt) -> {
                Platform.exit();
                System.exit(0);
            });
        }
        //Escribe las preferencias guardadas
        String user = acceso.getUser();
        if (user == null) {
            Platform.runLater(() -> textUser.requestFocus());
            user = "";
        }
        textUser.setText(user);
        String vpn = acceso.getVPN();
        if (vpn == null) {
            Platform.runLater(() -> textVPN.requestFocus());
            vpn = "";
        }
        textVPN.setText(vpn);
        String drive = acceso.getDrive();
        //Si la lista lo contiene, selecciona la unidad guardada (o 'W:' por defecto si no hay guardada ninguna unidad)
        if (dataDrives.contains(drive)) {
            comboDrive.getSelectionModel().select(drive);
        } else {
            comboDrive.getSelectionModel().selectFirst();
        }
    }
    
    @FXML
    private void savePrefs(ActionEvent event) {
            acceso.setVPN(textVPN.getText());
            acceso.setDrive(comboDrive.getValue());
            acceso.setUser(textUser.getText());
            acceso.savePrefs();
            //Cierra la ventana de ajustes
            primaryStage.hide();
    }
    
    private void showAyuda(String page) {
        try {
            Stage stage = new Stage();
            FXMLLoader myLoader = new FXMLLoader(getClass().getResource("/accesoupv/view/AyudaView.fxml"));
            Parent root = (Parent) myLoader.load();
            AyudaController dialogue = myLoader.<AyudaController>getController();
            dialogue.init(stage, page);
            Scene scene = new Scene(root);

            stage.setScene(scene);
            stage.getIcons().add(new Image(getClass().getResourceAsStream("/accesoupv/resources/help-icon.png")));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.show();
        } catch (IOException ex) {
        }
    }
    
    private boolean anyChanges() {
        String vpn = acceso.getVPN();
        if (vpn == null) vpn = "";
        String user = acceso.getUser();
        if (user == null) user = "";
        
        return !(textVPN.getText().equals(vpn)
        && textUser.getText().equals(user)
        && comboDrive.getSelectionModel().getSelectedItem().equals(acceso.getDrive()));
    }
    
    @FXML
    private void closeDialogue(ActionEvent event) {
        if (anyChanges()) {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "¿Desea salir sin guardar los cambios?");
            confirm.setHeaderText(null);
            Optional<ButtonType> result = confirm.showAndWait();
            if (result.get() != ButtonType.OK) return;
        }
        if (exitOnCancel) {
            Platform.exit();
            System.exit(0);
        }
        primaryStage.hide();
    }
    
    /**
     * Initializes the controller class.
     * @param url
     * @param rb
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        dataDrives = FXCollections.observableList(AccesoUPV.getAvailableDrives());
        comboDrive.setItems(dataDrives);
        //Si se ha cambiado el valor de la VPN y se encuentra conectada, muestra un mensaje.
        if (acceso.isVPNConnected()) {
            textVPN.textProperty().addListener((obs, oldValue, newValue) -> {
                textWarningVPN.setVisible(!newValue.equals(acceso.getVPN()));
            });
        }
                 
        menuAyuda.setOnAction(e -> showAyuda(""));
        menuAyudaDSIC.setOnAction(e -> showAyuda("DSIC"));
        menuAyudaVPN.setOnAction(e -> showAyuda("VPN"));
        
        helpLinkVPN.setOnAction(e -> showAyuda("VPN"));
        helpLinkVPN.setTooltip(new Tooltip("Click para ver cómo \"" + menuAyudaVPN.getText() + "\""));
        //Evento para que si haces click, muestre directamente el Tooltip
        helpLinkUser.addEventFilter(MouseEvent.MOUSE_PRESSED, e -> { 
            helpLinkUser.getTooltip().show(primaryStage);
            e.consume();
        });
        //Evento para que siempre que quites el ratón del nodo, esconda el Tooltip
        helpLinkUser.addEventHandler(MouseEvent.MOUSE_EXITED, e -> helpLinkUser.getTooltip().hide());
        helpLinkUser.setTooltip(new Tooltip(HELP_TOOLTIP));
        //Hace que al abrir la ventana, el focus lo tenga el botón de cancelar
        Platform.runLater(() -> buttonOK.requestFocus());
    }    
}
