/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package accesoupv.model.tasks;

import accesoupv.model.Dominio;
import accesoupv.model.ProcessUtils;
import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

/**
 *
 * @author aleja
 */
public class AccesoWService extends AccesoService {
    //Error messages
    public static final String ERROR_W = "Ha habido un error al tratar de conectarse al disco W. Pulse en 'Mostrar detalles' para más información.";
    public static final String ERROR_INVALID_USER = "El usuario especificado no existe.";
    public static final String ERROR_DIS_W = 
            "Ha habido un error al desconectar el disco W.\n\n"
            + "Compruebe que no tenga abierto un archivo/carpeta del disco e inténtelo de nuevo.\n\n"
            + "Si el error persiste, desconéctelo manualmente.";
    public static final String OPEN_FILES_WARNING = 
            "Existen archivos abiertos y/o búsquedas incompletas de directorios pendientes en el disco W. Si no los cierra antes de desconectarse, podría perder datos.\n\n"
            + "¿Desea continuar la desconexión y forzar el cierre?";
    private String user;
    private String drive;
    private Dominio dom;
    
    public AccesoWService(String wUser, String wDrive, Dominio wDom) {
        user = wUser;
        drive = wDrive;
        dom = wDom;
    }

    public String getUser() {
        return user;
    }

    public String getDrive() {
        return drive;
    }

    public Dominio getDomain() {
        return dom;
    }
    
    
    public String getDirW() {
        return "\\\\nasupv.upv.es\\" + dom.toString().toLowerCase() + "\\" + user.charAt(0) + "\\" + user;
    }
    
    @Override
    protected Task<Void> createConnectTask() {
        return new AlertingTask() {
            @Override
            protected void doTask() throws Exception {
                updateErrorMsg(ERROR_W);
                updateMessage("Accediendo al disco W...");
                Process p = ProcessUtils.startProcess("cmd.exe", "/c", "net", "use", drive, getDirW());
                try {
                    ProcessUtils.waitAndCheck(p, delay);
                } catch (IOException ex) {
                    String out = ex.getMessage();
                    // 55 - Error del sistema "El recurso no se encuentra disponible" (es decir, la dirW no existe, por tanto, el usuario no es válido).
                    if (out.contains("55")) {
                        updateErrorMsg(ERROR_INVALID_USER);
                        throw new IllegalArgumentException();
                    } else {
                        throw new IOException(out);
                    }
                } 
                if (drive.equals("*")) { //Si la unidad no estaba determinada, la obtiene del output del proceso.
                    String out = ProcessUtils.getOutput(p);
                    int indexOf = out.indexOf(':');
                    drive = out.substring(indexOf-1, indexOf+1);
                }
            }
        };
    }

    @Override
    protected Task<Void> createDisconnectTask() {
        return new AlertingTask() {
            @Override
            protected void doTask() throws Exception {
                updateErrorMsg(ERROR_DIS_W);
                updateMessage("Desconectando Disco W...");
                Process p = ProcessUtils.startProcess("cmd.exe", "/c", "net", "use", drive, "/delete");
                Thread.sleep(delay);
                int exitValue = p.waitFor();
                if (exitValue != 0) {
                    String out = ProcessUtils.getOutput(p);
                    //Esa secuencia es parte de "(S/N)", con lo que deducimos que nos pide confirmación (porque tenemos archivos abiertos)
                    if (out.contains("/N)")) {
                        final AtomicBoolean res = new AtomicBoolean();
                        final CountDownLatch latch = new CountDownLatch(1);
                        Platform.runLater(() -> {
                            Alert conf = new Alert(Alert.AlertType.WARNING, OPEN_FILES_WARNING);
                            conf.setHeaderText(null);
                            conf.getButtonTypes().setAll(ButtonType.OK, ButtonType.CANCEL);
                            Optional<ButtonType> confRes = conf.showAndWait();
                            res.set(confRes.isPresent() && confRes.get() == ButtonType.OK);
                            latch.countDown();
                        });
                        latch.await();
                        if (res.get()) {
                            //"/y" le pasa un "Sí" automáticamente, mientras que "/no" le pasaría un "No".
                            p = ProcessUtils.startProcess("cmd.exe", "/c", "net", "use", drive, "/delete", "/y");
                            ProcessUtils.waitAndCheck(p, 1000);
                        } else {
                            cancel();
                        }
                    } else {
                        throw new IOException(out);
                    }
                }
            }
        };
    }

    public void setUser(String user) {
        checkConnected();
        this.user = user;
    }

    public void setDrive(String drive) {
        checkConnected();
        this.drive = drive;
    }

    public void setDomain(Dominio domain) {
        checkConnected();
        dom = domain;
    }
    
}
