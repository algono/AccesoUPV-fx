/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package accesoupv.model.tasks;

import static accesoupv.Launcher.acceso;

/**
 *
 * @author Alejandro
 */
public class DisconnectTask extends AccesoTask {
    //Error messages
    public static final String ERROR_DIS_VPN = "No ha podido desconectarse la VPN. Deberá desconectarla manualmente.";
    public static final String ERROR_DIS_W = 
            "Ha habido un error al desconectar el disco W.\n\n"
            + "Compruebe que no tenga abierto un archivo/carpeta del disco e inténtelo de nuevo.\n\n"
            + "Si el error persiste, desconéctelo manualmente.";
    //Decides if connecting to VPN, W, or both
    private final String source;
    
    public DisconnectTask(String src) { this(src, true); }
    public DisconnectTask(String src, boolean showError) {
        super(showError);
        source = src;
    }
    
    @Override
    protected Void call() throws Exception {
        switch(source) {
            case "VPN": disconnectVPN(); break;
            case "W": disconnectW(); break;
            default:
                disconnectW();
                disconnectVPN();
                break;
        }
        return null;
    }
    
    //Desconectar Disco W (si estaba conectado)
    public Void disconnectW() throws Exception {
        setErrorMessage(ERROR_DIS_W);
        updateMessage("Desconectando Disco W...");
        Process p = startProcess("cmd.exe", "/c", "net use " + acceso.getDrive() + " /delete");
        waitAndCheck(p, 1000, false);
        return null;
    }
    //Desconectar VPN
    public Void disconnectVPN() throws Exception {
        setErrorMessage(ERROR_DIS_VPN);
        exitOnFailed = true;
        updateMessage("Desconectando de la UPV...");
        Process p = startProcess("cmd.exe", "/c", "rasdial " + acceso.getVPN() + " /DISCONNECT");
        waitAndCheck(p, 1000);
        return null;
    }
}
