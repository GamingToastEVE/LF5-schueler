import de.obj.*;
import java.time.LocalDateTime;

public class TestCancelSale {
    public static void main(String[] args) {
        // Initialize database
        DatabaseManager dbManager = DatabaseManager.getInstance();
        
        // Create services
        UserService userService = new UserService();
        ProduktService produktService = new ProduktService();
        VerkaufService verkaufService = new VerkaufService();
        
        try {
            // Create a test sale first
            User verkaufer = userService.login("1234"); // Assuming default Filialleiter PIN
            if (verkaufer == null) {
                System.out.println("User not found!");
                return;
            }
            
            System.out.println("User found: " + verkaufer.getName() + " (Filialleiter: " + verkaufer.isFilialleiter() + ")");
            
            // Create test sale
            Bon testBon = new Bon(verkaufer);
            Produkt testProdukt = produktService.findByBarcode("123456789");
            if (testProdukt != null) {
                testBon.addArtikel(new Artikel(testProdukt, 2.0));
                
                int bonId = verkaufService.saveSale(testBon);
                System.out.println("Created test sale with ID: " + bonId);
                
                if (bonId > 0) {
                    // Test cancellation
                    boolean cancelled = verkaufService.cancelSale(bonId, verkaufer, "Test cancellation");
                    System.out.println("Cancellation result: " + cancelled);
                    
                    // Verify receipt is cancelled
                    Bon retrieved = verkaufService.getReceiptById(bonId);
                    if (retrieved != null) {
                        System.out.println("Receipt cancelled: " + retrieved.isCancelled());
                    }
                }
            } else {
                System.out.println("No test product found");
            }
            
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}