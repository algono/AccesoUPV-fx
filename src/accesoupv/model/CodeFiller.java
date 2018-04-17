/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package accesoupv.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

/**
 *
 * @author Alejandro
 */
public class CodeFiller {
    //Constants
    public static final char DEFAULT_SYMBOL = '_';
    //Private instances
    private InputStream input;
    private Map<String, String> replaceMap;
    private char keySymbol = DEFAULT_SYMBOL;
    private OutputStream output;
    //Constructors
    public CodeFiller(InputStream in, Map<String, String> map, OutputStream out) {
        input = in;
        replaceMap = map;
        output = out;
    }
    //Getters
    public InputStream getInput() { return input; }
    public Map<String, String> getMap() { return replaceMap; }
    public char getSymbol() { return keySymbol; }
    public OutputStream getOutput() { return output; }
    //Setters
    public void setInput(InputStream in) { input = in; }
    public void setInput(File in) throws FileNotFoundException { input = new FileInputStream(in); }
    public void setMap(Map<String, String> map) { replaceMap = map; }
    public void setSymbol(char symbol) { keySymbol = symbol; }
    public void setOutput(OutputStream out) { output = out; }
    public void setOutput(File out) throws FileNotFoundException { output = new FileOutputStream(out); }
    
    /**
     * Transcripts the input into the output replacing the keywords using the map.
     */
    public void transcript() {
        try (Scanner sc = new Scanner(input); PrintWriter pw = new PrintWriter(output)) {
            Set<String> keySet = replaceMap.keySet();
            while (sc.hasNext()) {
                String line = sc.nextLine();
                Iterator<String> keys = keySet.iterator();
                while (keys.hasNext() && line.indexOf(keySymbol)!= -1) {
                    String key = keys.next();
                    line = line.replaceAll(keySymbol + key, replaceMap.get(key));
                }
                pw.println(line);
            }
        }
    }
}