/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package accesoupv.model;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

/**
 * Métodos estáticos de utilidad para tratar procesos
 * @author Alejandro
 */
public final class ProcessUtils {
    
    public static boolean waitAndCheck(Process p) throws IOException, InterruptedException {
        return waitAndCheck(p, 0);
    }
    public static boolean waitAndCheck(Process p, int tMin) throws IOException, InterruptedException {
        return waitAndCheck(p, tMin, true);
    }
    public static boolean waitAndCheck(Process p, int tMin, boolean showExMsg) throws IOException, InterruptedException {
        if (tMin > 0) Thread.sleep(tMin);
        int exitValue = p.waitFor();
        if (exitValue != 0) {
            String msg = showExMsg ? getOutput(p) : "";
            throw new IOException(msg);
        }
        return exitValue == 0;
    }
    public static String getOutput(Process p) throws IOException {
        String res = "";
        try (Scanner out = new Scanner(p.getInputStream())) {
            while (out.hasNext()) { res += out.nextLine() + "\n"; }
        }
        return res;
    }
    public static Process startProcess(String... args) throws IOException {
        return startProcess(Arrays.asList(args));
    }
    public static Process startProcess(List<String> args) throws IOException {
        ProcessBuilder builder = new ProcessBuilder(args);
        builder.redirectErrorStream(true);
        return builder.start();
    }
    
    public static Process runPsScript(String script) throws IOException, InterruptedException { return runPsScript(script, Input.NONE); }
    public static Process runPsScript(String script, Input input) throws IOException, InterruptedException {
        //Runs the script and waits for its completion
        Process p = new ProcessBuilder("powershell.exe", "-ExecutionPolicy", "ByPass", "-Command", script).start();
        //If the script needs an input, it is passed through a pipe.
        if (input != Input.NONE) {
            try (PrintWriter pw = new PrintWriter(p.getOutputStream(), true)) {
                pw.println(input.getInput());
            }
        }
        return p;
    }
    
}
