/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package accesoupv.model.services.drives;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import javafx.concurrent.Task;

/**
 *
 * @author aleja
 */
public class AccesoDriveDSICService extends AccesoDriveService {
    
    public static final String ERROR_INVALID_DATA = "El usuario o la contrase\u00f1a son incorrectos.";
    
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
    protected Task<Boolean> createConnectTask() {
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
            protected Boolean call() throws Exception {
                updateMessage("Conectando disco del DSIC...");
                Boolean res = false;
                try {
                    res = super.call();
                } catch (IOException ex) {
                    String out = ex.getMessage();
                    Throwable cause = null;
                    // 86 - Error del sistema "La contrase\u00f1a de red es incorrecta"
                    // 1326 - Error del sistema "El usuario o la contrase\u00f1a son incorrectos"
                    //Cuando las credenciales son erroneas, da uno de estos dos errores de forma arbitraria.
                    if (out.contains("86") || out.contains("1326")) {
                        updateErrorMsg(ERROR_INVALID_DATA);
                        out = "";
                        cause = new IllegalArgumentException();
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
                initMsg = "Desconectando disco del DSIC...";
                return super.call();
            }
        };
    }
    
}
