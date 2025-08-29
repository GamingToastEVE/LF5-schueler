package de.obj;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Scanner;

/**
 * Main cash register application for GoodFood GmbH.
 * Console-based interface simulating a touchscreen system.
 */
public class KassensystemApp {
    private final Scanner scanner;
    private final UserService userService;
    private final ProduktService produktService;
    private final VerkaufService verkaufService;
    private User currentUser;
    private Bon currentBon;

    public KassensystemApp() {
        this.scanner = new Scanner(System.in);
        this.userService = new UserService();
        this.produktService = new ProduktService();
        this.verkaufService = new VerkaufService();
    }

    public static void main(String[] args) {
        System.out.println("===========================================");
        System.out.println("    GoodFood GmbH - Kassensystem v1.0     ");
        System.out.println("===========================================");
        
        KassensystemApp app = new KassensystemApp();
        app.run();
    }

    public void run() {
        // Initialize database
        DatabaseManager.getInstance();
        
        while (true) {
            if (currentUser == null) {
                if (!login()) {
                    continue;
                }
            }
            
            showMainMenu();
            int choice = readInt("Wählen Sie eine Option: ");
            
            switch (choice) {
                case 1:
                    startNewSale();
                    break;
                case 2:
                    if (currentUser.isFilialleiter()) {
                        manageProducts();
                    } else {
                        System.out.println("Nur Filialleiter haben Zugriff auf Produktverwaltung!");
                        pause();
                    }
                    break;
                case 3:
                    if (currentUser.isFilialleiter()) {
                        cancelSale();
                    } else {
                        System.out.println("Nur Filialleiter können Verkäufe stornieren!");
                        pause();
                    }
                    break;
                case 4:
                    if (currentUser.isFilialleiter()) {
                        showStatistics();
                    } else {
                        System.out.println("Nur Filialleiter haben Zugriff auf Statistiken!");
                        pause();
                    }
                    break;
                case 5:
                    currentUser = null;
                    System.out.println("Abgemeldet.");
                    break;
                case 6:
                    System.out.println("Auf Wiedersehen!");
                    return;
                default:
                    System.out.println("Ungültige Auswahl!");
                    break;
            }
        }
    }

    private boolean login() {
        System.out.println("\n=== ANMELDUNG ===");
        System.out.print("PIN eingeben: ");
        String pin = scanner.nextLine();

        User user = userService.authenticate(pin);
        if (user != null) {
            currentUser = user;
            System.out.printf("\nWillkommen, %s (%s)!\n", 
                user.getFullName(), 
                user.isFilialleiter() ? "Filialleiter" : "Verkäufer");
            pause();
            return true;
        } else {
            System.out.println("Ungültige PIN! Bitte versuchen Sie es erneut.");
            System.out.println("\nHinweis - Demo PINs:");
            System.out.println("Maria Schmidt (Filialleiter): 1234");
            System.out.println("Johannes Müller (Verkäufer): 5678");
            System.out.println("Emma Fischer (Verkäufer): 9999");
            pause();
            return false;
        }
    }

    private void showMainMenu() {
        clearScreen();
        System.out.println("===========================================");
        System.out.println("           HAUPTMENÜ");
        System.out.printf("    Angemeldet: %s (%s)\n", 
            currentUser.getFullName(),
            currentUser.isFilialleiter() ? "Filialleiter" : "Verkäufer");
        System.out.println("===========================================");
        System.out.println("1. Neuer Verkauf");
        System.out.println("2. Produktverwaltung" + (currentUser.isFilialleiter() ? "" : " (FL)"));
        System.out.println("3. Verkauf stornieren" + (currentUser.isFilialleiter() ? "" : " (FL)"));
        System.out.println("4. Statistiken" + (currentUser.isFilialleiter() ? "" : " (FL)"));
        System.out.println("5. Abmelden");
        System.out.println("6. Beenden");
        System.out.println("===========================================");
    }

    private void startNewSale() {
        currentBon = new Bon(currentUser);
        
        while (true) {
            clearScreen();
            System.out.println("=== NEUER VERKAUF ===");
            displayCurrentBon();
            
            System.out.println("\n1. Artikel per Barcode hinzufügen");
            System.out.println("2. Artikel aus Liste wählen");
            System.out.println("3. Artikel entfernen");
            System.out.println("4. Verkauf abschließen");
            System.out.println("5. Verkauf abbrechen");
            
            int choice = readInt("Wählen Sie eine Option: ");
            
            switch (choice) {
                case 1:
                    addProductByBarcode();
                    break;
                case 2:
                    addProductFromList();
                    break;
                case 3:
                    removeProductFromBon();
                    break;
                case 4:
                    if (completeSale()) {
                        return;
                    }
                    break;
                case 5:
                    System.out.println("Verkauf abgebrochen.");
                    pause();
                    return;
                default:
                    System.out.println("Ungültige Auswahl!");
                    break;
            }
        }
    }

    private void addProductByBarcode() {
        System.out.print("Barcode eingeben (oder ENTER für Demo-Artikel): ");
        String barcode = scanner.nextLine().trim();
        
        Produkt product;
        if (barcode.isEmpty()) {
            // Demo: Use first product if no barcode entered
            List<Produkt> products = produktService.getAllProducts();
            if (!products.isEmpty()) {
                product = products.get(0);
                System.out.println("Demo: Verwende " + product.getBezeichnung());
            } else {
                System.out.println("Keine Produkte verfügbar!");
                pause();
                return;
            }
        } else {
            product = produktService.findByBarcode(barcode);
            if (product == null) {
                System.out.println("Produkt mit Barcode '" + barcode + "' nicht gefunden!");
                pause();
                return;
            }
        }
        
        addProductToBon(product);
    }

    private void addProductFromList() {
        List<Produkt> products = produktService.getAllProducts();
        if (products.isEmpty()) {
            System.out.println("Keine Produkte verfügbar!");
            pause();
            return;
        }
        
        System.out.println("\n=== PRODUKTLISTE ===");
        for (int i = 0; i < products.size(); i++) {
            System.out.printf("%d. %s\n", i + 1, products.get(i));
        }
        
        int choice = readInt("Produkt wählen (1-" + products.size() + "): ");
        if (choice >= 1 && choice <= products.size()) {
            addProductToBon(products.get(choice - 1));
        } else {
            System.out.println("Ungültige Auswahl!");
            pause();
        }
    }

    private void addProductToBon(Produkt product) {
        double menge;
        if (product.isWeightBased()) {
            menge = readDouble("Gewicht eingeben (kg): ");
        } else {
            menge = readDouble("Anzahl eingeben: ");
        }
        
        if (menge > 0) {
            Artikel artikel = new Artikel(product, menge);
            currentBon.addArtikel(artikel);
            System.out.printf("Hinzugefügt: %s\n", artikel);
        } else {
            System.out.println("Ungültige Menge!");
        }
        pause();
    }

    private void removeProductFromBon() {
        if (currentBon.getPositionen().isEmpty()) {
            System.out.println("Keine Artikel im Bon!");
            pause();
            return;
        }
        
        System.out.println("\n=== ARTIKEL ENTFERNEN ===");
        displayBonItems();
        
        int index = readInt("Artikel-Nummer zum Entfernen: ") - 1;
        if (index >= 0 && index < currentBon.getPositionen().size()) {
            Artikel removed = currentBon.getPositionen().get(index);
            currentBon.removeArtikel(index);
            System.out.printf("Entfernt: %s\n", removed);
        } else {
            System.out.println("Ungültige Artikel-Nummer!");
        }
        pause();
    }

    private boolean completeSale() {
        if (currentBon.getPositionen().isEmpty()) {
            System.out.println("Keine Artikel im Bon!");
            pause();
            return false;
        }
        
        System.out.printf("\nGesamtbetrag: %.2f EUR\n", currentBon.getBruttoGesamtbetrag());
        System.out.print("Verkauf abschließen? (j/n): ");
        String confirm = scanner.nextLine().trim().toLowerCase();
        
        if ("j".equals(confirm) || "ja".equals(confirm)) {
            int bonId = verkaufService.saveSale(currentBon);
            if (bonId > 0) {
                System.out.println("\n=== BON ===");
                System.out.println(currentBon.generateReceiptText());
                System.out.println("Verkauf erfolgreich abgeschlossen!");
                pause();
                return true;
            } else {
                System.out.println("Fehler beim Speichern des Verkaufs!");
                pause();
                return false;
            }
        }
        return false;
    }

    private void manageProducts() {
        while (true) {
            clearScreen();
            System.out.println("=== PRODUKTVERWALTUNG ===");
            System.out.println("1. Alle Produkte anzeigen");
            System.out.println("2. Neues Produkt hinzufügen");
            System.out.println("3. Zurück");
            
            int choice = readInt("Wählen Sie eine Option: ");
            
            switch (choice) {
                case 1:
                    showAllProducts();
                    break;
                case 2:
                    addNewProduct();
                    break;
                case 3:
                    return;
                default:
                    System.out.println("Ungültige Auswahl!");
                    break;
            }
        }
    }

    private void showAllProducts() {
        List<Produkt> products = produktService.getAllProducts();
        
        System.out.println("\n=== ALLE PRODUKTE ===");
        if (products.isEmpty()) {
            System.out.println("Keine Produkte vorhanden.");
        } else {
            for (Produkt product : products) {
                System.out.printf("ID: %d, Barcode: %s, %s\n", 
                    product.getPid(), 
                    product.getBarcode().isEmpty() ? "---" : product.getBarcode(),
                    product);
            }
        }
        pause();
    }

    private void addNewProduct() {
        System.out.println("\n=== NEUES PRODUKT ===");
        System.out.print("Bezeichnung: ");
        String bezeichnung = scanner.nextLine().trim();
        
        if (bezeichnung.isEmpty()) {
            System.out.println("Bezeichnung ist erforderlich!");
            pause();
            return;
        }
        
        double preis = readDouble("Preis (EUR): ");
        if (preis <= 0) {
            System.out.println("Preis muss größer als 0 sein!");
            pause();
            return;
        }
        
        System.out.print("Barcode (optional): ");
        String barcode = scanner.nextLine().trim();
        
        System.out.print("Gewichtsbasiert? (j/n): ");
        boolean isWeightBased = "j".equals(scanner.nextLine().trim().toLowerCase());
        
        Produkt product = new Produkt();
        product.setBezeichnung(bezeichnung);
        product.setPreis(preis);
        product.setBarcode(barcode);
        product.setWeightBased(isWeightBased);
        product.setKid(1); // Default category
        product.setMwst(0.19); // Default 19% VAT
        
        if (produktService.saveProduct(product)) {
            System.out.println("Produkt erfolgreich hinzugefügt!");
        } else {
            System.out.println("Fehler beim Hinzufügen des Produkts!");
        }
        pause();
    }

    private void cancelSale() {
        List<Bon> activeReceipts = verkaufService.getActiveReceipts();
        
        if (activeReceipts.isEmpty()) {
            System.out.println("Keine aktiven Belege zum Stornieren vorhanden!");
            pause();
            return;
        }
        
        System.out.println("\n=== BELEGE ZUM STORNIEREN ===");
        for (int i = 0; i < activeReceipts.size(); i++) {
            Bon bon = activeReceipts.get(i);
            System.out.printf("%d. Bon-Nr: %d | Datum: %s | Verkäufer: %s | Betrag: %.2f EUR%n", 
                i + 1, 
                bon.getBonId(), 
                bon.getFormattedDatum(), 
                bon.getVerkaufer().getFullName(), 
                bon.getBruttoGesamtbetrag());
        }
        
        int choice = readInt("Beleg wählen (1-" + activeReceipts.size() + "): ");
        if (choice < 1 || choice > activeReceipts.size()) {
            System.out.println("Ungültige Auswahl!");
            pause();
            return;
        }
        
        Bon selectedBon = activeReceipts.get(choice - 1);
        
        // Load full receipt details
        Bon bon = verkaufService.getReceiptById(selectedBon.getBonId());
        if (bon == null) {
            System.out.println("Fehler beim Laden der Beleg-Details!");
            pause();
            return;
        }
        
        System.out.println("\n=== BON DETAILS ===");
        System.out.println(bon.generateReceiptText());
        
        System.out.print("Wirklich stornieren? (j/n): ");
        String confirm = scanner.nextLine().trim().toLowerCase();
        
        if ("j".equals(confirm) || "ja".equals(confirm)) {
            System.out.print("Grund für Stornierung: ");
            String reason = scanner.nextLine().trim();
            
            if (verkaufService.cancelSale(bon.getBonId(), currentUser, reason)) {
                System.out.println("Verkauf erfolgreich storniert!");
            } else {
                System.out.println("Fehler beim Stornieren!");
            }
        }
        pause();
    }

    private void showStatistics() {
        System.out.println("\n=== VERKAUFSSTATISTIKEN ===");
        
        LocalDate today = LocalDate.now();
        String todayStr = today.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String weekAgoStr = today.minusDays(7).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        
        // Today's statistics
        VerkaufService.SalesStatistics todayStats = verkaufService.getSalesStatistics(todayStr, todayStr);
        System.out.println("\n=== HEUTE ===");
        System.out.println(todayStats);
        
        // Last 7 days statistics
        VerkaufService.SalesStatistics weekStats = verkaufService.getSalesStatistics(weekAgoStr, todayStr);
        System.out.println("\n=== LETZTE 7 TAGE ===");
        System.out.println(weekStats);
        
        pause();
    }

    // Helper methods
    private void displayCurrentBon() {
        if (currentBon.getPositionen().isEmpty()) {
            System.out.println("Bon ist leer.");
        } else {
            System.out.println("Aktuelle Artikel:");
            displayBonItems();
            System.out.printf("\nZwischensumme: %.2f EUR (netto)\n", currentBon.getNettoGesamtbetrag());
            System.out.printf("MwSt: %.2f EUR\n", currentBon.getGesamtMwst());
            System.out.printf("GESAMT: %.2f EUR (brutto)\n", currentBon.getBruttoGesamtbetrag());
        }
    }

    private void displayBonItems() {
        for (int i = 0; i < currentBon.getPositionen().size(); i++) {
            System.out.printf("%d. %s\n", i + 1, currentBon.getPositionen().get(i));
        }
    }

    private int readInt(String prompt) {
        while (true) {
            try {
                System.out.print(prompt);
                int value = scanner.nextInt();
                scanner.nextLine(); // consume newline
                return value;
            } catch (InputMismatchException e) {
                System.out.println("Bitte geben Sie eine gültige Zahl ein!");
                scanner.nextLine(); // consume invalid input
            }
        }
    }

    private double readDouble(String prompt) {
        while (true) {
            try {
                System.out.print(prompt);
                double value = scanner.nextDouble();
                scanner.nextLine(); // consume newline
                return value;
            } catch (InputMismatchException e) {
                System.out.println("Bitte geben Sie eine gültige Zahl ein!");
                scanner.nextLine(); // consume invalid input
            }
        }
    }

    private void clearScreen() {
        // Simple screen clear for demo
        for (int i = 0; i < 50; i++) {
            System.out.println();
        }
    }

    private void pause() {
        System.out.print("\nDrücken Sie ENTER um fortzufahren...");
        scanner.nextLine();
    }
}