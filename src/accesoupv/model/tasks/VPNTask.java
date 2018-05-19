/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package accesoupv.model.tasks;

import java.io.IOException;

/**
 *
 * @author aleja
 */
public class VPNTask extends AccesoTask {
    
    //Webpage for possible VPN errors
    public static final String WEB_ERROR_VPN = "https://www.upv.es/contenidos/INFOACCESO/infoweb/infoacceso/dat/723787normalc.html";
    //Error messages
    public static final String ERROR_CON_VPN
            = "Ha habido un error desconocido al tratar de conectarse a la VPN.\n\n"
            + "Para más información, pulse 'Mostrar detalles'.";
    
    public static final String ERROR_NONEXISTANT_VPN = 
            "No existe ninguna VPN creada con el nombre registrado.\n"
            + "Debe establecer un nombre válido.";
    
    public static final String ERROR_INVALID_VPN = 
            "La VPN proporcionada no es válida, pues no es capaz de acceder a la UPV.\n"
            + "Establezca una válida.";
    
    public static final String ERROR_MISSING_DATA_VPN
            = "La VPN requiere datos que no le han sido proporcionados.\n\n"
            + "Compruebe manualmente que la VPN se conecta correctamente recordando tus datos, y vuelva a intentarlo."
            + "Para más información, pulse 'Mostrar detalles'.";
    
    public static final String ERROR_DIS_VPN = "No ha podido desconectarse la VPN. Deberá desconectarla manualmente.";   
    
    private final String VPN;
    
    public VPNTask(String vpn, boolean connecting) {
        super(connecting);
        VPN = vpn;
    }
    
    @Override
    protected void connect() throws Exception {
        setErrorMessage(ERROR_CON_VPN);
        updateMessage("Conectando con la UPV...");
        Process p = startProcess("cmd.exe", "/c", "rasdial \"" + VPN + "\"");
        Thread.sleep(1000);
        int exitValue = p.waitFor();
        if (exitValue != 0) {
            String out = getOutput(p);
            //623 - Código de error "No se encontró una VPN con ese nombre".
            if (out.contains("623")) {
                setErrorMessage(ERROR_NONEXISTANT_VPN);
                throw new IllegalArgumentException();
            //703 - Código de error "A la VPN le faltan datos"
            } else if (out.contains("703")) {
                setErrorMessage(ERROR_MISSING_DATA_VPN);
            }
            throw new IOException(out);
            
        }
    }
    
    @Override
    protected void disconnect() throws Exception {
        setErrorMessage(ERROR_DIS_VPN);
        updateMessage("Desconectando de la UPV...");
        Process p = startProcess("cmd.exe", "/c", "rasdial " + VPN + " /DISCONNECT");
        waitAndCheck(p, 1000);
    }
    
}
