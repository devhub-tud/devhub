package nl.tudelft.ewi.devhub.server.web.templating;
import java.util.List;
import java.util.Locale;

public class TranslatorFactory {

	private final String bundleName;

	public TranslatorFactory(String bundleName) {
		this.bundleName = bundleName;
	}
	
	public Translator create(List<Locale> locales) {
		return new Translator(bundleName, locales);
	}
	
}