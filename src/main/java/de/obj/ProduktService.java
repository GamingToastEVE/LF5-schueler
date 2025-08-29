package de.obj;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Service class for product management.
 */
public class ProduktService {
    private final DatabaseManager dbManager;
    
    public ProduktService() {
        this.dbManager = DatabaseManager.getInstance();
    }
    
    /**
     * Get all products.
     */
    public List<Produkt> getAllProducts() {
        List<Produkt> products = new ArrayList<>();
        String sql = "SELECT PID, Bezeichnung, Preis, COALESCE(MwSt, 0.19) as MwSt, " +
                    "COALESCE(KID, 1) as KID, COALESCE(Barcode, '') as Barcode, " +
                    "COALESCE(IsWeightBased, 0) as IsWeightBased FROM Produkt";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                Produkt product = new Produkt();
                product.setPid(rs.getInt("PID"));
                product.setBezeichnung(rs.getString("Bezeichnung"));
                product.setPreis(rs.getDouble("Preis"));
                product.setMwst(rs.getDouble("MwSt"));
                product.setKid(rs.getInt("KID"));
                product.setBarcode(rs.getString("Barcode"));
                product.setWeightBased(rs.getBoolean("IsWeightBased"));
                products.add(product);
            }
        } catch (SQLException e) {
            System.err.println("Error getting products: " + e.getMessage());
        }
        
        return products;
    }
    
    /**
     * Find product by barcode.
     */
    public Produkt findByBarcode(String barcode) {
        String sql = "SELECT PID, Bezeichnung, Preis, COALESCE(MwSt, 0.19) as MwSt, " +
                    "COALESCE(KID, 1) as KID, COALESCE(Barcode, '') as Barcode, " +
                    "COALESCE(IsWeightBased, 0) as IsWeightBased FROM Produkt WHERE Barcode = ?";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, barcode);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Produkt product = new Produkt();
                    product.setPid(rs.getInt("PID"));
                    product.setBezeichnung(rs.getString("Bezeichnung"));
                    product.setPreis(rs.getDouble("Preis"));
                    product.setMwst(rs.getDouble("MwSt"));
                    product.setKid(rs.getInt("KID"));
                    product.setBarcode(rs.getString("Barcode"));
                    product.setWeightBased(rs.getBoolean("IsWeightBased"));
                    return product;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding product by barcode: " + e.getMessage());
        }
        
        return null;
    }
    
    /**
     * Find product by ID.
     */
    public Produkt findById(int pid) {
        String sql = "SELECT PID, Bezeichnung, Preis, COALESCE(MwSt, 0.19) as MwSt, " +
                    "COALESCE(KID, 1) as KID, COALESCE(Barcode, '') as Barcode, " +
                    "COALESCE(IsWeightBased, 0) as IsWeightBased FROM Produkt WHERE PID = ?";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, pid);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Produkt product = new Produkt();
                    product.setPid(rs.getInt("PID"));
                    product.setBezeichnung(rs.getString("Bezeichnung"));
                    product.setPreis(rs.getDouble("Preis"));
                    product.setMwst(rs.getDouble("MwSt"));
                    product.setKid(rs.getInt("KID"));
                    product.setBarcode(rs.getString("Barcode"));
                    product.setWeightBased(rs.getBoolean("IsWeightBased"));
                    return product;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding product by ID: " + e.getMessage());
        }
        
        return null;
    }
    
    /**
     * Add or update product.
     */
    public boolean saveProduct(Produkt product) {
        if (product.getPid() > 0) {
            return updateProduct(product);
        } else {
            return insertProduct(product);
        }
    }
    
    private boolean insertProduct(Produkt product) {
        String sql = "INSERT INTO Produkt (Bezeichnung, Preis, MwSt, KID, Barcode, IsWeightBased) VALUES (?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, product.getBezeichnung());
            stmt.setDouble(2, product.getPreis());
            stmt.setDouble(3, product.getMwst());
            stmt.setInt(4, product.getKid());
            stmt.setString(5, product.getBarcode());
            stmt.setBoolean(6, product.isWeightBased());
            
            return stmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.err.println("Error inserting product: " + e.getMessage());
            return false;
        }
    }
    
    private boolean updateProduct(Produkt product) {
        String sql = "UPDATE Produkt SET Bezeichnung = ?, Preis = ?, MwSt = ?, KID = ?, Barcode = ?, IsWeightBased = ? WHERE PID = ?";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, product.getBezeichnung());
            stmt.setDouble(2, product.getPreis());
            stmt.setDouble(3, product.getMwst());
            stmt.setInt(4, product.getKid());
            stmt.setString(5, product.getBarcode());
            stmt.setBoolean(6, product.isWeightBased());
            stmt.setInt(7, product.getPid());
            
            return stmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.err.println("Error updating product: " + e.getMessage());
            return false;
        }
    }
}