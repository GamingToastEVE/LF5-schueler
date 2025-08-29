package de.obj;

/**
 * Main entry point for the GoodFood GmbH cash register system.
 */
public class Main {
    public static void main(String[] args) {
        // Check if GUI mode is requested
        boolean useGui = true; // Default to GUI
        
        for (String arg : args) {
            if ("--console".equals(arg) || "-c".equals(arg)) {
                useGui = false;
                break;
            }
        }
        
        if (useGui) {
            SwingKassensystemApp.main(args);
        } else {
            KassensystemApp.main(args);
        }
    }
}
