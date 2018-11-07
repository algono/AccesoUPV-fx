/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package accesoupv.model.services;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
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
        return new CreateVPNTask(VPN, "vpn.upv.es") {
            @Override
            protected String doTask() throws Exception {
                updateMessage("Creando conexión VPN...");
                File tempXml = createXml();
                runScript("$importXML = New-Object XML" + "\n"
                    + "$importXML.Load(\"${env:temp}\\" + tempXml.getName() +"\")" + "\n"
                    + "Add-VpnConnection -Name \"VPNNAME\" -ServerAddress \"VPNSERVER\" -AuthenticationMethod Eap -EncryptionLevel Required -RememberCredential -TunnelType Sstp -EapConfigXmlStream $importXML" + "\n");
                tempXml.delete();
                return name;
            }

            protected File createXml() throws IOException {
                InputStream xmlIn = getClass().getResourceAsStream("/accesoupv/resources/VPN_config.xml");
                File temp = File.createTempFile("temp", ".xml");
                //Just in case the task throws an exception, it ensures the temp file is deleted
                temp.deleteOnExit();
                //Copies the file
                try (Scanner sc = new Scanner(xmlIn); PrintWriter pw = new PrintWriter(new FileOutputStream(temp), true)) {
                    while (sc.hasNext()) { pw.println(sc.nextLine()); }
                }
                //Returns the file created
                return temp;
            }
        };
    }

    @Override
    protected Task<Boolean> createConnectTask() {
        return new VPNConnectTask();
    }
    
}
