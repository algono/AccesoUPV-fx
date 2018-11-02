/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package accesoupv.model.tasks;

/**
 *
 * @author Alejandro
 */
public class CreateVpnDSICTask extends CreateVPNTask {
    
    private static final String SCRIPT = "Add-VpnConnection -Name \"VPNNAME\" -ServerAddress \"VPNSERVER\" -AuthenticationMethod MSChapv2 -EncryptionLevel Optional -L2tpPsk dsic -RememberCredential -TunnelType L2tp" + "\n";
    
    public CreateVpnDSICTask(String vpnName) {
        super(vpnName, "r1-vpn.dsic.upv.es");
    }
    
    @Override
    protected Void call() throws Exception {
        updateMessage("Creando VPN al DSIC...");
        runScript(SCRIPT);
        return null;
    }
    
}
