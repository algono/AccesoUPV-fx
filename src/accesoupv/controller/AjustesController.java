/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package accesoupv.controller;

import static accesoupv.Launcher.acceso;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
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
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
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
    private Text textWarning;
    @FXML
    private Button buttonAccept;
    @FXML
    private Button buttonClose;
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
    
    //Constants (Messages)
    public static final String SUCCESS_MESSAGE = "El archivo ha sido creado con éxito.\n¿Desea abrir la carpeta en la cual ha sido guardado?";
    public static final String ERROR_MESSAGE = "Ha habido un error al crear el programa. Vuelva a intentarlo.";
    public static final String FOLDER_ERROR_MESSAGE = "Ha habido un error al abrir la carpeta. Ábrala manualmente.";
    
    private Stage primaryStage;
    private boolean exitOnCancelled;
    private ObservableList<String> dataDrives;
    
    public void init(Stage stage, boolean eOnCancel) {
        primaryStage = stage;
        primaryStage.setTitle("Preferencias");
        exitOnCancelled = eOnCancel;
        if (exitOnCancelled) {
            stage.setOnCloseRequest((evt) -> {
                Platform.exit();
                System.exit(0);
            });
        }
        String drive;
        if (acceso.isIncomplete()) {
            textWarning.setVisible(true);
            drive = "W:"; //Por defecto, selecciona 'W:' como la unidad en comboDrive
        } else {
            //Escribe las preferencias guardadas
            textUser.setText(acceso.getUser());
            textVPN.setText(acceso.getVPN());
            drive = acceso.getDrive();
        }
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
    
    private void createDriveList() {
        ArrayList<String> drives = new ArrayList<>();
        char letter = 'Z';
        while (letter >= 'D') {
            String drive = letter + ":";
            if (!new File(drive).exists()) drives.add(drive);
            letter--;
        }
        dataDrives = FXCollections.observableList(drives);
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
    
    @FXML
    private void closeDialogue(ActionEvent event) {
        if (!textVPN.getText().equals(acceso.getVPN())
                || !textUser.getText().equals(acceso.getUser())
                || !comboDrive.getSelectionModel().getSelectedItem().equals(acceso.getDrive())) {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "¿Desea salir sin guardar los cambios?");
            confirm.setHeaderText(null);
            Optional<ButtonType> result = confirm.showAndWait();
            if (result.get() != ButtonType.OK) return;
        }
        if (exitOnCancelled) {
            Platform.exit();
            System.exit(0);
        }
        primaryStage.hide();
    }
    
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        createDriveList();
        comboDrive.setItems(dataDrives);
        
        buttonAccept.disableProperty().bind(
            Bindings.or(
                    Bindings.isEmpty(textUser.textProperty()),
                    Bindings.isEmpty(textVPN.textProperty())
            )
        );
        menuAyuda.setOnAction((e) -> gotoAyuda(""));
        menuAyudaDSIC.setOnAction((e) -> gotoAyuda("DSIC"));
        menuAyudaVPN.setOnAction((e) -> gotoAyuda("VPN"));
        
        Platform.runLater(() -> buttonClose.requestFocus());
    }    
}
