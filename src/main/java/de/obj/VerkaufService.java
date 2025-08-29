package de.obj;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Service class for managing sales transactions.
 */
public class VerkaufService {
    private final DatabaseManager dbManager;
    private final ProduktService produktService;
    private final UserService userService;
    
    public VerkaufService() {
        this.dbManager = DatabaseManager.getInstance();
        this.produktService = new ProduktService();
        this.userService = new UserService();
    }
    
    /**
     * Save a completed sale to database.
     */
    public int saveSale(Bon bon) {
        String bonSql = "INSERT INTO Kassenbons (VerkauferID, Datum, Gesamtbetrag, IsCancelled) VALUES (?, ?, ?, ?)";
        String positionSql = "INSERT INTO BonPositionen (BonID, ProduktID, Menge, Einzelpreis, Gesamtpreis) VALUES (?, ?, ?, ?, ?)";
        
        try (Connection conn = dbManager.getConnection()) {
            conn.setAutoCommit(false); // Start transaction
            
            // Insert receipt header
            try (PreparedStatement bonStmt = conn.prepareStatement(bonSql, Statement.RETURN_GENERATED_KEYS)) {
                bonStmt.setInt(1, bon.getVerkaufer().getVid());
                bonStmt.setString(2, bon.getDatum().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                bonStmt.setDouble(3, bon.getBruttoGesamtbetrag());
                bonStmt.setBoolean(4, bon.isCancelled());
                
                int affectedRows = bonStmt.executeUpdate();
                if (affectedRows == 0) {
                    throw new SQLException("Creating receipt failed, no rows affected.");
                }
                
                try (ResultSet generatedKeys = bonStmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int bonId = generatedKeys.getInt(1);
                        bon.setBonId(bonId);
                        
                        // Insert receipt positions
                        try (PreparedStatement posStmt = conn.prepareStatement(positionSql)) {
                            for (Artikel artikel : bon.getPositionen()) {
                                posStmt.setInt(1, bonId);
                                posStmt.setInt(2, artikel.getProdukt().getPid());
                                posStmt.setDouble(3, artikel.getMenge());
                                posStmt.setDouble(4, artikel.getProdukt().getBruttoPreis());
                                posStmt.setDouble(5, artikel.getBruttoGesamtpreis());
                                posStmt.executeUpdate();
                            }
                        }
                        
                        conn.commit(); // Commit transaction
                        return bonId;
                    } else {
                        throw new SQLException("Creating receipt failed, no ID obtained.");
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error saving sale: " + e.getMessage());
            return -1;
        }
    }
    
    /**
     * Cancel a sale (only for Filialleiter).
     */
    public boolean cancelSale(int bonId, User cancelledBy, String reason) {
        if (!cancelledBy.isFilialleiter()) {
            System.err.println("Only Filialleiter can cancel sales!");
            return false;
        }
        
        String updateBonSql = "UPDATE Kassenbons SET IsCancelled = 1 WHERE BonID = ?";
        String insertCancelSql = "INSERT INTO Stornierungen (BonID, StorniertVon, StorniertAm, Grund) VALUES (?, ?, ?, ?)";
        
        try (Connection conn = dbManager.getConnection()) {
            conn.setAutoCommit(false);
            
            // Update receipt as cancelled
            try (PreparedStatement updateStmt = conn.prepareStatement(updateBonSql)) {
                updateStmt.setInt(1, bonId);
                int updated = updateStmt.executeUpdate();
                
                if (updated > 0) {
                    // Log cancellation
                    try (PreparedStatement cancelStmt = conn.prepareStatement(insertCancelSql)) {
                        cancelStmt.setInt(1, bonId);
                        cancelStmt.setInt(2, cancelledBy.getVid());
                        cancelStmt.setString(3, dbManager.getCurrentTimestamp());
                        cancelStmt.setString(4, reason);
                        cancelStmt.executeUpdate();
                        
                        conn.commit();
                        return true;
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error cancelling sale: " + e.getMessage());
        }
        
        return false;
    }
    
    /**
     * Get receipt by ID.
     */
    public Bon getReceiptById(int bonId) {
        String bonSql = "SELECT k.BonID, k.VerkauferID, k.Datum, k.Gesamtbetrag, k.IsCancelled " +
                       "FROM Kassenbons k WHERE k.BonID = ?";
        String positionsSql = "SELECT bp.ProduktID, bp.Menge, bp.Einzelpreis, bp.Gesamtpreis " +
                             "FROM BonPositionen bp WHERE bp.BonID = ?";
        
        try (Connection conn = dbManager.getConnection()) {
            // Get receipt header
            try (PreparedStatement bonStmt = conn.prepareStatement(bonSql)) {
                bonStmt.setInt(1, bonId);
                
                try (ResultSet rs = bonStmt.executeQuery()) {
                    if (rs.next()) {
                        Bon bon = new Bon();
                        bon.setBonId(rs.getInt("BonID"));
                        bon.setCancelled(rs.getBoolean("IsCancelled"));
                        
                        // Get seller
                        User verkaufer = userService.getUserById(rs.getInt("VerkauferID"));
                        bon.setVerkaufer(verkaufer);
                        
                        // Parse date
                        String datumStr = rs.getString("Datum");
                        LocalDateTime datum = LocalDateTime.parse(datumStr, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                        bon.setDatum(datum);
                        
                        // Get positions
                        try (PreparedStatement posStmt = conn.prepareStatement(positionsSql)) {
                            posStmt.setInt(1, bonId);
                            
                            try (ResultSet posRs = posStmt.executeQuery()) {
                                List<Artikel> positionen = new ArrayList<>();
                                
                                while (posRs.next()) {
                                    Produkt produkt = produktService.findById(posRs.getInt("ProduktID"));
                                    if (produkt != null) {
                                        Artikel artikel = new Artikel(produkt, posRs.getDouble("Menge"));
                                        positionen.add(artikel);
                                    }
                                }
                                
                                bon.setPositionen(positionen);
                            }
                        }
                        
                        return bon;
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting receipt: " + e.getMessage());
        }
        
        return null;
    }
    
    /**
     * Get sales statistics for a date range.
     */
    public SalesStatistics getSalesStatistics(String fromDate, String toDate) {
        String sql = "SELECT COUNT(*) as totalSales, SUM(Gesamtbetrag) as totalAmount, " +
                    "AVG(Gesamtbetrag) as avgAmount FROM Kassenbons " +
                    "WHERE Datum BETWEEN ? AND ? AND IsCancelled = 0";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, fromDate + " 00:00:00");
            stmt.setString(2, toDate + " 23:59:59");
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    SalesStatistics stats = new SalesStatistics();
                    stats.totalSales = rs.getInt("totalSales");
                    stats.totalAmount = rs.getDouble("totalAmount");
                    stats.avgAmount = rs.getDouble("avgAmount");
                    stats.fromDate = fromDate;
                    stats.toDate = toDate;
                    return stats;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting statistics: " + e.getMessage());
        }
        
        return new SalesStatistics();
    }
    
    /**
     * Get all active (non-cancelled) receipts for cancellation selection.
     */
    public List<Bon> getActiveReceipts() {
        String sql = "SELECT BonID, VerkauferID, Datum, Gesamtbetrag FROM Kassenbons " +
                    "WHERE IsCancelled = 0 ORDER BY Datum DESC";
        
        List<Bon> receipts = new ArrayList<>();
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                Bon bon = new Bon();
                bon.setBonId(rs.getInt("BonID"));
                
                // Get seller
                User verkaufer = userService.getUserById(rs.getInt("VerkauferID"));
                bon.setVerkaufer(verkaufer);
                
                // Parse date
                String datumStr = rs.getString("Datum");
                LocalDateTime datum = LocalDateTime.parse(datumStr, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                bon.setDatum(datum);
                
                // Store total amount for display
                double gesamtbetrag = rs.getDouble("Gesamtbetrag");
                // Add a temporary field to store the total amount - we'll create a simple wrapper
                BonSummary summary = new BonSummary(bon, gesamtbetrag);
                
                receipts.add(summary);
            }
        } catch (SQLException e) {
            System.err.println("Error getting active receipts: " + e.getMessage());
        }
        
        return receipts;
    }

    /**
     * Helper class to include total amount in receipt summary.
     */
    public static class BonSummary extends Bon {
        private final double totalAmount;
        
        public BonSummary(Bon bon, double totalAmount) {
            this.setBonId(bon.getBonId());
            this.setVerkaufer(bon.getVerkaufer());
            this.setDatum(bon.getDatum());
            this.totalAmount = totalAmount;
        }
        
        @Override
        public double getBruttoGesamtbetrag() {
            return totalAmount;
        }
    }

    /**
     * Sales statistics data class.
     */
    public static class SalesStatistics {
        public int totalSales = 0;
        public double totalAmount = 0.0;
        public double avgAmount = 0.0;
        public String fromDate;
        public String toDate;
        
        @Override
        public String toString() {
            return String.format("Verkaufsstatistik (%s - %s):\n" +
                    "Anzahl Verkäufe: %d\n" +
                    "Gesamtumsatz: %.2f EUR\n" +
                    "Durchschnitt pro Verkauf: %.2f EUR",
                    fromDate, toDate, totalSales, totalAmount, avgAmount);
        }
    }
}