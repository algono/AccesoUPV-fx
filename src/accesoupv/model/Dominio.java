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
public enum Dominio {
    ALUMNO("alumnos"),
    UPVNET("discos");
    
    private final String dir;
    
    private Dominio(String dir) {
        this.dir = dir;
    }
    
    public String getDir() { return dir; }
}
