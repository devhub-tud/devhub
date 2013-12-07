package nl.devhub.server.database;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * This class is able to load the persistence configuration from disk. This configuration is loaded
 * from two locations (on the classpath):
 * <ul>
 * <li><code>/META-INF/persistence.xml</code></li>
 * <li><code>/persistence.properties</code></li>
 * </ul>
 * The <code>persistence.xml</code> file holds all persistence unit settings, and the
 * <code>persistence.properties</code> file is able to override the settings of the chosen
 * persistence unit by assigning new values to the same keys using as in the
 * <code>persistence.xml</code> file.
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PersistenceConfiguration {

	/**
	 * This method loads all properties defined in the <code>persistence.properties</code> file
	 * 
	 * @return A {@link Properties} object containing all the key-value pairs as defined in the
	 *         <code>persistence.properties</code> file.
	 * @throws IOException
	 *             If the <code>persistence.properties</code> file could not be located or parsed.
	 */
	public static Properties load() throws IOException {
		Properties properties = new Properties();
		try (InputStream stream = PersistenceConfiguration.class.getResourceAsStream("/persistence.properties")) {
			checkNotNull(stream, "Persistence properties not found");
			properties.load(stream);
		}
		catch (IOException e) {
			log.error(e.getMessage(), e);
			throw e;
		}
		return properties;
	}

	/**
	 * This method loads the entire configuration (including the overriding settings defined in the
	 * <code>persistence.properties</code>) from the <code>persistence.xml</code> file for the
	 * specified persistence unit.
	 * 
	 * @param persistenceUnit
	 *            The persistence unit to load the configuration of.
	 * @return A {@link Properties} object containing all the defined properties.
	 * @throws IOException
	 *             If one of the files could not be found or parsed.
	 */
	public static Properties load(String persistenceUnit) throws IOException {
		Properties properties = new Properties();

		try (InputStream stream = PersistenceConfiguration.class.getResourceAsStream("/META-INF/persistence.xml")) {
			checkNotNull(stream, "Persistence XML not found");
			Document doc = new SAXBuilder().build(stream);
			Element root = doc.getRootElement();

			for (Element element : root.getChildren()) {
				boolean elementIsPersistenceUnit = "persistence-unit".equals(element.getName());
				if (elementIsPersistenceUnit && persistenceUnit.equals(element.getAttributeValue("name"))) {
					for (Element pElement : element.getChildren()) {
						if ("properties".equals(pElement.getName())) {
							List<Element> settings = pElement.getChildren();
							for (Element setting : settings) {
								String key = setting.getAttributeValue("name");
								String value = setting.getAttributeValue("value");
								properties.setProperty(key, value);
							}
						}
					}
					break;
				}
			}
		}
		catch (IOException e) {
			log.error(e.getMessage(), e);
			throw e;
		}
		catch (JDOMException e) {
			log.error(e.getMessage(), e);
			throw new IOException(e.getMessage(), e);
		}

		Properties override = PersistenceConfiguration.load();
		for (String propertyName : override.stringPropertyNames()) {
			properties.put(propertyName, override.getProperty(propertyName));
		}

		return properties;
	}

}
