package de.obj;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for core cash register functionality.
 */
class KassensystemTest {
    
    private UserService userService;
    private ProduktService produktService;
    private VerkaufService verkaufService;
    
    @BeforeEach
    void setUp() {
        // Initialize database and services
        DatabaseManager.getInstance();
        userService = new UserService();
        produktService = new ProduktService();
        verkaufService = new VerkaufService();
    }
    
    @Test
    void testUserAuthentication() {
        // Test authentication with valid PIN
        User user = userService.authenticate("1234");
        assertNotNull(user, "User should be authenticated with valid PIN");
        assertEquals("Maria", user.getVorname(), "First name should be Maria");
        assertEquals("Schmidt", user.getNachname(), "Last name should be Schmidt");
        assertTrue(user.isFilialleiter(), "User should be Filialleiter");
        
        // Test authentication with invalid PIN
        User invalidUser = userService.authenticate("0000");
        assertNull(invalidUser, "User should not be authenticated with invalid PIN");
    }
    
    @Test
    void testProductRetrieval() {
        // Test getting all products
        var products = produktService.getAllProducts();
        assertFalse(products.isEmpty(), "Products should be available");
        
        // Test finding product by ID
        Produkt product = produktService.findById(1);
        assertNotNull(product, "Product with ID 1 should exist");
        assertEquals("Bio-Alpenbutter", product.getBezeichnung(), "Product name should be Bio-Alpenbutter");
        assertTrue(product.getPreis() > 0, "Product price should be greater than 0");
    }
    
    @Test
    void testBonCalculation() {
        // Create a test user
        User testUser = new User(1, "Test", "User", "pin", User.Role.VERKAUFER);
        
        // Create a bon
        Bon bon = new Bon(testUser);
        
        // Create test product
        Produkt product = new Produkt();
        product.setPid(1);
        product.setBezeichnung("Test Product");
        product.setPreis(10.0); // 10 EUR netto
        product.setMwst(0.19); // 19% VAT
        
        // Add article to bon
        Artikel artikel = new Artikel(product, 2.0); // 2 pieces
        bon.addArtikel(artikel);
        
        // Test calculations
        assertEquals(20.0, bon.getNettoGesamtbetrag(), 0.01, "Netto total should be 20.00");
        assertEquals(3.8, bon.getGesamtMwst(), 0.01, "Total VAT should be 3.80");
        assertEquals(23.8, bon.getBruttoGesamtbetrag(), 0.01, "Brutto total should be 23.80");
    }
    
    @Test
    void testBonGeneration() {
        // Create test user
        User testUser = new User(1, "Test", "User", "pin", User.Role.VERKAUFER);
        
        // Create bon
        Bon bon = new Bon(testUser);
        bon.setBonId(999);
        
        // Add test product
        Produkt product = new Produkt();
        product.setBezeichnung("Test Item");
        product.setPreis(5.0);
        product.setMwst(0.19);
        
        Artikel artikel = new Artikel(product, 1.0);
        bon.addArtikel(artikel);
        
        // Generate receipt text
        String receiptText = bon.generateReceiptText();
        
        assertNotNull(receiptText, "Receipt text should not be null");
        assertTrue(receiptText.contains("GoodFood GmbH"), "Receipt should contain company name");
        assertTrue(receiptText.contains("Test User"), "Receipt should contain seller name");
        assertTrue(receiptText.contains("Test Item"), "Receipt should contain product name");
        assertTrue(receiptText.contains("5.95"), "Receipt should contain brutto total");
    }
    
    @Test
    void testRoleBasedAccess() {
        // Test regular employee
        User employee = new User(2, "John", "Doe", "pin", User.Role.VERKAUFER);
        assertFalse(employee.isFilialleiter(), "Regular employee should not be Filialleiter");
        
        // Test manager
        User manager = new User(1, "Jane", "Manager", "pin", User.Role.FILIALLEITER);
        assertTrue(manager.isFilialleiter(), "Manager should be Filialleiter");
    }
}