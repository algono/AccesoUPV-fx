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
public enum Input {
    NONE(null), ENTER(""), NO("N");
    
    private final String input;
    
    private Input(String in) {
        input = in;
    }
    
    public String getInput() {
        if (input == null) throw new UnsupportedOperationException("Input.NONE has no input.");
        return input; 
    }
}
