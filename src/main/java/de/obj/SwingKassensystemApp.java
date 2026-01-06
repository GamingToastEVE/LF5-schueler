package de.obj;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

/**
 * Swing GUI application for the cash register system.
 */
public class SwingKassensystemApp extends JFrame {
    private static final int DEFAULT_CATEGORY_ID = 1;
    private static final double DEFAULT_VAT_RATE = 0.19;
    private static final double MIN_VAT_RATE = 0.0;
    private static final double MAX_VAT_RATE = 100.0;

    private final UserService userService;
    private final ProduktService produktService;
    private final VerkaufService verkaufService;
    private User currentUser;
    private Bon currentBon;
    private JLabel statusLabel;
    private JButton loginButton;
    private JButton newSaleButton;
    private JButton productManagementButton;
    private JButton statisticsButton;
    private JButton cancelSaleButton;
    private JButton logoutButton;

    public SwingKassensystemApp() {
        this.userService = new UserService();
        this.produktService = new ProduktService();
        this.verkaufService = new VerkaufService();
        
        initializeGUI();
        updateButtonStates();
    }

    private void initializeGUI() {
        setTitle("GoodFood GmbH - Kassensystem v1.0");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);
        
        // Create menu bar
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("Datei");
        JMenuItem exitItem = new JMenuItem("Beenden");
        exitItem.addActionListener(e -> System.exit(0));
        fileMenu.add(exitItem);
        menuBar.add(fileMenu);
        setJMenuBar(menuBar);

        // Main panel
        JPanel mainPanel = new JPanel(new BorderLayout());
        
        // Header panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        headerPanel.setBackground(new Color(70, 130, 180));
        
        JLabel titleLabel = new JLabel("GoodFood GmbH - Kassensystem", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel, BorderLayout.CENTER);
        
        statusLabel = new JLabel("Nicht angemeldet", SwingConstants.CENTER);
        statusLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        statusLabel.setForeground(Color.WHITE);
        headerPanel.add(statusLabel, BorderLayout.SOUTH);
        
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // Button panel
        JPanel buttonPanel = new JPanel(new GridLayout(3, 2, 20, 20));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(30, 50, 30, 50));
        
        loginButton = createButton("Anmelden", e -> showLoginDialog());
        newSaleButton = createButton("Neuer Verkauf", e -> showNewSaleDialog());
        productManagementButton = createButton("Produktverwaltung", e -> showProductManagement());
        cancelSaleButton = createButton("Verkauf stornieren", e -> showCancelSaleDialog());
        statisticsButton = createButton("Statistiken", e -> showStatistics());
        logoutButton = createButton("Abmelden", e -> logout());
        
        buttonPanel.add(loginButton);
        buttonPanel.add(newSaleButton);
        buttonPanel.add(productManagementButton);
        buttonPanel.add(cancelSaleButton);
        buttonPanel.add(statisticsButton);
        buttonPanel.add(logoutButton);
        
        mainPanel.add(buttonPanel, BorderLayout.CENTER);
        
        add(mainPanel);
    }

    private JButton createButton(String text, ActionListener listener) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.PLAIN, 16));
        button.setPreferredSize(new Dimension(200, 80));
        button.addActionListener(listener);
        return button;
    }

    private void updateButtonStates() {
        boolean loggedIn = currentUser != null;
        boolean isManager = loggedIn && currentUser.isFilialleiter();
        
        loginButton.setEnabled(!loggedIn);
        newSaleButton.setEnabled(loggedIn);
        productManagementButton.setEnabled(isManager);
        cancelSaleButton.setEnabled(isManager);
        statisticsButton.setEnabled(isManager);
        logoutButton.setEnabled(loggedIn);
        
        if (loggedIn) {
            statusLabel.setText("Angemeldet: " + currentUser.getFullName() + 
                    " (" + (currentUser.isFilialleiter() ? "Filialleiter" : "Verkäufer") + ")");
        } else {
            statusLabel.setText("Nicht angemeldet");
        }
    }

    private void showLoginDialog() {
        JDialog loginDialog = new JDialog(this, "Anmeldung", true);
        loginDialog.setSize(400, 200);
        loginDialog.setLocationRelativeTo(this);
        
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("PIN eingeben:"), gbc);
        
        JPasswordField pinField = new JPasswordField(10);
        gbc.gridx = 1;
        panel.add(pinField, gbc);
        
        JButton loginBtn = new JButton("Anmelden");
        JButton cancelBtn = new JButton("Abbrechen");
        
        loginBtn.addActionListener(e -> {
            String pin = new String(pinField.getPassword());
            User user = userService.authenticate(pin);
            if (user != null) {
                currentUser = user;
                updateButtonStates();
                loginDialog.dispose();
                JOptionPane.showMessageDialog(this, 
                    "Willkommen, " + user.getFullName() + "!");
            } else {
                JOptionPane.showMessageDialog(loginDialog, 
                    "Ungültige PIN! Bitte versuchen Sie es erneut.\n\n" +
                    "Demo PINs:\n" +
                    "Maria Schmidt (Filialleiter): 1234\n" +
                    "Johannes Müller (Verkäufer): 5678\n" +
                    "Emma Fischer (Verkäufer): 9999", 
                    "Anmeldung fehlgeschlagen", JOptionPane.ERROR_MESSAGE);
                pinField.setText("");
            }
        });
        
        cancelBtn.addActionListener(e -> loginDialog.dispose());
        
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(loginBtn);
        buttonPanel.add(cancelBtn);
        
        gbc.gridx = 0; gbc.gridy = 1;
        gbc.gridwidth = 2;
        panel.add(buttonPanel, gbc);
        
        loginDialog.add(panel);
        loginDialog.setVisible(true);
    }

    private void showNewSaleDialog() {
        currentBon = new Bon(currentUser);
        
        JDialog saleDialog = new JDialog(this, "Neuer Verkauf", true);
        saleDialog.setSize(800, 600);
        saleDialog.setLocationRelativeTo(this);
        
        JPanel mainPanel = new JPanel(new BorderLayout());
        
        // Bon items list
        DefaultListModel<Artikel> listModel = new DefaultListModel<>();
        JList<Artikel> itemsList = new JList<>(listModel);
        itemsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        JScrollPane scrollPane = new JScrollPane(itemsList);
        scrollPane.setPreferredSize(new Dimension(400, 300));
        scrollPane.setBorder(BorderFactory.createTitledBorder("Artikel im Bon"));
        
        // Total labels
        JLabel nettoLabel = new JLabel("Netto: 0.00 EUR");
        JLabel mwstLabel = new JLabel("MwSt: 0.00 EUR");
        JLabel totalLabel = new JLabel("GESAMT: 0.00 EUR");
        totalLabel.setFont(new Font("Arial", Font.BOLD, 16));
        
        JPanel totalsPanel = new JPanel(new GridLayout(3, 1));
        totalsPanel.setBorder(BorderFactory.createTitledBorder("Gesamtbetrag"));
        totalsPanel.add(nettoLabel);
        totalsPanel.add(mwstLabel);
        totalsPanel.add(totalLabel);
        
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.add(scrollPane, BorderLayout.CENTER);
        leftPanel.add(totalsPanel, BorderLayout.SOUTH);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new GridLayout(6, 1, 5, 5));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JButton addProductButton = new JButton("Artikel hinzufügen");
        JButton removeProductButton = new JButton("Artikel entfernen");
        JButton completeSaleButton = new JButton("Verkauf abschließen");
        JButton cancelButton = new JButton("Abbrechen");
        
        addProductButton.addActionListener(e -> showAddProductDialog(listModel, nettoLabel, mwstLabel, totalLabel));
        
        removeProductButton.addActionListener(e -> {
            int selectedIndex = itemsList.getSelectedIndex();
            if (selectedIndex >= 0) {
                currentBon.removeArtikel(selectedIndex);
                listModel.remove(selectedIndex);
                updateTotals(nettoLabel, mwstLabel, totalLabel);
            } else {
                JOptionPane.showMessageDialog(saleDialog, "Bitte wählen Sie einen Artikel zum Entfernen aus.");
            }
        });
        
        completeSaleButton.addActionListener(e -> {
            if (currentBon.getPositionen().isEmpty()) {
                JOptionPane.showMessageDialog(saleDialog, "Keine Artikel im Bon!");
                return;
            }
            
            int result = JOptionPane.showConfirmDialog(saleDialog,
                String.format("Verkauf abschließen?\nGesamtbetrag: %.2f EUR", currentBon.getBruttoGesamtbetrag()),
                "Verkauf abschließen", JOptionPane.YES_NO_OPTION);
            
            if (result == JOptionPane.YES_OPTION) {
                int bonId = verkaufService.saveSale(currentBon);
                if (bonId > 0) {
                    showReceiptDialog(currentBon);
                    saleDialog.dispose();
                } else {
                    JOptionPane.showMessageDialog(saleDialog, "Fehler beim Speichern des Verkaufs!");
                }
            }
        });
        
        cancelButton.addActionListener(e -> saleDialog.dispose());
        
        buttonPanel.add(addProductButton);
        buttonPanel.add(removeProductButton);
        buttonPanel.add(new JLabel()); // Spacer
        buttonPanel.add(completeSaleButton);
        buttonPanel.add(cancelButton);
        
        mainPanel.add(leftPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.EAST);
        
        saleDialog.add(mainPanel);
        saleDialog.setVisible(true);
    }

    private void showAddProductDialog(DefaultListModel<Artikel> listModel, JLabel nettoLabel, JLabel mwstLabel, JLabel totalLabel) {
        List<Produkt> products = produktService.getAllProducts();
        if (products.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Keine Produkte verfügbar!");
            return;
        }
        
        JDialog productDialog = new JDialog(this, "Produkt hinzufügen", true);
        productDialog.setSize(500, 400);
        productDialog.setLocationRelativeTo(this);
        
        JPanel panel = new JPanel(new BorderLayout());
        
        // Product list
        DefaultListModel<Produkt> productListModel = new DefaultListModel<>();
        for (Produkt product : products) {
            productListModel.addElement(product);
        }
        
        JList<Produkt> productList = new JList<>(productListModel);
        productList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(productList);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Verfügbare Produkte"));
        
        // Quantity input
        JPanel quantityPanel = new JPanel(new FlowLayout());
        quantityPanel.add(new JLabel("Menge:"));
        JTextField quantityField = new JTextField("1", 10);
        quantityPanel.add(quantityField);
        
        JButton addButton = new JButton("Hinzufügen");
        JButton cancelButton = new JButton("Abbrechen");
        
        addButton.addActionListener(e -> {
            Produkt selectedProduct = productList.getSelectedValue();
            if (selectedProduct == null) {
                JOptionPane.showMessageDialog(productDialog, "Bitte wählen Sie ein Produkt aus.");
                return;
            }
            
            try {
                double quantity = Double.parseDouble(quantityField.getText());
                if (quantity > 0) {
                    Artikel artikel = new Artikel(selectedProduct, quantity);
                    currentBon.addArtikel(artikel);
                    listModel.addElement(artikel);
                    updateTotals(nettoLabel, mwstLabel, totalLabel);
                    productDialog.dispose();
                } else {
                    JOptionPane.showMessageDialog(productDialog, "Bitte geben Sie eine positive Menge ein.");
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(productDialog, "Bitte geben Sie eine gültige Zahl ein.");
            }
        });
        
        cancelButton.addActionListener(e -> productDialog.dispose());
        
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(addButton);
        buttonPanel.add(cancelButton);
        
        panel.add(scrollPane, BorderLayout.CENTER);
        JPanel southPanel = new JPanel(new BorderLayout());
        southPanel.add(quantityPanel, BorderLayout.CENTER);
        southPanel.add(buttonPanel, BorderLayout.SOUTH);
        panel.add(southPanel, BorderLayout.SOUTH);
        
        productDialog.add(panel);
        productDialog.setVisible(true);
    }

    private void updateTotals(JLabel nettoLabel, JLabel mwstLabel, JLabel totalLabel) {
        double netto = currentBon.getNettoGesamtbetrag();
        double mwst = currentBon.getGesamtMwst();
        double brutto = currentBon.getBruttoGesamtbetrag();
        
        nettoLabel.setText(String.format("Netto: %.2f EUR", netto));
        mwstLabel.setText(String.format("MwSt: %.2f EUR", mwst));
        totalLabel.setText(String.format("GESAMT: %.2f EUR", brutto));
    }

    private void showReceiptDialog(Bon bon) {
        JDialog receiptDialog = new JDialog(this, "Bon", true);
        receiptDialog.setSize(500, 600);
        receiptDialog.setLocationRelativeTo(this);
        
        JTextArea receiptText = new JTextArea(bon.generateReceiptText());
        receiptText.setEditable(false);
        receiptText.setFont(new Font("Monospaced", Font.PLAIN, 12));
        
        JScrollPane scrollPane = new JScrollPane(receiptText);
        
        JButton closeButton = new JButton("Schließen");
        closeButton.addActionListener(e -> receiptDialog.dispose());
        
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(closeButton);
        
        receiptDialog.add(scrollPane, BorderLayout.CENTER);
        receiptDialog.add(buttonPanel, BorderLayout.SOUTH);
        receiptDialog.setVisible(true);
    }

    private void showProductManagement() {
        JDialog productDialog = new JDialog(this, "Produktverwaltung", true);
        productDialog.setSize(700, 500);
        productDialog.setLocationRelativeTo(this);

        JPanel mainPanel = new JPanel(new BorderLayout());

        // Product list
        DefaultListModel<String> productListModel = new DefaultListModel<>();
        refreshProductList(productListModel);

        JList<String> productList = new JList<>(productListModel);
        productList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        productList.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane scrollPane = new JScrollPane(productList);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Alle Produkte"));

        mainPanel.add(scrollPane, BorderLayout.CENTER);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton addProductButton = new JButton("Neues Produkt hinzufügen");
        JButton adjustVatButton = new JButton("MwSt anpassen");
        JButton closeButton = new JButton("Schließen");

        addProductButton.addActionListener(e -> {
            showAddNewProductDialog(productListModel);
        });

        adjustVatButton.addActionListener(e -> {
            showAdjustVatDialog(productListModel);
        });

        closeButton.addActionListener(e -> productDialog.dispose());

        buttonPanel.add(addProductButton);
        buttonPanel.add(adjustVatButton);
        buttonPanel.add(closeButton);

        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        productDialog.add(mainPanel);
        productDialog.setVisible(true);
    }

    private void refreshProductList(DefaultListModel<String> productListModel) {
        productListModel.clear();
        List<Produkt> products = produktService.getAllProducts();
        for (Produkt p : products) {
            String unit = p.isWeightBased() ? "pro kg" : "pro Stück";
            String barcode = p.getBarcode() != null && !p.getBarcode().isEmpty()
                    ? p.getBarcode() : "---";
            productListModel.addElement(String.format("ID: %d | Barcode: %s | %s - %.2f EUR %s",
                    p.getPid(), barcode, p.getBezeichnung(), p.getPreis(), unit));
        }
    }

    private void showAddNewProductDialog(DefaultListModel<String> productListModel) {
        JDialog addDialog = new JDialog(this, "Neues Produkt hinzufügen", true);
        addDialog.setSize(400, 350);
        addDialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;

        // Bezeichnung
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Bezeichnung:"), gbc);
        JTextField bezeichnungField = new JTextField(20);
        gbc.gridx = 1;
        panel.add(bezeichnungField, gbc);

        // Preis
        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Preis (EUR):"), gbc);
        JTextField preisField = new JTextField(20);
        gbc.gridx = 1;
        panel.add(preisField, gbc);

        // Barcode
        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(new JLabel("Barcode (optional):"), gbc);
        JTextField barcodeField = new JTextField(20);
        gbc.gridx = 1;
        panel.add(barcodeField, gbc);

        // Gewichtsbasiert
        gbc.gridx = 0; gbc.gridy = 3;
        panel.add(new JLabel("Gewichtsbasiert:"), gbc);
        JCheckBox weightBasedCheckBox = new JCheckBox();
        gbc.gridx = 1;
        panel.add(weightBasedCheckBox, gbc);

        // Buttons
        JButton saveButton = new JButton("Speichern");
        JButton cancelButton = new JButton("Abbrechen");

        saveButton.addActionListener(e -> {
            String bezeichnung = bezeichnungField.getText().trim();
            String preisStr = preisField.getText().trim();
            String barcode = barcodeField.getText().trim();
            boolean isWeightBased = weightBasedCheckBox.isSelected();

            if (bezeichnung.isEmpty()) {
                JOptionPane.showMessageDialog(addDialog, 
                        "Bezeichnung ist erforderlich!", 
                        "Eingabefehler", JOptionPane.ERROR_MESSAGE);
                return;
            }

            double preis;
            try {
                preis = Double.parseDouble(preisStr);
                if (preis <= 0) {
                    throw new NumberFormatException("Preis muss größer als 0 sein");
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(addDialog, 
                        "Bitte geben Sie einen gültigen Preis größer als 0 ein!", 
                        "Eingabefehler", JOptionPane.ERROR_MESSAGE);
                return;
            }

            Produkt product = new Produkt();
            product.setBezeichnung(bezeichnung);
            product.setPreis(preis);
            product.setBarcode(barcode);
            product.setWeightBased(isWeightBased);
            product.setKid(DEFAULT_CATEGORY_ID);
            product.setMwst(DEFAULT_VAT_RATE);

            if (produktService.saveProduct(product)) {
                JOptionPane.showMessageDialog(addDialog, 
                        "Produkt erfolgreich hinzugefügt!", 
                        "Erfolg", JOptionPane.INFORMATION_MESSAGE);
                // Refresh product list
                refreshProductList(productListModel);
                addDialog.dispose();
            } else {
                JOptionPane.showMessageDialog(addDialog, 
                        "Fehler beim Hinzufügen des Produkts!", 
                        "Fehler", JOptionPane.ERROR_MESSAGE);
            }
        });

        cancelButton.addActionListener(e -> addDialog.dispose());

        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);

        gbc.gridx = 0; gbc.gridy = 4;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        panel.add(buttonPanel, gbc);

        addDialog.add(panel);
        addDialog.setVisible(true);
    }

    private void showAdjustVatDialog(DefaultListModel<String> productListModel) {
        List<Produkt> products = produktService.getAllProducts();
        if (products.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Keine Produkte verfügbar!");
            return;
        }

        JDialog vatDialog = new JDialog(this, "MwSt anpassen", true);
        vatDialog.setSize(500, 400);
        vatDialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new BorderLayout());

        // Product list
        DefaultListModel<Produkt> productDialogListModel = new DefaultListModel<>();
        for (Produkt product : products) {
            productDialogListModel.addElement(product);
        }

        JList<Produkt> productJList = new JList<>(productDialogListModel);
        productJList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(productJList);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Produkt auswählen"));

        // VAT input panel
        JPanel vatInputPanel = new JPanel(new FlowLayout());
        vatInputPanel.add(new JLabel("Neue MwSt (%):"));
        String defaultVatStr = String.format("%.0f", DEFAULT_VAT_RATE * 100);
        JTextField vatField = new JTextField(defaultVatStr, 10);
        vatInputPanel.add(vatField);

        // Buttons
        JButton saveButton = new JButton("Speichern");
        JButton cancelButton = new JButton("Abbrechen");

        saveButton.addActionListener(e -> {
            Produkt selectedProduct = productJList.getSelectedValue();
            if (selectedProduct == null) {
                JOptionPane.showMessageDialog(vatDialog, "Bitte wählen Sie ein Produkt aus.");
                return;
            }

            String vatStr = vatField.getText().trim();
            double vatPercent;
            try {
                vatPercent = Double.parseDouble(vatStr);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(vatDialog,
                        "Bitte geben Sie eine gültige Zahl ein!",
                        "Eingabefehler", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (vatPercent < MIN_VAT_RATE || vatPercent > MAX_VAT_RATE) {
                JOptionPane.showMessageDialog(vatDialog,
                        String.format("MwSt muss zwischen %.0f und %.0f liegen!", MIN_VAT_RATE, MAX_VAT_RATE),
                        "Eingabefehler", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Convert percentage to decimal (e.g., 19% -> 0.19)
            double vatDecimal = vatPercent / 100.0;
            selectedProduct.setMwst(vatDecimal);

            if (produktService.saveProduct(selectedProduct)) {
                JOptionPane.showMessageDialog(vatDialog,
                        String.format("MwSt für '%s' auf %.1f%% geändert!",
                                selectedProduct.getBezeichnung(), vatPercent),
                        "Erfolg", JOptionPane.INFORMATION_MESSAGE);
                refreshProductList(productListModel);
                vatDialog.dispose();
            } else {
                JOptionPane.showMessageDialog(vatDialog,
                        "Fehler beim Speichern der MwSt!",
                        "Fehler", JOptionPane.ERROR_MESSAGE);
            }
        });

        cancelButton.addActionListener(e -> vatDialog.dispose());

        // Update VAT field when product is selected
        productJList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                Produkt selectedProduct = productJList.getSelectedValue();
                if (selectedProduct != null) {
                    vatField.setText(String.format("%.1f", selectedProduct.getMwst() * 100));
                }
            }
        });

        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);

        JPanel southPanel = new JPanel(new BorderLayout());
        southPanel.add(vatInputPanel, BorderLayout.CENTER);
        southPanel.add(buttonPanel, BorderLayout.SOUTH);

        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(southPanel, BorderLayout.SOUTH);

        vatDialog.add(panel);
        vatDialog.setVisible(true);
    }

    private void showCancelSaleDialog() {
        if (!currentUser.isFilialleiter()) {
            JOptionPane.showMessageDialog(this, "Nur Filialleiter können Verkäufe stornieren!", 
                                          "Berechtigung fehlt", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Input dialog for Bon-ID
        String bonIdStr = JOptionPane.showInputDialog(this, "Bon-ID zum Stornieren eingeben:", 
                                                      "Verkauf stornieren", JOptionPane.PLAIN_MESSAGE);
        if (bonIdStr == null || bonIdStr.trim().isEmpty()) {
            return; // User cancelled
        }
        
        int bonId;
        try {
            bonId = Integer.parseInt(bonIdStr.trim());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Ungültige Bon-ID!", "Fehler", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Retrieve receipt
        Bon bon = verkaufService.getReceiptById(bonId);
        if (bon == null) {
            JOptionPane.showMessageDialog(this, "Bon nicht gefunden!", "Fehler", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        if (bon.isCancelled()) {
            JOptionPane.showMessageDialog(this, "Bon ist bereits storniert!", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        // Show receipt details dialog
        showReceiptDetailsForCancellation(bon);
    }

    private void showReceiptDetailsForCancellation(Bon bon) {
        JDialog cancelDialog = new JDialog(this, "Verkauf stornieren - Bon #" + bon.getBonId(), true);
        cancelDialog.setLayout(new BorderLayout());
        cancelDialog.setSize(500, 600);
        cancelDialog.setLocationRelativeTo(this);
        
        // Receipt display area
        JTextArea receiptArea = new JTextArea();
        receiptArea.setText(bon.generateReceiptText());
        receiptArea.setEditable(false);
        receiptArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        
        JScrollPane scrollPane = new JScrollPane(receiptArea);
        scrollPane.setPreferredSize(new Dimension(450, 400));
        
        // Reason input
        JLabel reasonLabel = new JLabel("Grund für Stornierung:");
        JTextField reasonField = new JTextField(30);
        
        JPanel reasonPanel = new JPanel(new FlowLayout());
        reasonPanel.add(reasonLabel);
        reasonPanel.add(reasonField);
        
        // Buttons
        JButton confirmButton = new JButton("Stornieren");
        JButton cancelButton = new JButton("Abbrechen");
        
        confirmButton.addActionListener(e -> {
            String reason = reasonField.getText().trim();
            if (reason.isEmpty()) {
                JOptionPane.showMessageDialog(cancelDialog, "Bitte einen Grund für die Stornierung angeben!", 
                                              "Eingabe erforderlich", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            boolean success = verkaufService.cancelSale(bon.getBonId(), currentUser, reason);
            if (success) {
                JOptionPane.showMessageDialog(cancelDialog, "Verkauf erfolgreich storniert!", 
                                              "Erfolg", JOptionPane.INFORMATION_MESSAGE);
                cancelDialog.dispose();
            } else {
                JOptionPane.showMessageDialog(cancelDialog, "Fehler beim Stornieren des Verkaufs!", 
                                              "Fehler", JOptionPane.ERROR_MESSAGE);
            }
        });
        
        cancelButton.addActionListener(e -> cancelDialog.dispose());
        
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(confirmButton);
        buttonPanel.add(cancelButton);
        
        // Layout
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(scrollPane, BorderLayout.CENTER);
        topPanel.add(reasonPanel, BorderLayout.SOUTH);
        
        cancelDialog.add(topPanel, BorderLayout.CENTER);
        cancelDialog.add(buttonPanel, BorderLayout.SOUTH);
        
        cancelDialog.setVisible(true);
    }

    private void showStatistics() {
        JOptionPane.showMessageDialog(this, "Statistiken - Implementierung folgt in erweiterten Versionen");
    }

    private void logout() {
        currentUser = null;
        updateButtonStates();
        JOptionPane.showMessageDialog(this, "Erfolgreich abgemeldet.");
    }

    public static void main(String[] args) {
        // Initialize database
        DatabaseManager.getInstance();
        
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                SwingKassensystemApp app = new SwingKassensystemApp();
                app.setVisible(true);
            }
        });
    }
}