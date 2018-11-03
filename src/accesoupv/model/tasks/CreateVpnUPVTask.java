/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package accesoupv.model.tasks;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Scanner;

/**
 *
 * @author Alejandro
 */
public class CreateVpnUPVTask extends CreateVPNTask {
    
    private static final String SCRIPT = 
    "$importXML = New-Object XML" + "\n"
    + "$importXML.Load(\"${env:temp}\\XMLNAME\")" + "\n"
    + "Add-VpnConnection -Name \"VPNNAME\" -ServerAddress \"VPNSERVER\" -AuthenticationMethod Eap -EncryptionLevel Required -RememberCredential -TunnelType Sstp -EapConfigXmlStream $importXML" + "\n";
    
    public CreateVpnUPVTask(String vpnName) {
        super(vpnName, "vpn.upv.es");
    }
    
    @Override
    protected void doTask() throws Exception {
        updateMessage("Creando conexión VPN...");
        File tempXml = createXml();
        runScript(SCRIPT);
        tempXml.delete();
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
}
