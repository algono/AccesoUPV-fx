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
 * @param <T> Service return type
 */
public abstract class AccesoService extends Service<Boolean> {
    
    private final ReadOnlyBooleanWrapper connectedProperty = new ReadOnlyBooleanWrapper(false);
    
    @Override
    protected void succeeded() {
        //Si todo fue bien, invierte el estado entre conectado y desconectado
        connectedProperty.set(!connectedProperty.get());
    }
    
    @Override
    public final Task<Boolean> createTask() {
        return connectedProperty.get() ? createDisconnectTask() : createConnectTask();
    }
    //La Task devuelve si la accion de conexion/desconexion se tuvo que realizar (puede que no hiciera falta)
    protected abstract Task<Boolean> createConnectTask();
    protected abstract Task<Boolean> createDisconnectTask();
    
    //Getters
    public boolean isConnected() { return connectedProperty.get(); }
    
    protected void checkConnected() {
        if (isConnected()) throw new IllegalStateException("You cannot change any variable while the service is connected.");
    }
    
    //Properties
    public ReadOnlyBooleanProperty connectedProperty() { return connectedProperty.getReadOnlyProperty(); }
    
}