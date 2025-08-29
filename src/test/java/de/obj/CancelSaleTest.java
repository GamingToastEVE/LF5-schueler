package de.obj;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test class for cancel sale functionality.
 */
public class CancelSaleTest {
    private VerkaufService verkaufService;
    private UserService userService;
    private ProduktService produktService;
    private User filialleiter;
    private User verkaufer;

    @BeforeEach
    public void setUp() {
        // Initialize database
        DatabaseManager.getInstance();
        
        verkaufService = new VerkaufService();
        userService = new UserService();
        produktService = new ProduktService();
        
        // Get test users
        filialleiter = userService.authenticate("1234"); // Assuming this is Filialleiter PIN
        verkaufer = userService.authenticate("9999"); // Assuming this is regular Verkäufer PIN
    }

    @Test
    public void testCancelSalePermissions() {
        // Create a test sale
        Bon testBon = new Bon(filialleiter);
        
        // Add a test product if available
        Produkt testProdukt = produktService.findByBarcode("123456789");
        if (testProdukt != null) {
            testBon.addArtikel(new Artikel(testProdukt, 1.0));
            
            int bonId = verkaufService.saveSale(testBon);
            assertTrue(bonId > 0, "Sale should be saved successfully");
            
            // Test that only Filialleiter can cancel sales
            if (verkaufer != null) {
                boolean result = verkaufService.cancelSale(bonId, verkaufer, "Test cancellation");
                assertFalse(result, "Regular Verkäufer should not be able to cancel sales");
            }
            
            // Test that Filialleiter can cancel sales
            if (filialleiter != null) {
                boolean result = verkaufService.cancelSale(bonId, filialleiter, "Test cancellation");
                assertTrue(result, "Filialleiter should be able to cancel sales");
                
                // Verify receipt is marked as cancelled
                Bon retrieved = verkaufService.getReceiptById(bonId);
                assertNotNull(retrieved, "Receipt should be retrievable");
                assertTrue(retrieved.isCancelled(), "Receipt should be marked as cancelled");
            }
        }
    }

    @Test
    public void testGetReceiptById() {
        // Create a test sale
        if (filialleiter != null) {
            Bon testBon = new Bon(filialleiter);
            
            // Add a test product if available
            Produkt testProdukt = produktService.findByBarcode("123456789");
            if (testProdukt != null) {
                testBon.addArtikel(new Artikel(testProdukt, 1.0));
                
                int bonId = verkaufService.saveSale(testBon);
                assertTrue(bonId > 0, "Sale should be saved successfully");
                
                // Test receipt retrieval
                Bon retrieved = verkaufService.getReceiptById(bonId);
                assertNotNull(retrieved, "Receipt should be retrievable");
                assertFalse(retrieved.isCancelled(), "New receipt should not be cancelled");
                assertTrue(retrieved.getPositionen().size() > 0, "Receipt should have positions");
            }
        }
    }
}