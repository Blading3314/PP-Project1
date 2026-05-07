package I18n;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.prefs.Preferences;

public class I18nManager {
    private static final String BUNDLE_NAME = "i18n.messages";
    private static final String PREF_LOCALE = "preferred_locale";
    
    private static I18nManager instance;
    private ResourceBundle resourceBundle;
    private Locale currentLocale;
    private Preferences preferences;
    
    private I18nManager() {
        preferences = Preferences.userNodeForPackage(I18nManager.class);
        loadSavedLocale();
    }
    
    public static synchronized I18nManager getInstance() {
        if (instance == null) {
            instance = new I18nManager();
        }
        return instance;
    }
    
    private void loadSavedLocale() {
        String savedLocale = preferences.get(PREF_LOCALE, null);
        if (savedLocale != null) {
            try {
                String[] parts = savedLocale.split("_");
                if (parts.length == 2) {
                    setLocale(new Locale(parts[0], parts[1]));
                } else if (parts.length == 1) {
                    setLocale(new Locale(parts[0]));
                }
                return;
            } catch (Exception e) {
                // If loading fails, fall back to default, which is english
            }
        }
        // Default to system locale or English
        Locale systemLocale = Locale.getDefault();
        setLocale(systemLocale);
    }
    
    public void setLocale(Locale locale) {
        this.currentLocale = locale;
        try {
            this.resourceBundle = ResourceBundle.getBundle(BUNDLE_NAME, locale);
            // Save the locale preference
            preferences.put(PREF_LOCALE, locale.toString());
        } catch (MissingResourceException e) {
            // Fallback to default locale if requested locale is not supported
            this.resourceBundle = ResourceBundle.getBundle(BUNDLE_NAME, Locale.ENGLISH);
            this.currentLocale = Locale.ENGLISH;
        }
    }
    
    public String getString(String key) {
        try {
            return resourceBundle.getString(key);
        } catch (MissingResourceException e) {
            return "!" + key + "!"; // Return key with ! markers to indicate missing translation
        }
    }
    
    public String getString(String key, Object... args) {
        try {
            String pattern = resourceBundle.getString(key);
            return java.text.MessageFormat.format(pattern, args);
        } catch (MissingResourceException e) {
            return "!" + key + "!"; // Return key with ! markers to indicate missing translation
        }
    }
    
    public Locale getCurrentLocale() {
        return currentLocale;
    }
    
    public ResourceBundle getResourceBundle() {
        return resourceBundle;
    }
    
    // Convenience methods for common locales
    public void setEnglish() {
        setLocale(Locale.ENGLISH);
    }
    
    public void setFrench() {
        setLocale(Locale.FRENCH);
    }
    
    public void setSpanish() {
        setLocale(new Locale("es"));
    }
    
    // Get available locales
    public Locale[] getAvailableLocales() {
        return new Locale[] {
            Locale.ENGLISH,
            Locale.FRENCH,
            new Locale("es")
        };
    }
    
    // Get display name for locale
    public String getLocaleDisplayName(Locale locale) {
        return locale.getDisplayName(locale);
    }
}
