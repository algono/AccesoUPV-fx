/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package accesoupv.model.services;

import accesoupv.controller.AyudaController;
import accesoupv.model.Input;
import javafx.concurrent.Task;
import javafx.scene.control.Alert;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.VBox;

/**
 *
 * @author Alejandro
 */
public class AccesoVpnDSICService extends AccesoVPNService {

    public static final String PORTAL_DSIC_WEB = "portal-ng.dsic.cloud";
    
    public AccesoVpnDSICService(String vpn) {
        super(vpn);
        conMsg = "Conectando con el DSIC...";
        disMsg = "Desconectando del DSIC...";
        iServer = PORTAL_DSIC_WEB;
    }

    @Override
    public CreateVPNTask getCreateTask() {
        return new CreateVPNTask(vpn, "r1-vpn.dsic.upv.es") {
            @Override
            protected String call() throws Exception {
                updateMessage("Creando VPN al DSIC...");
                runScript(
                    "Add-VpnConnection -Name \"" + vpn + "\" -ServerAddress \"" + server + "\" -AuthenticationMethod MSChapv2 -EncryptionLevel Optional -L2tpPsk dsic -RememberCredential -TunnelType L2tp" + "\n",
                    Input.ENTER
                );
                return name;
            }
        };
    }
    
    @Override
    protected Task<Boolean> createConnectTask() {
        return new VPNConnectTask() {
            @Override
            public Alert getErrorAlert(Throwable ex) {
                Alert alert = super.getErrorAlert(ex);
                Label label = new Label("\nSi se trataba del error 789, vuelve a intentarlo y si persiste,\n haz click en este link:");
                Hyperlink helpLink = new Hyperlink("Click aquí para ayuda");
                helpLink.setOnAction(e -> {
                    AyudaController ayuda = AyudaController.getInstance();
                    ayuda.getStage().show();
                    for (TitledPane t : ayuda.getIndex().getPanes()) {
                        //Comprueba si contiene "no", "vpn" y "dsic" en ese orden, con lo que sea en medio
                        // (. = Cualquier carácter) (* = Cualquier número de veces)
                        if (t.getText().toLowerCase().matches(".*no.*vpn.*dsic.*")) {
                            t.setExpanded(true); break; //En cuanto lo encuentra lo maximiza y sale del bucle
                        }
                    }
                });
                VBox box = new VBox(alert.getDialogPane().getContent(), label, helpLink);
                alert.getDialogPane().setContent(box);
                return alert;
            }
        };
    }
    
}
