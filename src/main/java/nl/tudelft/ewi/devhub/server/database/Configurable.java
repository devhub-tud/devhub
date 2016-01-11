package nl.tudelft.ewi.devhub.server.database;

import java.util.Map;

/**
 * Created by Jan-Willem on 8/11/2015.
 */
public interface Configurable {

    Map<String, String> getProperties();

    default int getIntegerProperty(String key, Object other) {
		Map<String, String> properties = getProperties();
		if(properties != null) {
			return Integer.parseInt(getProperties().getOrDefault(key, other.toString()));
		}
		return Integer.parseInt(other.toString());
    }

    default boolean getBooleanProperty(String key, Object other) {
		Map<String, String> properties = getProperties();
		if(properties != null) {
			return Boolean.parseBoolean(getProperties().getOrDefault(key, other.toString()));
		}
		return Boolean.parseBoolean(other.toString());
    }

    default String getStringProperty(String key, Object other) {
		Map<String, String> properties = getProperties();
		if(properties != null) {
        	return getProperties().getOrDefault(key, other.toString());
		}
		return other.toString();
    }

    default String[] getCommaSeparatedValues(String key, String... other) {
		Map<String, String> properties = getProperties();
		if(properties != null) {
        	String value = getProperties().get(key);
			if(value != null) {
				return value.split(",");
			}
		}
		return other;
    }

}
