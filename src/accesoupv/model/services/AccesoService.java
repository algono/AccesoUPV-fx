/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package accesoupv.model.services;

import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.concurrent.Service;
import javafx.concurrent.Task;

/**
 *
 * @author Alejandro
 */
public abstract class AccesoService extends Service<Boolean> {
    
    //Task delay (being used or not depends on task implementation)
    protected int delay = 0;
    
    private final ReadOnlyBooleanWrapper connectedProperty = new ReadOnlyBooleanWrapper(false);
    
    @Override
    protected void succeeded() {
        //Si todo fue bien, invierte el estado entre conectado y desconectado
        if (getValue()) connectedProperty.set(!connectedProperty.get());
    }
    
    @Override
    public final Task<Boolean> createTask() {
        return connectedProperty.get() ? createDisconnectTask() : createConnectTask();
    }
    //La Task devuelve si la acción de conexión/desconexión se tuvo que realizar (puede que no hiciera falta)
    protected abstract Task<Boolean> createConnectTask();
    protected abstract Task<Boolean> createDisconnectTask();
    
    //Getters
    public boolean isConnected() { return connectedProperty.get(); }
    
    protected void checkConnected() {
        if (isConnected()) throw new IllegalStateException("You cannot change any variable while the service is connected.");
    }
    
    public int getDelay() { return delay; }
    
    //Setters
    public void setDelay(int d) { delay = d; }
    
    //Properties
    public ReadOnlyBooleanProperty connectedProperty() { return connectedProperty.getReadOnlyProperty(); }
    
}