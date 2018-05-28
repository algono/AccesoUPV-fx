/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package accesoupv.model.tasks;

/**
 *
 * @author aleja
 */
public class VPNTask extends AccesoTask {
    
    //Webpage for possible VPN errors
    public static final String WEB_ERROR_VPN = "https://www.upv.es/contenidos/INFOACCESO/infoweb/infoacceso/dat/723787normalc.html";
    //Error messages
    public static final String ERROR_CON_VPN
            = "Se ha producido un error al tratar de conectarse a la VPN.\n\n"
            + "Compruebe que el nombre de la VPN está bien escrito y vuelva a intentarlo.";
    
    public static final String ERROR_INVALID_VPN = 
            "La VPN proporcionada no es válida, pues no es capaz de acceder a la UPV.\n"
            + "Establezca una válida.";
    
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
        Process p = startProcess("rasphone.exe", "-d", "\"" + VPN + "\"");
        waitAndCheck(p, 1000);
        //Si el proceso fue correctamente pero la VPN no esta conectada, se entiende que el usuario canceló la operacion
        p = AccesoTask.startProcess("rasdial.exe");
        waitAndCheck(p, 0);
        if (!getOutput(p).contains(VPN)) cancel();
    }
    
    @Override
    protected void disconnect() throws Exception {
        setErrorMessage(ERROR_DIS_VPN);
        updateMessage("Desconectando de la UPV...");
        Process p = startProcess("rasdial.exe", "\"" + VPN + "\"", "/DISCONNECT");
        waitAndCheck(p, 1000);
    }
    
}
