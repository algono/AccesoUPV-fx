/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package accesoupv.model.services.drives;

import accesoupv.model.ProcessUtils;
import accesoupv.model.services.AccesoService;
import accesoupv.model.AlertingTask;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

/**
 *
 * @author aleja
 */
public abstract class AccesoDriveService extends AccesoService {
    //Error messages
    public static final String ERROR_CON = "Ha habido un error al tratar de conectarse al disco. Pulse en 'Mostrar detalles' para más información.";
    public static final String ERROR_INVALID_USER = "El usuario especificado no existe.";
    public static final String ERROR_DIS = 
            "Ha habido un error al desconectar el disco.\n\n"
            + "Compruebe que no tenga abierto un archivo/carpeta del disco e inténtelo de nuevo.\n\n"
            + "Si el error persiste, desconéctelo manualmente.";
    public static final String OPEN_FILES_WARNING = 
            "Existen archivos abiertos y/o búsquedas incompletas de directorios pendientes en el disco. Si no los cierra antes de desconectarse, podría perder datos.\n\n"
            + "¿Desea continuar la desconexión y forzar el cierre?";
    
    protected String user, drive, connectedDrive;
    
    public AccesoDriveService(String wUser, String wDrive) {
        user = wUser;
        drive = wDrive;
    }

    public String getUser() {
        return user;
    }
    
    public String getDrive() {
        return drive;
    }
    
    public String getConnectedDrive() {
        return isConnected() ? connectedDrive : drive;
    }
    
    @Override
    protected void succeeded() {
        super.succeeded();
        connectedDrive = isConnected() ? drive : null;
    }
    
    abstract class DriveConnectTask extends AlertingTask<Boolean> {
        
        protected String initMsg = "Accediendo al disco...";
        
        public DriveConnectTask() { super(ERROR_CON); }
        public DriveConnectTask(String errMsg) { super(errMsg); }
        
        public abstract String getDir();
        public List<String> getExtraArgs() { return null; }
    
        @Override
        protected Boolean doTask() throws Exception {
            updateMessage(initMsg);
            List<String> args = new ArrayList<>(Arrays.asList("cmd.exe", "/c", "net", "use", drive, getDir()));
            List<String> extraArgs = getExtraArgs();
            if (extraArgs != null) args.addAll(getExtraArgs());
            Process p = ProcessUtils.startProcess(args);
            try {
                ProcessUtils.waitAndCheck(p, delay);
            } catch (IOException ex) {
                String out = ex.getMessage();
                Throwable cause = null;
                // 55 - Error del sistema "El recurso no se encuentra disponible" (es decir, la dirW no existe, por tanto, el usuario no es válido).
                if (out.contains("55")) {
                    updateErrorMsg(ERROR_INVALID_USER);
                    out = null;
                    cause = new IllegalArgumentException();
                }
                throw new IOException(out, cause);
            } 
            if (drive.equals("*")) { //Si la unidad no estaba determinada, la obtiene del output del proceso.
                String out = ProcessUtils.getOutput(p);
                int indexOf = out.indexOf(':');
                drive = out.charAt(indexOf-1) + ":";
            }
            return true;
        }
    }
    
    class DriveDisconnectTask extends AlertingTask<Boolean> {
        
        protected String initMsg = "Desconectando disco...";
        
        public DriveDisconnectTask() { super(ERROR_DIS); }
        public DriveDisconnectTask(String errMsg) { super(errMsg); }
        
        @Override
        protected Boolean doTask() throws Exception {
            updateMessage(initMsg);
            Process p = ProcessUtils.startProcess("cmd.exe", "/c", "net", "use", connectedDrive, "/delete");
            Thread.sleep(delay);
            int exitValue = p.waitFor();
            if (exitValue != 0) {
                String out = ProcessUtils.getOutput(p);
                //Esa secuencia es parte de "(S/N)", con lo que deducimos que nos pide confirmación (porque tenemos archivos abiertos)
                if (out.contains("/N)")) {
                    final AtomicBoolean res = new AtomicBoolean();
                    final CountDownLatch latch = new CountDownLatch(1);
                    Platform.runLater(() -> {
                        Alert conf = new Alert(Alert.AlertType.WARNING, OPEN_FILES_WARNING, ButtonType.OK, ButtonType.CANCEL);
                        conf.setHeaderText(null);
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
            return true;
        }
    }

    public void setUser(String user) {
        this.user = user;
    }
    public void setDrive(String drive) {
        this.drive = drive; 
    }
}
