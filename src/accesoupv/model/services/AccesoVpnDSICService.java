/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package accesoupv.model.services;

import accesoupv.model.Input;
import javafx.concurrent.Task;

/**
 *
 * @author Alejandro
 */
public class AccesoVpnDSICService extends AccesoVPNService {

    public AccesoVpnDSICService(String vpn) {
        super(vpn);
    }

    @Override
    public Task getCreateTask() {
        return new CreateVPNTask(VPN, "r1-vpn.dsic.upv.es", Input.ENTER) {
            @Override
            protected void doTask() throws Exception {
                updateMessage("Creando VPN al DSIC...");
                runScript("Add-VpnConnection -Name \"VPNNAME\" -ServerAddress \"VPNSERVER\" -AuthenticationMethod MSChapv2 -EncryptionLevel Optional -L2tpPsk dsic -RememberCredential -TunnelType L2tp" + "\n");
            }
        };
    }
    
}
