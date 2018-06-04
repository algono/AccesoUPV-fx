/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package accesoupv.model.tasks;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Scanner;
import javafx.concurrent.Task;

/**
 *
 * @author Alejandro
 */
public class CreateVPNTask extends Task<Void> {
    
    private static final String SCRIPT = 
    "$importXML = New-Object XML" + "\n"
    + "$importXML.Load(\"${env:temp}\\XMLNAME\")" + "\n"
    + "Add-VpnConnection -Name \"VPNNAME\" -ServerAddress \"vpn.upv.es\" -AuthenticationMethod Eap -EncryptionLevel Required -RememberCredential -TunnelType Sstp -EapConfigXmlStream $importXML" + "\n";
    private final String vpn;
    
    public CreateVPNTask(String vpnName) {
        vpn = vpnName;
    }
    
    @Override
    protected Void call() throws Exception {
        updateMessage("Creando conexión VPN...");
        InputStream xmlIn = getClass().getResourceAsStream("/accesoupv/resources/VPN_config.xml");
        File temp = File.createTempFile("temp", ".ps1");
        File tempXml = File.createTempFile("temp", ".xml");
        //Just in case the task throws an exception, it ensures the temp files are deleted
        temp.deleteOnExit(); tempXml.deleteOnExit();
        //Copies both files
        try (Scanner sc = new Scanner(SCRIPT); PrintWriter pw = new PrintWriter(new FileOutputStream(temp), true)) {
            while (sc.hasNext()) {
                pw.println(sc.nextLine().replaceAll("VPNNAME", vpn).replaceAll("XMLNAME", tempXml.getName()));
            }
        }
        try (Scanner sc = new Scanner(xmlIn); PrintWriter pw = new PrintWriter(new FileOutputStream(tempXml), true)) {
            while (sc.hasNext()) { pw.println(sc.nextLine()); }
        }
        //Runs the script and waits for its completion               
        Process p = new ProcessBuilder("powershell.exe", "-ExecutionPolicy", "ByPass", "-Command", temp.getAbsolutePath()).start();
        p.waitFor();
        //Deletes the temp files
        temp.delete(); tempXml.delete();
        return null;
    }
    
}
