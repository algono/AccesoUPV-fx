/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package accesoupv.model.tasks;

import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.concurrent.Service;
import javafx.concurrent.Task;

/**
 *
 * @author Alejandro
 */
public abstract class AccesoService extends Service<Void> {
    
    //Timeout
    public static final int PROCESS_TIMEOUT = 5000; //5 seconds
    
    private final ReadOnlyBooleanWrapper connectedProperty = new ReadOnlyBooleanWrapper(false);
    
    @Override
    protected void succeeded() {
        connectedProperty.set(!connectedProperty.get()); //Si todo fue bien, invierte el estado entre conectado y desconectado
    }
    
    @Override
    public final Task<Void> createTask() {
        return connectedProperty.get() ? createDisconnectTask() : createConnectTask();
    }
    
    protected abstract Task<Void> createConnectTask();
    protected abstract Task<Void> createDisconnectTask();
    
    //Getters
    public boolean isConnected() { return connectedProperty.get(); }
    
    public void checkConnected() {
        if (isConnected()) throw new IllegalStateException("You cannot change any variable while the service is connected.");
    }
    
    //Properties
    public ReadOnlyBooleanProperty connectedProperty() { return connectedProperty.getReadOnlyProperty(); }
    
}