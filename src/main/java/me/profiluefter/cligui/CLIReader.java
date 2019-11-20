/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor. Brigitte.
 */
package me.profiluefter.cligui;

import java.util.InputMismatchException;
import java.util.Scanner;

/**
 *
 * @author Fabian Gurtner (fabian@profiluefter.me)
 */
public class CLIReader {
    private static final Scanner SCANNER = new Scanner(System.in);
    
    static {
        SCANNER.useDelimiter("\n");
    }
    
    public static String readLine() {
        return SCANNER.nextLine();
    }
    
    public static String readLine(String prompt) {
        System.out.format("%s: ", prompt);
        return SCANNER.nextLine();
    }
    
    public static String[] readLines(String[] prompts) {
        String[] results = new String[prompts.length];
        for (int i = 0; i < prompts.length; i++) {
            results[i] = readLine(prompts[i]);
        }
        return results;
    }
    
    @SuppressWarnings("UnnecessaryBoxing")
    public static <T> T read(Class<T> clazz) {
        switch(clazz.getName()) {
            case "java.lang.Boolean":
                String line = readLine();
                if(
                        line.equalsIgnoreCase("true")
                        || line.equalsIgnoreCase("y")
                        || line.equalsIgnoreCase("j")
                        || line.equalsIgnoreCase("1")
                        )
                    return (T)Boolean.TRUE;
                else if(
                        line.equalsIgnoreCase("false")
                        || line.equalsIgnoreCase("n")
                        || line.equalsIgnoreCase("0")
                        )
                    return (T)Boolean.FALSE;
                throw new InputMismatchException();
            case "java.lang.Character":
                line = readLine();
                if(line.length() != 1)
                    throw new InputMismatchException();
                else
                    return (T)Character.valueOf(line.charAt(0));
            case "java.lang.Float":
                return (T)Float.valueOf(SCANNER.nextLine());
            case "java.lang.Integer":
                return (T)Integer.valueOf(SCANNER.nextLine());
            case "java.lang.Long": // De Methode is long
                return (T)Long.valueOf(SCANNER.nextLine());
            case "java.lang.Short":
                return (T)Short.valueOf(SCANNER.nextLine());
            case "java.lang.Double":
                return (T)Double.valueOf(SCANNER.nextLine());
            case "java.lang.String":
                return (T)readLine();
            default:
                throw new IllegalArgumentException("Unknown type: "+clazz.getName());
        }
    }
    
    public static <T> T read(Class<T> clazz, String prompt) {
        System.out.format("%s: ", prompt);
        return read(clazz);
    }
}