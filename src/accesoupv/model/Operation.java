/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package accesoupv.model;

/**
 *
 * @author Alejandro
 */
public enum Operation {
    CONTINUE(true), RETRY(false), ABORT(false), CANCEL(true);
    
    private final boolean success;
    
    private Operation(boolean suc) {
        success = suc;
    }
    
    public boolean isSucceeded() {
        return success;
    }
}
