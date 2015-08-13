package nl.tudelft.ewi.devhub.server.database;

import java.util.Map;

/**
 * Created by Jan-Willem on 8/11/2015.
 */
public interface Configurable {

    Map<String, String> getProperties();

    default int getIntegerProperty(String key, Object other) {
        return Integer.parseInt(getProperties().getOrDefault(key, other.toString()));
    }

    default boolean getBooleanProperty(String key, Object other) {
        return Boolean.parseBoolean(getProperties().getOrDefault(key, other.toString()));
    }

    default String getStringProperty(String key, Object other) {
        return getProperties().getOrDefault(key, other.toString());
    }

    default String[] getCommaSeparatedValues(String key, String... other) {
        String value = getProperties().get(key);
        return (value != null) ? value.split(",") : other;
    }

}
