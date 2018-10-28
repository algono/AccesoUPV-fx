/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package accesoupv.model.tasks.DiscosW;

import accesoupv.model.tasks.*;
import accesoupv.model.Dominio;

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
      
    @Override
    public String getDir() {
        return "\\\\nasupv.upv.es\\" + dom.toString().toLowerCase() + "\\" + user.charAt(0) + "\\" + user;
    }
    
    public void setDomain(Dominio domain) {
        checkConnected();
        dom = domain;
    }
    
    @Override
    protected String getConMsg() { return "Accediendo al disco W..."; }
    @Override
    protected String getDisMsg() { return "Desconectando disco W..."; }
    
}
