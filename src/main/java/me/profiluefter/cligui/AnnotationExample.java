/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.profiluefter.cligui;

import me.profiluefter.cligui.annotations.CLIApplication;
import me.profiluefter.cligui.annotations.CLIOption;
import me.profiluefter.cligui.annotations.CLInject;

@CLIApplication("CLIGui Demo")
public class AnnotationExample {
    public static void main(String[] args) {
        CLI.launch(AnnotationExample.class);
    }

    @CLIOption(value="Persönliche Daten ausgeben", successMessage="Erfolgreich ausgeführt!")
    public static void exampleOne(
            @CLInject("Dein Vorname") String firstName, 
            @CLInject("Dein Nachname") String lastName, 
            @CLInject("Alter") int age) {
        System.out.format("Hallo %s %s, du bist %d Jahre alt!\n", firstName, lastName, age);
    }
}
