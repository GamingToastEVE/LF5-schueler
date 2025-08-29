package de.obj;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Service class for user authentication and management.
 */
public class UserService {
    private final DatabaseManager dbManager;
    
    public UserService() {
        this.dbManager = DatabaseManager.getInstance();
    }
    
    /**
     * Authenticate user with PIN.
     */
    public User authenticate(String pin) {
        String sql = "SELECT VID, Vorname, Nachname, PIN, COALESCE(Rolle, 'VERKAUFER') as Rolle FROM Verkäufer WHERE PIN = ?";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, "hash_" + pin + "_salt"); // Use same hashing as in DatabaseManager
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    User user = new User();
                    user.setVid(rs.getInt("VID"));
                    user.setVorname(rs.getString("Vorname"));
                    user.setNachname(rs.getString("Nachname"));
                    user.setPin(rs.getString("PIN"));
                    
                    String rolle = rs.getString("Rolle");
                    if ("FILIALLEITER".equals(rolle)) {
                        user.setRolle(User.Role.FILIALLEITER);
                    } else {
                        user.setRolle(User.Role.VERKAUFER);
                    }
                    
                    return user;
                }
            }
        } catch (SQLException e) {
            System.err.println("Authentication error: " + e.getMessage());
        }
        
        return null;
    }
    
    /**
     * Get user by ID.
     */
    public User getUserById(int vid) {
        String sql = "SELECT VID, Vorname, Nachname, PIN, COALESCE(Rolle, 'VERKAUFER') as Rolle FROM Verkäufer WHERE VID = ?";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, vid);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    User user = new User();
                    user.setVid(rs.getInt("VID"));
                    user.setVorname(rs.getString("Vorname"));
                    user.setNachname(rs.getString("Nachname"));
                    user.setPin(rs.getString("PIN"));
                    
                    String rolle = rs.getString("Rolle");
                    if ("FILIALLEITER".equals(rolle)) {
                        user.setRolle(User.Role.FILIALLEITER);
                    } else {
                        user.setRolle(User.Role.VERKAUFER);
                    }
                    
                    return user;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting user: " + e.getMessage());
        }
        
        return null;
    }
}