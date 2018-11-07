/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package accesoupv.model;

import java.io.IOException;
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
    
}
