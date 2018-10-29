/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package accesoupv.model.services.drives;

import java.util.Arrays;
import java.util.List;
import javafx.concurrent.Task;

/**
 *
 * @author aleja
 */
public class AccesoDriveDSICService extends AccesoDriveService {
    
    private String password = "";
    
    public AccesoDriveDSICService(String wUser, String wDrive) {
        super(wUser, wDrive);
    }
    
    public AccesoDriveDSICService(String wUser, String pass, String wDrive) {
        super(wUser, wDrive);
        password = pass;
    }
    
    public String getPass() { return password; }
    public boolean isPassDefined() { return !password.isEmpty(); }
    public void setPass(String p) {
        checkConnected();
        password = p;
    }
    
    @Override
    protected Task<Void> createConnectTask() {
        return new DriveConnectTask() {            
            @Override
            public String getDir() {
                return "\\\\fileserver.dsic.upv.es\\" + user;
            }
            
            @Override
            public List<String> getExtraArgs() {
                return Arrays.asList(password, "/USER:DSIC\\" + user);
            }
            
            @Override
            protected void doTask() throws Exception {
                initMsg = "Accediendo al disco del DSIC...";
                super.doTask();
            }
        };
    }

    @Override
    protected Task<Void> createDisconnectTask() {
        return new DriveDisconnectTask() {
            @Override
            protected void doTask() throws Exception {
                initMsg = "Desconectando disco del DSIC...";
                super.doTask();
            }
        };
    }
    
}
