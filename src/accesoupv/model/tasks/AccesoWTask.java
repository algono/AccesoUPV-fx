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
public class AccesoWTask extends AccesoTask {
    //Error messages
    public static final String ERROR_W = "Ha habido un error al tratar de conectarse al disco W. Pulse en 'Mostrar detalles' para más información.";
    public static final String ERROR_INVALID_USER = "El usuario especificado no existe.";
    public static final String ERROR_DIS_W = 
            "Ha habido un error al desconectar el disco W.\n\n"
            + "Compruebe que no tenga abierto un archivo/carpeta del disco e inténtelo de nuevo.\n\n"
            + "Si el error persiste, desconéctelo manualmente.";
    
    private final String user, drive;
    
    public AccesoWTask(String wUser, String wDrive, boolean connecting) {
        super(connecting);
        user = wUser;
        drive = wDrive;
    }
    
    public String getDirW() {
        return "\\\\nasupv.upv.es\\alumnos\\" + user.charAt(0) + "\\" + user;
    }
    
    @Override
    protected void connect() throws Exception {
        updateErrorMsg(ERROR_W);
        updateMessage("Accediendo al disco W...");
        Process p = startProcess("cmd.exe", "/c", "net", "use", drive, getDirW());
        Thread.sleep(1000);
        int exitValue = p.waitFor();
        if (exitValue != 0) {
            String out = getOutput(p);
            // 55 - Error del sistema "El recurso no se encuentra disponible" (es decir, la dirW no existe, por tanto, el usuario no es válido).
            if (out.contains("55")) {
                updateErrorMsg(ERROR_INVALID_USER);
                throw new IllegalArgumentException();
            } else {
                throw new IOException(out);
            }
        }
    }

    @Override
    protected void disconnect() throws Exception {
        updateErrorMsg(ERROR_DIS_W);
        updateMessage("Desconectando Disco W...");
        Process p = startProcess("cmd.exe", "/c", "net", "use", drive, "/delete");
        waitAndCheck(p, 1000, false);
    }
    
}
