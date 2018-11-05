/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package accesoupv.model.services;

import javafx.concurrent.Task;

/**
 *
 * @author Alejandro
 */
public interface Creatable {
    //The AccesoService has a Task associated to create the Acceso.
    public Task getCreateTask();
}
