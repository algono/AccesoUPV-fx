/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package accesoupv.model.services.drives;

import accesoupv.model.Dominio;
import java.io.IOException;
import javafx.concurrent.Task;

/**
 *
 * @author aleja
 */
public class AccesoDriveWService extends AccesoDriveService {
    
    private Dominio dom;
    public static final String ERROR_BUG = "Ha habido un error inesperado al conectarse.\n\n"
            + "Si se encuentra conectado a la UPV desde una red WiFi (como UPVNET), reconecte la red manualmente.\n\n"
            + "Si se encuentra conectado desde la VPN, se comenzará ahora el proceso de reconexión.";
    
    public AccesoDriveWService(String wUser, String wDrive, Dominio wDom) {
        super(wUser, wDrive);
        dom = wDom;
    }

    public Dominio getDomain() {
        return dom;
    }     
    
    public void setDomain(Dominio domain) {
        checkConnected();
        dom = domain;
    }
    
    @Override
    protected Task<Boolean> createConnectTask() {
        return new DriveConnectTask() {
            @Override
            public String getDir() {
                return "\\\\nasupv.upv.es\\" + dom.getDir() + "\\" + user.charAt(0) + "\\" + user;
            }
            @Override
            protected Boolean call() throws Exception {
                updateMessage("Conectando disco W...");
                Boolean res = false;
                try {
                    res = super.call();
                } catch (IOException ex) {
                    String out = ex.getMessage();
                    Throwable cause = null;
                    /**
                     * 1223 - Error del sistema "El usuario o la contraseña son incorrectos".
                     * Esto es un bug porque debería obtener las credenciales de la conexión con la UPV
                     * (ya sea VPN o WiFi).
                     * Por ahora el bug sólo se ha visto desde red WiFi, y la solución es reconectarse.
                    */ 
                    if (out.contains("1223")) {
                        updateErrorMsg(ERROR_BUG);
                        out = "";
                        cause = new IOException("bug");
                    }
                    throw new IOException(out, cause);
                }
                return res;
            }
        };
    }

    @Override
    protected Task<Boolean> createDisconnectTask() {
        return new DriveDisconnectTask() {
            @Override
            protected Boolean call() throws Exception {
                initMsg = "Desconectando disco W...";
                return super.call();
            }
        };
    }
    
}
