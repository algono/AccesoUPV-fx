/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package accesoupv.model.tasks;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Scanner;

/**
 *
 * @author Alejandro
 */
public abstract class CreateVPNTask extends AlertingTask {
    
    protected final String name, server;
    protected Input input = Input.NONE;
    
    protected CreateVPNTask(String vpnName, String vpnServer) {
        super("Ha habido un error mientras se creaba la conexión VPN.");
        name = vpnName; 
        server = vpnServer;
    }
    
    public String getName() { return name; }
    public String getServer() { return server; }
    
    protected final void runScript(String script) throws IOException, InterruptedException {
        File temp = File.createTempFile("temp", ".ps1");
        //Just in case the task throws an exception, it ensures the temp file is deleted
        temp.deleteOnExit();
        //Fills the new file with the script
        try (Scanner sc = new Scanner(script); PrintWriter pw = new PrintWriter(new FileOutputStream(temp), true)) {
            while (sc.hasNext()) {
                pw.println(sc.nextLine().replaceAll("VPNNAME", name)
                        .replaceAll("VPNSERVER", server));
            }
        }
        //Runs the script and waits for its completion
        Process p = new ProcessBuilder("powershell.exe", "-ExecutionPolicy", "ByPass", "-Command", temp.getAbsolutePath()).start();
        //If the script needs an input, it is passed through a pipe.
        if (input != Input.NONE) {
            try (PrintWriter pw = new PrintWriter(p.getOutputStream(), true)) {
                pw.println(input.getInput());
            }
        }
        p.waitFor();
        //Deletes the temp file
        temp.delete();
    }
    
}
