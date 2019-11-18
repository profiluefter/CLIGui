/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor. Brigitte.
 */
package me.profiluefter.cligui;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Fabian Gurtner (fabian@profiluefter.me)
 */
public class CLIGui {
    private final List<MenuItem> menuItems = new ArrayList<>();
    private final Settings settings = new Settings();

    private final String programName;

    public CLIGui(String programName) {
        this.programName = programName;
    }

    public void addMenuItem(String name, MenuItemHandler handler) {
        menuItems.add(new MenuItem(name, handler));
    }

    private String generateMenu() {
        StringBuilder sb = new StringBuilder();

        if (settings.printBanner && (!settings.printBannerOnlyOnce || !settings.printedBanner)) {
            sb.append(generateBanner());
            settings.printedBanner = true;
        }

        for (int i = 0; i < menuItems.size(); i++) {
            sb.append(i + 1).append(") ").append(menuItems.get(i).name).append('\n');
        }
        sb.append(menuItems.size() + 1).append(") Beenden\n");

        sb.append("\nAuswahl: ");

        return sb.toString();
    }

    private String generateBanner() {
        StringBuilder sb = new StringBuilder();

        sb.append('╔');
        for (int i = 0; i < programName.length() + 2; i++) {
            sb.append('═');
        }
        sb.append("╗\n║ ");
        sb.append(this.programName);
        sb.append(" ║\n╚");
        for (int i = 0; i < programName.length() + 2; i++) {
            sb.append('═');
        }
        sb.append("╝\n");

        return sb.toString();
    }

    public void run() {
        do {
            System.out.print(generateMenu());
            int selection = CLIReader.read(Integer.class);

            if(selection < 1 || selection > menuItems.size()+1) {
                System.err.println("Ungültige Eingabe!");
                continue;
            }
            
            if (selection == menuItems.size() + 1) {
                return;
            }
            
            menuItems.get(selection - 1).handler.handle();
        } while (true);
    }

    private static class MenuItem {
        private final String name;
        private final MenuItemHandler handler;

        public MenuItem(String name, MenuItemHandler handler) {
            this.name = name;
            this.handler = handler;
        }
    }

    public static interface MenuItemHandler {
        public void handle();
    }

    public static class Settings {
        public boolean printBanner = true;
        public boolean printBannerOnlyOnce = true;

        private boolean printedBanner = false;
    }
}
