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
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Window;

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
    public CodeFiller(File in, Map<String, String> map) throws IOException {
        this(new FileInputStream(in), map);
    }
    public CodeFiller(InputStream in, Map<String, String> map) throws IOException {
        input = in;
        replaceMap = map;
    }
    public CodeFiller(File in, Map<String, String> map, File out) throws IOException {
        this(new FileInputStream(in), map, new FileOutputStream(out));
    }
    public CodeFiller(File in, Map<String, String> map, OutputStream out) throws IOException {
        this(new FileInputStream(in), map, out);
    }
    public CodeFiller(InputStream in, Map<String, String> map, File out) throws IOException {
        this(in, map, new FileOutputStream(out));
    }
    public CodeFiller(InputStream in, Map<String, String> map, OutputStream out) throws IOException {
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
    
    public File chooseInput(Window win, String filename, ExtensionFilter ex) throws FileNotFoundException {
        File in = chooseFile(win, filename, ex);
        if (in != null) input = new FileInputStream(in);
        return in;
    }
    public File chooseOutput(Window win, String filename, ExtensionFilter ex) throws IOException {
        File out = chooseFile(win, filename, ex);
        if (out != null) {
            out.createNewFile();
            output = new FileOutputStream(out);
        }
        return out;
    }
    private File chooseFile(Window win, String filename, ExtensionFilter ex) {
        //El usuario elige la ruta del output
        FileChooser fc = new FileChooser();
        fc.setInitialFileName(filename);
        fc.getExtensionFilters().add(ex);
        return fc.showSaveDialog(win);
    }
    
    /**
     * Transcripts the input into the output replacing the keywords using the map.
     */
    public void run() {
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
