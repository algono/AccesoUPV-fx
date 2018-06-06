/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package accesoupv.model.tasks;

import java.awt.Desktop;
import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import javafx.scene.control.Alert;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

/**
 *
 * @author aleja
 */
public class AccesoVPNTask extends AccesoTask {
    //Error messages
    public static final String ERROR_CON_VPN
            = "Se ha producido un error al tratar de conectarse a la VPN.\n\n"
            + "Compruebe que el nombre de la VPN está bien escrito y vuelva a intentarlo.\n ";
    
    public static final String ERROR_INVALID_VPN = 
            "La VPN proporcionada no es válida, pues no es capaz de acceder a la UPV.\n"
            + "Establezca una válida.";
    
    public static final String ERROR_DIS_VPN = "No ha podido desconectarse la VPN. Deberá desconectarla manualmente.";   
    
    //Webpage for possible VPN errors
    public static final String WEB_ERROR_VPN = "https://www.upv.es/contenidos/INFOACCESO/infoweb/infoacceso/dat/723787normalc.html";
    //Timeout for checking if the connected VPN is able to connect the UPV (in ms)
    public static final int PING_TIMEOUT = 4000;
    
    private final String VPN;
    
    public AccesoVPNTask(String vpn, boolean connecting) {
        super(connecting);
        VPN = vpn;
    }
    
    @Override
    protected void connect() throws Exception {
        updateErrorMsg(ERROR_CON_VPN);
        updateMessage("Conectando con la UPV...");
        Process p = startProcess("rasphone.exe", "-d", "\"" + VPN + "\"");
        waitAndCheck(p);
        //Si el proceso fue correctamente pero la VPN no esta conectada, se entiende que el usuario canceló la operación
        p = AccesoTask.startProcess("rasdial.exe");
        waitAndCheck(p);
        if (!getOutput(p).contains(VPN)) cancel();
        //Si el usuario no canceló la operación (lo cual implica que la VPN se conectó con éxito), continúa.
        if (!isCancelled()) {
            //Si desde la VPN a la que se conectó no se puede acceder a la UPV, se entiende que ha elegido una incorrecta.
            boolean reachable = false;
            try {
                reachable = InetAddress.getByName("www.upv.es").isReachable(PING_TIMEOUT);
            } finally {
                //Tanto si no era alcanzable como si hubo un error al realizar el ping, desconecta la VPN por si acaso.
                if (!reachable) {
                    p = startProcess("rasdial.exe", "\"" + VPN + "\"", "/DISCONNECT");
                    waitAndCheck(p);
                }
            }
            //Si no era alcanzable, lanza la excepción adecuada
            if (!reachable) {
                updateErrorMsg(ERROR_INVALID_VPN);
                throw new IllegalArgumentException();
            }
        }
    }
    
    @Override
    protected void disconnect() throws Exception {
        updateErrorMsg(ERROR_DIS_VPN);
        updateMessage("Desconectando de la UPV...");
        Process p = startProcess("rasdial.exe", "\"" + VPN + "\"", "/DISCONNECT");
        waitAndCheck(p, 1000);
    }
    
    @Override
    public Alert getErrorAlert() {
        Alert alert = super.getErrorAlert();
        if (connecting && getErrorMessage().equals(ERROR_CON_VPN)) {
            Hyperlink helpLink = new Hyperlink("Para más información acerca de posibles errores, pulse aquí");
            helpLink.setOnAction(e -> {
                try {
                    Desktop desktop = Desktop.getDesktop();
                    URI oURL = new URI(AccesoVPNTask.WEB_ERROR_VPN);
                    desktop.browse(oURL);
                } catch (IOException | URISyntaxException ex) {
                    new Alert(Alert.AlertType.ERROR, "Ha ocurrido un error al tratar de abrir el navegador.").show();
                }
            });
            VBox box = new VBox(new Label(getErrorMessage()), helpLink);
            alert.getDialogPane().setContent(box);
        }
        return alert;
    }
}
