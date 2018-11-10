/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package accesoupv.model.services;

import accesoupv.model.ProcessUtils;
import java.io.File;
import java.util.Scanner;
import javafx.concurrent.Task;

/**
 *
 * @author Alejandro
 */
public class AccesoVpnUPVService extends AccesoVPNService {
    
    public static final String UPV_SERVER = "www.upv.es";
    
    public AccesoVpnUPVService(String vpn) {
        super(vpn);
        conMsg = "Conectando con la UPV...";
        disMsg = "Desconectando de la UPV...";
        iServer = UPV_SERVER;
    }

    @Override
    public Task getCreateTask() {
        return new CreateVPNTask(vpn, "vpn.upv.es") {
            @Override
            protected String doTask() throws Exception {
                updateMessage("Creando conexión VPN...");
                File tempXml = transcriptToTempFile("temp", ".xml", new Scanner(getClass().getResourceAsStream("/accesoupv/resources/VPN_config.xml")));
                
                String script = "$importXML = New-Object XML" + "\n"
                    + "$importXML.Load(\"${env:temp}\\" + tempXml.getName() +"\")" + "\n"
                    + "Add-VpnConnection -Name \"" + vpn + "\" -ServerAddress \""+ server + "\" -AuthenticationMethod Eap -EncryptionLevel Required -RememberCredential -TunnelType Sstp -EapConfigXmlStream $importXML" + "\n";
                File temp = transcriptToTempFile("temp", ".ps1", new Scanner(script));
                
                ProcessUtils.runPsScript(temp.getAbsolutePath()).waitFor();
                
                tempXml.delete(); temp.delete();
                return name;
            }
            
        };
    }
    
}
