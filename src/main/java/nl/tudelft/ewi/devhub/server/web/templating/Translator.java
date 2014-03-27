package nl.tudelft.ewi.devhub.server.web.templating;
import java.text.MessageFormat;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.ResourceBundle.Control;

public class Translator {

	private static final Control BUNDLE_CONTROL = Control.getNoFallbackControl(Control.FORMAT_PROPERTIES);
	
	private static ResourceBundle getBundle(String bundleName, List<Locale> locales) {
		ClassLoader classLoader = Translator.class.getClassLoader();
		for (Locale locale : locales) {
			try {
				ResourceBundle bundle = ResourceBundle.getBundle(bundleName, locale, classLoader, BUNDLE_CONTROL);
				if (bundle != null) {
					return bundle;
				}
			}
			catch (MissingResourceException e) {
				// Swallow exception. We simply don't have a bundle for the requested locale.
			}
		}
		return ResourceBundle.getBundle(bundleName, Locale.ENGLISH, classLoader, BUNDLE_CONTROL);
	}
	
	private final ResourceBundle bundle;

	public Translator(String bundleName, List<Locale> locales) {
		this.bundle = getBundle(bundleName, locales);
	}

	public String translate(String resourceKey, Object... parameters) {
		return MessageFormat.format(bundle.getString(resourceKey), parameters);
	}

}