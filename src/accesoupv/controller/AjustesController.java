/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package accesoupv.controller;

import accesoupv.model.AccesoUPV;
import accesoupv.model.Dominio;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.PasswordField;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Text;

/**
 * FXML Controller class
 *
 * @author Alejandro
 */
public class AjustesController implements Initializable {
    
    @FXML
    private MenuBar menuBar;
    @FXML
    private Text textWarningConnected;
    @FXML
    private TextField textVpnUPV;
    @FXML
    private TextField textVpnDSIC;
    @FXML
    private ComboBox<String> comboDriveW;
    @FXML
    private ComboBox<String> comboDriveDSIC;
    @FXML
    private TextField textUser;
    @FXML
    private Hyperlink helpLinkVpnUPV;
    @FXML
    private Hyperlink helpLinkVpnDSIC;
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
    private CheckBox driveWCheckBox;
    @FXML
    private CheckBox driveDSICCheckBox;
    @FXML
    private PasswordField passDriveDSIC;
    
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
            "Si te es indiferente la unidad en la que se cree la conexión con el disco, habilita esta opción.\n"
            + "(la conexión se creará en la primera unidad disponible)";
    
    //Lists with available drives for comboDrives
    private ObservableList<String> dataDrivesW, dataDrivesDSIC;
    //AccesoUPV Instance
    private static final AccesoUPV acceso = AccesoUPV.getInstance();
    
    @FXML
    private void savePrefs(ActionEvent evt) {
            if (VpnUPVChanged()) acceso.setVpnUPV(textVpnUPV.getText());
            if (VpnDSICChanged()) acceso.setVpnDSIC(textVpnDSIC.getText());
            if (userChanged()) acceso.setUser(textUser.getText());
            if (driveWChanged()) acceso.setDriveW(driveWCheckBox.isSelected() ? "*" : comboDriveW.getValue());
            if (domainChanged()) acceso.setDomain(alumnoRadioButton.isSelected() ? Dominio.ALUMNOS : Dominio.UPVNET);
            if (driveDSICChanged()) acceso.setDriveDSIC(driveDSICCheckBox.isSelected() ? "*" : comboDriveDSIC.getValue());
            if (passDSICChanged()) acceso.setPassDSIC(passDriveDSIC.getText());
            //Cierra la ventana de ajustes
            ((Node) evt.getSource()).getScene().getWindow().hide();
    }
    
    private boolean VpnUPVChanged() {
        String vpn = acceso.getVpnUPV();
        return !textVpnUPV.getText().equals(vpn);
    }
    
    private boolean VpnDSICChanged() {
        String vpn = acceso.getVpnDSIC();
        return !textVpnDSIC.getText().equals(vpn);
    }
    
    private boolean userChanged() {
        String user = acceso.getUser();
        return !textUser.getText().equals(user);
    }
    
    private boolean driveWChanged() {
        String selectedItem = comboDriveW.getSelectionModel().getSelectedItem();
        return driveWCheckBox.isSelected() 
                ? !acceso.getDriveW().equals("*") 
                : selectedItem != null && !selectedItem.equals(acceso.getDriveW());
    }
    
    private boolean driveDSICChanged() {
        String selectedItem = comboDriveDSIC.getSelectionModel().getSelectedItem();
        return driveDSICCheckBox.isSelected() 
                ? !acceso.getDriveDSIC().equals("*") 
                : selectedItem != null && !selectedItem.equals(acceso.getDriveDSIC());
    }
    
    private boolean passDSICChanged() {
        String pass = acceso.getPassDSIC();
        return !passDriveDSIC.getText().equals(pass);
    }
    
    private boolean domainChanged() {
        //XOR (Iguales -> false, Distintos -> true)
        return alumnoRadioButton.isSelected() != (acceso.getDomain() == Dominio.ALUMNOS);
    }
    
    private boolean anyChanges() {
        return VpnUPVChanged() || VpnDSICChanged() || userChanged() || driveWChanged() || driveDSICChanged() || domainChanged();
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
    
    private void initDriveBoxes(String drive, ComboBox<String> c, CheckBox ch, ObservableList<String> data) {
        c.setItems(data);
        //Si el comboBox no tiene unidades disponibles, selecciona el checkBox y deshabilita ambos
        if (data.isEmpty()) {
            ch.setSelected(true);
            c.setDisable(true); ch.setDisable(true);
        }
        //Por defecto se selecciona siempre el primer elemento de comboDrive
        c.getSelectionModel().selectFirst();
        //Si está en "indiferente" activa el checkbox
        if (drive.equals("*")) ch.setSelected(true);
        //Si la lista lo contiene, selecciona la unidad guardada
        else if (data.contains(drive)) {
            c.getSelectionModel().select(drive);
        }
        
        //Si el checkbox de "unidad indiferente" está seleccionado, la comboBox se desactiva (y viceversa)
        c.disableProperty().bind(ch.selectedProperty());
        
        ch.setTooltip(new Tooltip(DRIVE_TOOLTIP));
    }
    
    /**
     * Initializes the controller class.
     * @param url
     * @param rb
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        //Crea la lista de unidades disponibles para el disco W
        dataDrivesW = FXCollections.observableList(acceso.getAvailableDrivesW());
        dataDrivesDSIC = FXCollections.observableList(acceso.getAvailableDrivesDSIC());
        initDriveBoxes(acceso.getDriveW(), comboDriveW, driveWCheckBox, dataDrivesW);
        initDriveBoxes(acceso.getDriveDSIC(), comboDriveDSIC, driveDSICCheckBox, dataDrivesDSIC);
        
        //Si se ha cambiado algún valor de un servicio que se encuentra conectado, muestra un mensaje.
        textWarningConnected.visibleProperty().bind(Bindings.createBooleanBinding(() ->
            (acceso.isVpnUPVConnected() && VpnUPVChanged())
            || (acceso.isDriveWConnected() && (userChanged() || driveWChanged() || domainChanged()))
            || (acceso.isDriveDSICConnected() && (userChanged() || driveDSICChanged()))
        ,textVpnUPV.textProperty(), textVpnDSIC.textProperty(), textUser.textProperty(), 
        comboDriveW.getSelectionModel().selectedItemProperty(), driveWCheckBox.selectedProperty(),
        comboDriveDSIC.getSelectionModel().selectedItemProperty(), driveDSICCheckBox.selectedProperty(),
        passDriveDSIC.textProperty(), dominio.selectedToggleProperty()));
        
        Menu ayudaMenu = AyudaController.getInstance().getMenu(); 
        //Inicializa los links de ayuda
        for (MenuItem item : ayudaMenu.getItems()) {
            String text = item.getText();
            if (text != null && text.contains("crear una VPN")) {
                if (text.contains("UPV")) {
                    helpLinkVpnUPV.setOnAction(evt -> item.fire());
                    helpLinkVpnUPV.setTooltip(new Tooltip("Click para ver \"" + text + "\""));
                } else if (text.contains("DSIC")) {
                    helpLinkVpnDSIC.setOnAction(evt -> item.fire());
                    helpLinkVpnDSIC.setTooltip(new Tooltip("Click para ver \"" + text + "\""));
                }
            }
        }
        //Añade un menú de ayuda a la barra de menú
        menuBar.getMenus().add(ayudaMenu);
        
        resetButton.setOnAction(evt -> {
            //Ventana de confirmación
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, RESET_MESSAGE);
            confirm.setHeaderText(null);
            Optional<ButtonType> res = confirm.showAndWait();
            //Si confirma, resetea y cierra la ventana de ajustes
            if (res.isPresent() && res.get() == ButtonType.OK) {
                acceso.clearPrefs();
                acceso.setSavePrefsOnExit(false);
                acceso.shutdown();
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
        textUser.setText(user);
        
        String vpn = acceso.getVpnUPV();
        textVpnUPV.setText(vpn);
        
        vpn = acceso.getVpnDSIC();
        textVpnDSIC.setText(vpn);
        
        String pass = acceso.getPassDSIC();
        passDriveDSIC.setText(pass);
        
        //Si el dominio guardado es ALUMNOS, marca el RadioButton adecuado
        if (acceso.getDomain() == Dominio.ALUMNOS) alumnoRadioButton.setSelected(true);
        
        //Hace que al abrir la ventana, el focus lo tenga el botón de aceptar
        Platform.runLater(() -> OKButton.requestFocus());
    }    
}
