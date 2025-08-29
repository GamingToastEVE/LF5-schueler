package de.obj;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Database utility class for managing SQLite operations.
 */
public class DatabaseManager {
    private static final String DB_URL = "jdbc:sqlite:shop.db";
    private static DatabaseManager instance;
    
    private DatabaseManager() {
        initializeDatabase();
    }
    
    public static DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }
    
    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }
    
    /**
     * Initialize database with required tables and updates.
     */
    private void initializeDatabase() {
        try (Connection conn = getConnection()) {
            // Add PIN and Role columns to Verkäufer table if they don't exist
            addColumnIfNotExists(conn, "Verkäufer", "PIN", "VARCHAR(255)");
            addColumnIfNotExists(conn, "Verkäufer", "Rolle", "VARCHAR(20) DEFAULT 'VERKAUFER'");
            
            // Add barcode and weight flag to Produkt table
            addColumnIfNotExists(conn, "Produkt", "Barcode", "VARCHAR(50)");
            addColumnIfNotExists(conn, "Produkt", "IsWeightBased", "BOOLEAN DEFAULT 0");
            addColumnIfNotExists(conn, "Produkt", "MwSt", "DECIMAL(5,4) DEFAULT 0.19");
            
            // Create sales receipts table
            createTableIfNotExists(conn, "Kassenbons", 
                "BonID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "VerkauferID INTEGER, " +
                "Datum TEXT, " +
                "Gesamtbetrag DECIMAL(10,2), " +
                "IsCancelled BOOLEAN DEFAULT 0, " +
                "FOREIGN KEY (VerkauferID) REFERENCES Verkäufer(VID)");
            
            // Create receipt items table
            createTableIfNotExists(conn, "BonPositionen",
                "PositionID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "BonID INTEGER, " +
                "ProduktID INTEGER, " +
                "Menge DECIMAL(10,3), " +
                "Einzelpreis DECIMAL(10,2), " +
                "Gesamtpreis DECIMAL(10,2), " +
                "FOREIGN KEY (BonID) REFERENCES Kassenbons(BonID), " +
                "FOREIGN KEY (ProduktID) REFERENCES Produkt(PID)");
                
            // Create cancellations log table
            createTableIfNotExists(conn, "Stornierungen",
                "StornierungID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "BonID INTEGER, " +
                "StorniertVon INTEGER, " +
                "StorniertAm TEXT, " +
                "Grund TEXT, " +
                "FOREIGN KEY (BonID) REFERENCES Kassenbons(BonID), " +
                "FOREIGN KEY (StorniertVon) REFERENCES Verkäufer(VID)");
                
            // Initialize default users with PINs if not exist
            initializeDefaultUsers(conn);
            
        } catch (SQLException e) {
            System.err.println("Database initialization error: " + e.getMessage());
        }
    }
    
    private void addColumnIfNotExists(Connection conn, String table, String column, String definition) {
        try {
            String checkSql = "PRAGMA table_info(" + table + ")";
            try (PreparedStatement stmt = conn.prepareStatement(checkSql);
                 ResultSet rs = stmt.executeQuery()) {
                boolean columnExists = false;
                while (rs.next()) {
                    if (column.equalsIgnoreCase(rs.getString("name"))) {
                        columnExists = true;
                        break;
                    }
                }
                if (!columnExists) {
                    String alterSql = "ALTER TABLE " + table + " ADD COLUMN " + column + " " + definition;
                    try (Statement alterStmt = conn.createStatement()) {
                        alterStmt.execute(alterSql);
                        System.out.println("Added column " + column + " to table " + table);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error adding column " + column + " to table " + table + ": " + e.getMessage());
        }
    }
    
    private void createTableIfNotExists(Connection conn, String tableName, String definition) {
        try {
            String sql = "CREATE TABLE IF NOT EXISTS " + tableName + " (" + definition + ")";
            try (Statement stmt = conn.createStatement()) {
                stmt.execute(sql);
                System.out.println("Table " + tableName + " created or already exists");
            }
        } catch (SQLException e) {
            System.err.println("Error creating table " + tableName + ": " + e.getMessage());
        }
    }
    
    private void initializeDefaultUsers(Connection conn) {
        try {
            // Check if users already have PINs set
            String checkSql = "SELECT COUNT(*) FROM Verkäufer WHERE PIN IS NOT NULL AND PIN != ''";
            try (PreparedStatement stmt = conn.prepareStatement(checkSql);
                 ResultSet rs = stmt.executeQuery()) {
                if (rs.next() && rs.getInt(1) == 0) {
                    // Set default PINs and roles
                    String updateSql = "UPDATE Verkäufer SET PIN = ?, Rolle = ? WHERE VID = ?";
                    try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                        // Maria Schmidt - Filialleiter
                        updateStmt.setString(1, hashPin("1234"));
                        updateStmt.setString(2, "FILIALLEITER");
                        updateStmt.setInt(3, 1);
                        updateStmt.executeUpdate();
                        
                        // Johannes Müller - Verkäufer
                        updateStmt.setString(1, hashPin("5678"));
                        updateStmt.setString(2, "VERKAUFER");
                        updateStmt.setInt(3, 2);
                        updateStmt.executeUpdate();
                        
                        // Emma Fischer - Verkäufer
                        updateStmt.setString(1, hashPin("9999"));
                        updateStmt.setString(2, "VERKAUFER");
                        updateStmt.setInt(3, 3);
                        updateStmt.executeUpdate();
                        
                        System.out.println("Default user PINs initialized");
                        System.out.println("Maria Schmidt (FL): PIN 1234");
                        System.out.println("Johannes Müller (V): PIN 5678");
                        System.out.println("Emma Fischer (V): PIN 9999");
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error initializing default users: " + e.getMessage());
        }
    }
    
    /**
     * Simple PIN hashing using basic encryption for demo purposes.
     */
    private String hashPin(String pin) {
        // In a real system, use BCrypt or similar
        // For demo purposes, we'll use a simple transformation
        return "hash_" + pin + "_salt";
    }
    
    public boolean verifyPin(String pin, String hashedPin) {
        return hashPin(pin).equals(hashedPin);
    }
    
    public String getCurrentTimestamp() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }
}