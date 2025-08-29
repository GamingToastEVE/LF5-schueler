package de.obj;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

/**
 * Create a visual demo of the GUI and save it as image.
 */
public class GUIDemo {
    public static void main(String[] args) {
        try {
            // Create a simple representation of the GUI
            BufferedImage image = new BufferedImage(800, 600, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = image.createGraphics();
            
            // Set high quality rendering
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            
            // Background
            g2d.setColor(Color.WHITE);
            g2d.fillRect(0, 0, 800, 600);
            
            // Header
            g2d.setColor(new Color(70, 130, 180));
            g2d.fillRect(0, 0, 800, 100);
            
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Arial", Font.BOLD, 24));
            FontMetrics fm = g2d.getFontMetrics();
            String title = "GoodFood GmbH - Kassensystem";
            int titleX = (800 - fm.stringWidth(title)) / 2;
            g2d.drawString(title, titleX, 40);
            
            g2d.setFont(new Font("Arial", Font.PLAIN, 14));
            fm = g2d.getFontMetrics();
            String status = "Nicht angemeldet";
            int statusX = (800 - fm.stringWidth(status)) / 2;
            g2d.drawString(status, statusX, 80);
            
            // Buttons
            String[] buttonTexts = {
                "Anmelden", "Neuer Verkauf",
                "Produktverwaltung", "Verkauf stornieren",
                "Statistiken", "Abmelden"
            };
            
            int buttonWidth = 200;
            int buttonHeight = 80;
            int startX = (800 - (2 * buttonWidth + 20)) / 2;
            int startY = 150;
            
            for (int i = 0; i < buttonTexts.length; i++) {
                int row = i / 2;
                int col = i % 2;
                
                int x = startX + col * (buttonWidth + 20);
                int y = startY + row * (buttonHeight + 20);
                
                // Button background
                if (i == 0) { // Login button enabled
                    g2d.setColor(new Color(240, 248, 255));
                } else { // Other buttons disabled
                    g2d.setColor(new Color(245, 245, 245));
                }
                g2d.fillRect(x, y, buttonWidth, buttonHeight);
                
                // Button border
                g2d.setColor(Color.GRAY);
                g2d.drawRect(x, y, buttonWidth, buttonHeight);
                
                // Button text
                if (i == 0) {
                    g2d.setColor(Color.BLACK);
                } else {
                    g2d.setColor(Color.GRAY);
                }
                g2d.setFont(new Font("Arial", Font.PLAIN, 16));
                fm = g2d.getFontMetrics();
                int textX = x + (buttonWidth - fm.stringWidth(buttonTexts[i])) / 2;
                int textY = y + (buttonHeight + fm.getAscent()) / 2;
                g2d.drawString(buttonTexts[i], textX, textY);
            }
            
            // Footer
            g2d.setColor(Color.DARK_GRAY);
            g2d.setFont(new Font("Arial", Font.PLAIN, 12));
            String footer = "Java Swing GUI - Erstellt für LF5 Schülerversion";
            fm = g2d.getFontMetrics();
            int footerX = (800 - fm.stringWidth(footer)) / 2;
            g2d.drawString(footer, footerX, 580);
            
            g2d.dispose();
            
            // Save the image
            ImageIO.write(image, "png", new File("gui_main_window.png"));
            System.out.println("GUI mockup saved as gui_main_window.png");
            
            // Create login dialog mockup
            BufferedImage loginImage = new BufferedImage(400, 200, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d2 = loginImage.createGraphics();
            g2d2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            
            // Background
            g2d2.setColor(Color.WHITE);
            g2d2.fillRect(0, 0, 400, 200);
            g2d2.setColor(Color.BLACK);
            g2d2.drawRect(0, 0, 399, 199);
            
            // Title
            g2d2.setColor(Color.BLACK);
            g2d2.setFont(new Font("Arial", Font.BOLD, 16));
            fm = g2d2.getFontMetrics();
            String loginTitle = "Anmeldung";
            int loginTitleX = (400 - fm.stringWidth(loginTitle)) / 2;
            g2d2.drawString(loginTitle, loginTitleX, 30);
            
            // PIN label
            g2d2.setFont(new Font("Arial", Font.PLAIN, 14));
            g2d2.drawString("PIN eingeben:", 50, 80);
            
            // PIN field
            g2d2.setColor(Color.WHITE);
            g2d2.fillRect(150, 65, 150, 25);
            g2d2.setColor(Color.BLACK);
            g2d2.drawRect(150, 65, 150, 25);
            g2d2.drawString("****", 155, 82);
            
            // Buttons
            g2d2.setColor(new Color(240, 248, 255));
            g2d2.fillRect(100, 130, 80, 30);
            g2d2.fillRect(220, 130, 80, 30);
            g2d2.setColor(Color.BLACK);
            g2d2.drawRect(100, 130, 80, 30);
            g2d2.drawRect(220, 130, 80, 30);
            
            fm = g2d2.getFontMetrics();
            g2d2.drawString("Anmelden", 100 + (80 - fm.stringWidth("Anmelden")) / 2, 148);
            g2d2.drawString("Abbrechen", 220 + (80 - fm.stringWidth("Abbrechen")) / 2, 148);
            
            g2d2.dispose();
            
            ImageIO.write(loginImage, "png", new File("gui_login_dialog.png"));
            System.out.println("Login dialog mockup saved as gui_login_dialog.png");
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}