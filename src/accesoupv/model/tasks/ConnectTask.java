/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package accesoupv.model.tasks;

import static accesoupv.Launcher.acceso;
import java.io.IOException;
import java.net.InetAddress;

/**
 *
 * @author Alejandro
 */
public class ConnectTask extends AccesoTask {

    //Webpage for possible VPN errors
    public static final String WEB_ERROR_VPN = "https://www.upv.es/contenidos/INFOACCESO/infoweb/infoacceso/dat/723787normalc.html";
    //Error messages
    public static final String ERROR_VPN
            = "Ha habido un error desconocido al tratar de conectarse a la VPN.\n\n"
            + "Para más información, pulse 'Mostrar detalles'.";
    public static final String ERROR_W = "Ha habido un error al tratar de conectarse al disco W. Inténtelo de nuevo más tarde.";
    public static final String ERROR_INVALID_VPN = "No existe ninguna VPN creada con el nombre registrado.\nDebe establecer un nombre válido.";
    public static final String ERROR_INVALID_USER = "No existe el usuario especificado.\nDebe establecer un usuario válido.";
    public static final String ERROR_MISSING_DATA_VPN
            = "La VPN requiere datos que no le han sido proporcionados.\n\n"
            + "Compruebe manualmente que la VPN se conecta correctamente recordando tus datos, y vuelva a intentarlo.";
    //Decides if connecting to VPN, W, or both
    private final String destination;

    public ConnectTask(String dest) {
        this(dest, true);
    }

    public ConnectTask(String dest, boolean showError) {
        super(showError);
        destination = dest;
    }

    @Override
    protected Void call() throws Exception {
        switch (destination) {
            case "VPN":
                connectVPN();
                break;
            case "W":
                accessW();
                break;
            default:
                connectVPN();
                accessW();
                break;
        }
        return null;
    }

    public void connectVPN() throws Exception {
        setErrorMessage(ERROR_VPN);
        updateMessage("Conectando con la UPV...");
        Process p = startProcess("cmd.exe", "/c", "rasdial \"" + acceso.getVPN() + "\"");
        Thread.sleep(1000);
        int exitValue = p.waitFor();
        if (exitValue != 0) {
            String out = getOutput(p);
            //623 - Código de error "No se encontró una VPN con ese nombre".
            if (out.contains("623")) {
                setErrorMessage(ERROR_INVALID_VPN);
                throw new IllegalArgumentException(out);
                //703 - Código de error "A la VPN le faltan datos"
            } else if (out.contains("703")) {
                setErrorMessage(ERROR_MISSING_DATA_VPN);
                throw new IllegalStateException(out);
            } else {
                throw new IOException(out);
            }
        } else {
            //Si desde la VPN a la que se conectó no se puede acceder a la UPV, se entiende que ha elegido una incorrecta.
            try {
                if (!InetAddress.getByName("www.upv.es").isReachable(PING_TIMEOUT)) {
                    throw new IllegalArgumentException();
                }
            } catch (IOException ex) {}
        }
    }

    public void accessW() throws Exception {
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
}
