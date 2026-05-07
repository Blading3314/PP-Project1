package I18n;

import java.util.Locale;

public class I18nTest {
    public static void main(String[] args) {
        System.out.println("Testing I18n functionality...");
        
        // Test English
        I18nManager i18n = I18nManager.getInstance();
        i18n.setEnglish();
        System.out.println("English - Main Title: " + i18n.getString("main.title"));
        System.out.println("English - Customer Title: " + i18n.getString("customer.title"));
        System.out.println("English - Status Pending: " + i18n.getString("status.pending"));
        
        // Test French
        i18n.setFrench();
        System.out.println("French - Main Title: " + i18n.getString("main.title"));
        System.out.println("French - Customer Title: " + i18n.getString("customer.title"));
        System.out.println("French - Status Pending: " + i18n.getString("status.pending"));
        
        // Test Spanish
        i18n.setSpanish();
        System.out.println("Spanish - Main Title: " + i18n.getString("main.title"));
        System.out.println("Spanish - Customer Title: " + i18n.getString("customer.title"));
        System.out.println("Spanish - Status Pending: " + i18n.getString("status.pending"));
        
        // Test missing key
        System.out.println("Missing key test: " + i18n.getString("nonexistent.key"));
        
        System.out.println("I18n test completed successfully!");
    }
}
