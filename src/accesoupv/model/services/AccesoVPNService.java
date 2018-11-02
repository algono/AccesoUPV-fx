/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package accesoupv.model.services;

import accesoupv.model.ProcessUtils;
import accesoupv.model.tasks.AlertingTask;
import java.awt.Desktop;
import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import javafx.concurrent.Task;
import javafx.scene.control.Alert;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

/**
 *
 * @author aleja
 */
public class AccesoVPNService extends AccesoService {
    //Error messages
    public static final String ERROR_CON_VPN
            = "Se ha producido un error al tratar de conectarse a la VPN.\n\n"
            + "Compruebe que tiene conexión a Internet,\n"
            + "que el nombre de la VPN está bien escrito\ny vuelva a intentarlo.\n ";
    
    public static final String ERROR_INVALID_VPN = 
            "La VPN proporcionada no es válida, pues no es capaz de acceder a la UPV.\n"
            + "Establezca una válida.";
    
    public static final String ERROR_DIS_VPN = "No ha podido desconectarse la VPN. Deberá desconectarla manualmente.";   
    
    //Webpage for possible VPN errors
    public static final String WEB_ERROR_VPN = "https://www.upv.es/contenidos/INFOACCESO/infoweb/infoacceso/dat/723787normalc.html";
    //Timeout for checking if the connected VPN is able to connect the UPV (in ms)
    public static final int PING_TIMEOUT = 4000;
    
    private String VPN, connectedVPN;
    private String conMsg = "Conectando VPN...", disMsg = "Desconectando VPN...";
    
    public AccesoVPNService(String vpn) {
        VPN = vpn;
    }

    public String getVPN() {
        return VPN;
    }
    
    public String getConnectedVPN() {
        return isConnected() ? connectedVPN : VPN;
    }
    
    public String[] getMessages() {
        String[] msgs = {conMsg, disMsg};
        return msgs;
    }
    public void setMessages(String connectMsg, String disconnectMsg) {
        conMsg = connectMsg;
        disMsg = disconnectMsg;
    }
    
    @Override
    protected void succeeded() {
        super.succeeded();
        connectedVPN = isConnected() ? VPN : null;
    }
    
    @Override
    protected Task<Void> createConnectTask() {
        return new AlertingTask(ERROR_CON_VPN) {
            @Override
            protected void doTask() throws Exception {
                updateMessage(conMsg);
                Process p = ProcessUtils.startProcess("rasphone.exe", "-d", "\"" + VPN + "\"");
                ProcessUtils.waitAndCheck(p);
                //Si el proceso fue correctamente pero la VPN no esta conectada, se entiende que el usuario canceló la operación
                p = ProcessUtils.startProcess("rasdial.exe");
                ProcessUtils.waitAndCheck(p);
                //Si el usuario no canceló la operación (lo cual implica que la VPN se conectó con éxito), continúa.
                if (ProcessUtils.getOutput(p).contains(VPN)) {
                    //Si desde la VPN a la que se conectó no se puede acceder a la UPV, se entiende que ha elegido una incorrecta.
                    boolean reachable = false;
                    try {
                        reachable = InetAddress.getByName("www.upv.es").isReachable(PING_TIMEOUT);
                    } finally {
                        //Tanto si no era alcanzable como si hubo un error al realizar el ping
                        //(o si fue cancelado desde fuera), desconecta la VPN por si acaso.
                        if (!reachable || isCancelled()) {
                            p = ProcessUtils.startProcess("rasdial.exe", "\"" + VPN + "\"", "/DISCONNECT");
                            ProcessUtils.waitAndCheck(p);
                        }
                    }
                    //Si no era alcanzable, lanza la excepción adecuada
                    if (!reachable) {
                        updateErrorMsg(ERROR_INVALID_VPN);
                        throw new IllegalArgumentException();
                    }
                } else {
                    cancel(); 
                }
            }
            
            @Override
            public Alert getErrorAlert() {
                Alert alert = super.getErrorAlert();
                if (getErrorMessage().equals(ERROR_CON_VPN)) {
                    Hyperlink helpLink = new Hyperlink("Para más información acerca de posibles errores, pulse aquí");
                    helpLink.setOnAction(e -> {
                        try {
                            Desktop desktop = Desktop.getDesktop();
                            URI oURL = new URI(AccesoVPNService.WEB_ERROR_VPN);
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
        };
    }
    
    @Override
    protected Task<Void> createDisconnectTask() {
        return new AlertingTask() {
            @Override
            protected void doTask() throws Exception {
                updateErrorMsg(ERROR_DIS_VPN);
                updateMessage(disMsg);
                Process p = ProcessUtils.startProcess("rasdial.exe", "\"" + connectedVPN + "\"", "/DISCONNECT");
                ProcessUtils.waitAndCheck(p, 1000);
            }
        };
    }

    public void setVPN(String vpn) {
        this.VPN = vpn;
    }
}
