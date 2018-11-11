/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package accesoupv.model.services.drives;

import accesoupv.model.Dominio;
import javafx.concurrent.Task;

/**
 *
 * @author aleja
 */
public class AccesoDriveWService extends AccesoDriveService {
    
    private Dominio dom;
    
    public AccesoDriveWService(String wUser, String wDrive, Dominio wDom) {
        super(wUser, wDrive);
        dom = wDom;
    }

    public Dominio getDomain() {
        return dom;
    }     
    
    public void setDomain(Dominio domain) {
        checkConnected();
        dom = domain;
    }
    
    @Override
    protected Task<Boolean> createConnectTask() {
        return new DriveConnectTask() {
            @Override
            public String getDir() {
                return "\\\\nasupv.upv.es\\" + dom.getDir() + "\\" + user.charAt(0) + "\\" + user;
            }
            @Override
            protected Boolean doTask() throws Exception {
                updateMessage("Conectando disco W...");
                return super.doTask();
            }
        };
    }

    @Override
    protected Task<Boolean> createDisconnectTask() {
        return new DriveDisconnectTask() {
            @Override
            protected Boolean doTask() throws Exception {
                initMsg = "Desconectando disco W...";
                return super.doTask();
            }
        };
    }
    
}
