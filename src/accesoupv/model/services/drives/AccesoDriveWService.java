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
    protected Task<Void> createConnectTask() {
        return new DriveConnectTask() {
            @Override
            public String getDir() {
                return "\\\\nasupv.upv.es\\" + dom.toString().toLowerCase() + "\\" + user.charAt(0) + "\\" + user;
            }
            @Override
            protected void doTask() throws Exception {
                initMsg = "Accediendo al disco W...";
                super.doTask();
            }
        };
    }

    @Override
    protected Task<Void> createDisconnectTask() {
        return new DriveDisconnectTask() {
            @Override
            protected void doTask() throws Exception {
                initMsg = "Desconectando disco W...";
                super.doTask();
            }
        };
    }
    
}
