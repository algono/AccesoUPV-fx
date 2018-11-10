/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package accesoupv.model.services;

import accesoupv.model.ProcessUtils;
import myLibrary.javafx.AlertingTask;
import accesoupv.model.Input;
import java.awt.Desktop;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Scanner;
import javafx.concurrent.Task;
import javafx.scene.control.Alert;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import myLibrary.javafx.ErrorAlert;

/**
 *
 * @author aleja
 */
public abstract class AccesoVPNService extends AccesoService implements Creatable<String> {
    //Error messages
    public static final String ERROR_CON_VPN
            = "Se ha producido un error al tratar de conectarse a la VPN.\n\n"
            + "Compruebe que tiene conexión a Internet,\n"
            + "que el nombre de la VPN está bien escrito\ny vuelva a intentarlo.\n\n";
    
    public static final String ERROR_INVALID_VPN = 
            "La VPN proporcionada no es válida, pues no es capaz de acceder al servidor asociado.\n"
            + "Establezca una válida.";
    
    public static final String ERROR_DIS_VPN = "No ha podido desconectarse la VPN. Deberá desconectarla manualmente.";   
    
    //Webpage for possible VPN errors
    public static final String WEB_ERROR_VPN = "https://www.upv.es/contenidos/INFOACCESO/infoweb/infoacceso/dat/723787normalc.html";
    //Timeout for checking if the connected VPN is able to connect the server (in ms)
    public static final int PING_TIMEOUT = 500, FINAL_PING_TIMEOUT = 4000;
    
    protected String vpn, connectedVpn, iServer;
    protected String conMsg = "Conectando VPN...", disMsg = "Desconectando VPN...";
    
    public AccesoVPNService(String vpn) {
        this.vpn = vpn;
    }

    public String getVPN() {
        return vpn;
    }
    
    public String getConnectedVPN() {
        return isConnected() ? connectedVpn : vpn;
    }
    
    public boolean isReachable() { return isReachable(PING_TIMEOUT); }
    public boolean isReachable(int timeout) {
        //Si ya es posible acceder al servidor, estamos conectados
        try {
            return ProcessUtils.startProcess("ping", "-n", "1", "-w", String.valueOf(timeout), iServer).waitFor() == 0;
        } catch (IOException | InterruptedException ex) {}
        
        return false;
    }
    
    public void setVPN(String vpn) {   
        this.vpn = vpn;
    }
    
    @Override
    protected void succeeded() {
        super.succeeded();
        connectedVpn = isConnected() ? vpn : null;
    }
    
    class VPNConnectTask extends AlertingTask<Boolean> {
        
        public VPNConnectTask() {
            super(ERROR_CON_VPN);
        }
        
        @Override
        protected Boolean doTask() throws Exception {
            updateMessage(conMsg);
            Process p = ProcessUtils.startProcess("rasphone.exe", "-d", "\"" + vpn + "\"");
            ProcessUtils.waitAndCheck(p);
            //Si el proceso fue correctamente pero la VPN no esta conectada, se entiende que el usuario canceló la operación
            p = ProcessUtils.startProcess("rasdial.exe");
            ProcessUtils.waitAndCheck(p);
            //Si el usuario no canceló la operación (lo cual implica que la VPN se conectó con éxito), continúa.
            if (ProcessUtils.getOutput(p).contains(vpn)) {
                //Si desde la VPN a la que se conectó no se puede acceder al servidor, se entiende que ha elegido una incorrecta.
                boolean reachable = isReachable(FINAL_PING_TIMEOUT);
                if (!reachable || isCancelled()) {
                    p = ProcessUtils.startProcess("rasdial.exe", "\"" + vpn + "\"", "/DISCONNECT");
                    ProcessUtils.waitAndCheck(p);
                    //Si no era alcanzable, lanza la excepción adecuada
                    if (!reachable) {
                        updateErrorMsg(ERROR_INVALID_VPN);
                        throw new IllegalArgumentException();
                    }
                }
            } else {
                cancel(); 
            }
            return true;
        }

        @Override
        public Alert getErrorAlert(Throwable ex) {
            Alert alert = super.getErrorAlert(ex);
            Hyperlink helpLink = new Hyperlink("Para más información acerca de posibles errores, pulse aquí");
            helpLink.setOnAction(e -> {
                try {
                    Desktop desktop = Desktop.getDesktop();
                    URI oURL = new URI(AccesoVPNService.WEB_ERROR_VPN);
                    desktop.browse(oURL);
                } catch (IOException | URISyntaxException linkEx) {
                    new ErrorAlert(linkEx, "Ha ocurrido un error al tratar de abrir el navegador.").show();
                }
            });
            VBox box = new VBox(new Label(getErrorMessage()), helpLink);
            alert.getDialogPane().setContent(box);
            return alert;
        }
    };
    
    @Override
    protected Task<Boolean> createConnectTask() {
        return new VPNConnectTask();
    }
    
    @Override
    protected Task<Boolean> createDisconnectTask() {
        return new AlertingTask<Boolean>() {
            @Override
            protected Boolean doTask() throws Exception {
                updateErrorMsg(ERROR_DIS_VPN);
                updateMessage(disMsg);
                Process p = ProcessUtils.startProcess("rasdial.exe", "\"" + connectedVpn + "\"", "/DISCONNECT");
                ProcessUtils.waitAndCheck(p, 1000);
                return true;
            }
        };
    }
    //Initial class for creating VPN (Creatable impl)
    public abstract class CreateVPNTask extends AlertingTask<String> {
    
        protected final String name, server;
        
        protected CreateVPNTask(String vpnName, String vpnServer) {
            this(vpnName, vpnServer, "Ha habido un error mientras se creaba la conexión VPN.");
        }
        
        protected CreateVPNTask(String vpnName, String vpnServer, String errMsg) {
            super(errMsg);
            name = vpnName; 
            server = vpnServer;
        }

        public String getName() { return name; }
        public String getServer() { return server; }
        
        public Process runScript(String script) throws IOException, InterruptedException { return runScript(script, Input.NONE); }
        public Process runScript(String script, Input input) throws IOException, InterruptedException {
            Process p = ProcessUtils.runPsScript(script, input);
            String output = ProcessUtils.getOutput(p);
            if (p.waitFor() != 0) throw new IOException(output);
            //Nos toca comprobar este posible error siempre porque en el caso de que el Script Powershell lo lea de un archivo
            //(como es el caso de la VPN a la UPV), no devuelve correctamente un exitValue != 0 (lo cual indicaría que hay error).
            else if (output.contains("ResourceExists")) {
                updateErrorMsg("Ya existe una conexión VPN con el nombre indicado.\n\n"
                + "Compruebe si se trata de la conexión adecuada, pues puede que no sea necesario crear otra.");
                throw new IllegalArgumentException();
            }
            return p;
        }
        
        protected final File transcriptToTempFile(String name, String ext, Scanner sc) throws IOException {
            File temp = File.createTempFile(name, ext);
            //Just in case the task throws an exception, it ensures the temp file is deleted
            temp.deleteOnExit();
            //Copies the file
            try (PrintWriter pw = new PrintWriter(new FileOutputStream(temp), true)) {
                while (sc.hasNext()) { pw.println(sc.nextLine()); }
            } finally {
                sc.close();
            }
            //Returns the file created
            return temp;
        }
        
    }
    
}
