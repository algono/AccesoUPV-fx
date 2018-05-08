/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package accesoupv.model.tasks;

import static accesoupv.Launcher.acceso;
import java.io.IOException;

/**
 *
 * @author aleja
 */
public class WTask extends AccesoTask {
    //Error messages
    public static final String ERROR_W = "Ha habido un error al tratar de conectarse al disco W. Inténtelo de nuevo más tarde.";
    public static final String ERROR_INVALID_USER = "No existe el usuario especificado.\nDebe establecer un usuario válido.";
    public static final String ERROR_DIS_W = 
            "Ha habido un error al desconectar el disco W.\n\n"
            + "Compruebe que no tenga abierto un archivo/carpeta del disco e inténtelo de nuevo.\n\n"
            + "Si el error persiste, desconéctelo manualmente.";
    
    public WTask(boolean nState) {
        super(nState);
    }
    public WTask(boolean nState, boolean showErr) {
        super(nState, showErr);
    }

    @Override
    protected void connect() throws Exception {
        setErrorMessage(ERROR_W);
        updateMessage("Accediendo al disco W...");
        String drive = acceso.getDrive();
        Process p = startProcess("cmd.exe", "/c", "net use " + drive + " " + acceso.getDirW());
        Thread.sleep(1000);
        int exitValue = p.waitFor();
        if (exitValue != 0) {
            String out = getOutput(p);
            // 55 - Error del sistema "El recurso no se encuentra disponible" (es decir, la dirW no existe, por tanto, el usuario no es válido).
            if (out.contains("55")) {
                setErrorMessage(ERROR_INVALID_USER);
                throw new IllegalArgumentException(out);
            } else {
                throw new IOException(out);
            }
        }
    }

    @Override
    protected void disconnect() throws Exception {
        setErrorMessage(ERROR_DIS_W);
        updateMessage("Desconectando Disco W...");
        Process p = startProcess("cmd.exe", "/c", "net use " + acceso.getDrive() + " /delete");
        waitAndCheck(p, 1000, false);
    }
    
}
