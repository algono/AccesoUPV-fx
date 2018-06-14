/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package accesoupv.controller;

import accesoupv.model.AccesoUPV;
import accesoupv.model.Dominio;
import java.io.IOException;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
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
    private Text textWarningConnected;
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
    private ToggleGroup dominio;
    @FXML
    private RadioButton alumnoRadioButton;
    @FXML
    private Button resetButton;
    @FXML
    private Button OKButton;
    @FXML
    private CheckBox driveCheckBox;
    @FXML
    private HBox driveBox;
        
    //Constants (Messages)
    public static final String SUCCESS_MESSAGE = "El archivo ha sido creado con éxito.\n¿Desea abrir la carpeta en la cual ha sido guardado?";
    public static final String ERROR_MESSAGE = "Ha habido un error al crear el programa. Vuelva a intentarlo.";
    public static final String FOLDER_ERROR_MESSAGE = "Ha habido un error al abrir la carpeta. Ábrala manualmente.";
    public static final String RESET_MESSAGE = 
            "Se reestablecerán los valores predeterminados, por tanto será como si acabaras de ejecutar el programa por primera vez.\n\n"
            + "Para completar este proceso, el programa se cerrará. Al volverlo a abrir se habrá completado.\n\n"
            + "¿Estás seguro?";
    public static final String HELP_USER_TOOLTIP = 
            "Formato:\n"
            + "Siendo tu usuario completo: \"usuario@dominio.upv.es\"\n"
            + "Escriba: \"usuario\"";
    public static final String DRIVE_TOOLTIP = 
            "Si te es indiferente la unidad en la que se cree la conexión con el Disco W, habilita esta opción.\n"
            + "(la conexión se creará en la primera unidad disponible)";
    
    //List with available drives for comboDrive
    private ObservableList<String> dataDrives;
    //AccesoUPV Instance
    private static final AccesoUPV acceso = AccesoUPV.getInstance();
    
    @FXML
    private void savePrefs(ActionEvent evt) {
            if (VPNChanged()) acceso.setVPN(textVPN.getText());
            if (userChanged()) acceso.setUser(textUser.getText());
            if (driveChanged()) acceso.setDrive(driveCheckBox.isSelected() ? "*" : comboDrive.getValue());
            if (domainChanged()) acceso.setDomain(alumnoRadioButton.isSelected() ? Dominio.ALUMNOS : Dominio.UPVNET);
            //Cierra la ventana de ajustes
            ((Node) evt.getSource()).getScene().getWindow().hide();
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
            stage.getIcons().add(new Image(getClass().getResourceAsStream("/accesoupv/resources/icons/help-icon.png")));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.show();
        } catch (IOException ex) {
        }
    }
    
    private boolean VPNChanged() {
        String vpn = acceso.getVPN();
        if (vpn == null) vpn = "";
        return !textVPN.getText().equals(vpn);
    }
    
    private boolean userChanged() {
        String user = acceso.getUser();
        if (user == null) user = "";
        return !textUser.getText().equals(user);
    }
    
    private boolean driveChanged() {
        String selectedItem = comboDrive.getSelectionModel().getSelectedItem();
        return driveCheckBox.isSelected() 
                ? !acceso.getDrive().equals("*") 
                : selectedItem != null && !selectedItem.equals(acceso.getDrive());
    }
    
    private boolean domainChanged() {
        //XOR (Iguales -> false, Distintos -> true)
        return alumnoRadioButton.isSelected() != (acceso.getDomain() == Dominio.ALUMNOS);
    }
    
    private boolean anyChanges() {
        return VPNChanged() || userChanged() || driveChanged() || domainChanged();
    }
    
    @FXML
    private void closeDialogue(ActionEvent evt) {
        if (anyChanges()) {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "¿Desea salir sin guardar los cambios?");
            confirm.setHeaderText(null);
            Optional<ButtonType> result = confirm.showAndWait();
            if (result.get() != ButtonType.OK) return;
        }
        ((Node) evt.getSource()).getScene().getWindow().hide();
    }
    
    /**
     * Initializes the controller class.
     * @param url
     * @param rb
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        //Crea la lista de unidades disponibles para el disco W
        dataDrives = FXCollections.observableList(acceso.getAvailableDrives());
        comboDrive.setItems(dataDrives);
        //Si el comboBox no tiene unidades disponibles, selecciona el checkBox y deshabilita la HBox entera
        if (dataDrives.isEmpty()) {
            driveCheckBox.setSelected(true);
            driveBox.setDisable(true);
        }
        
        //Si se ha cambiado algún valor de un servicio que se encuentra conectado, muestra un mensaje.
        textWarningConnected.visibleProperty().bind(Bindings.createBooleanBinding(() ->
            (acceso.isVPNConnected() && VPNChanged())
            || (acceso.isWConnected() && (userChanged() || driveChanged() || domainChanged()))
        ,textVPN.textProperty(), textUser.textProperty(), 
        comboDrive.getSelectionModel().selectedItemProperty(), driveCheckBox.selectedProperty(),
        dominio.selectedToggleProperty()));
        
        menuAyuda.setOnAction(evt -> showAyuda(""));
        menuAyudaDSIC.setOnAction(evt -> showAyuda("DSIC"));
        menuAyudaVPN.setOnAction(evt -> showAyuda("VPN"));
        helpLinkVPN.setOnAction(evt -> showAyuda("VPN"));
        helpLinkVPN.setTooltip(new Tooltip("Click para ver cómo \"" + menuAyudaVPN.getText() + "\""));
        
        resetButton.setOnAction(evt -> {
            //Ventana de confirmación
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, RESET_MESSAGE);
            confirm.setHeaderText(null);
            Optional<ButtonType> res = confirm.showAndWait();
            //Si confirma, resetea y cierra la ventana de ajustes
            if (res.isPresent() && res.get() == ButtonType.OK) {
                acceso.reset();
                acceso.disconnectVPN();
                System.exit(0);
            }
        });
        
        //Evento para que si haces click, muestre directamente el Tooltip
        helpLinkUser.addEventFilter(MouseEvent.MOUSE_PRESSED, evt -> { 
            helpLinkUser.getTooltip().show(((Node) evt.getSource()).getScene().getWindow());
            evt.consume();
        });
        
        //Evento para que siempre que quites el ratón del nodo, esconda el Tooltip
        helpLinkUser.addEventHandler(MouseEvent.MOUSE_EXITED, evt -> helpLinkUser.getTooltip().hide());
        helpLinkUser.setTooltip(new Tooltip(HELP_USER_TOOLTIP));
        
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
        //Por defecto se selecciona siempre el primer elemento de comboDrive
        comboDrive.getSelectionModel().selectFirst();
        //Si está en "indiferente" activa el checkbox
        if (drive.equals("*")) driveCheckBox.setSelected(true);
        //Si la lista lo contiene, selecciona la unidad guardada
        else if (dataDrives.contains(drive)) {
            comboDrive.getSelectionModel().select(drive);
        }
        
        //Si el checkbox de "unidad indiferente" está seleccionado, la comboBox se desactiva (y viceversa)
        comboDrive.disableProperty().bind(driveCheckBox.selectedProperty());
        
        driveCheckBox.setTooltip(new Tooltip(DRIVE_TOOLTIP));
        
        //Si el dominio guardado es ALUMNOS, marca el RadioButton adecuado
        if (acceso.getDomain() == Dominio.ALUMNOS) alumnoRadioButton.setSelected(true);
        
        //Hace que al abrir la ventana, el focus lo tenga el botón de aceptar
        Platform.runLater(() -> OKButton.requestFocus());
    }    
}
