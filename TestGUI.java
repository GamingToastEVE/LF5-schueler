
import de.obj.*;
public class TestGUI {
    public static void main(String[] args) {
        // Initialize database
        DatabaseManager.getInstance();
        
        SwingKassensystemApp app = new SwingKassensystemApp();
        app.setVisible(true);
        
        // Keep the application running for testing
        System.out.println("GUI Application started. You can now test the cancel sale functionality.");
    }
}
