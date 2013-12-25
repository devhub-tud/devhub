package nl.devhub.client.settings;

import java.io.File;
import java.util.Properties;
import java.util.Set;

import com.google.common.collect.Sets;

public class Model<T> {
	
	public static interface Listener<T> {
		void onChange(T newValue);
	}

	private final Set<Listener<T>> listeners;
	private final Class<T> type;
	private final String key;
	
	private T value;
	
	public Model(T defaultValue, String key, Class<T> type) {
		this.key = key;
		this.listeners = Sets.newHashSet();
		this.type = type;
		this.value = defaultValue;
	}
	
	public T getValue() {
		return value;
	}
	
	public void setValue(T value) {
		if ((this.value != null && !this.value.equals(value)) || (this.value == null && value != null)) {
			this.value = value;
			notifyListeners(value);
		}
	}
	
	public void notifyOnChange(Listener<T> listener) {
		listeners.add(listener);
	}

	private void notifyListeners(T newValue) {
		for (Listener<T> listener : listeners) {
			listener.onChange(newValue);
		}
	}

	void update(Properties properties) {
		String newValue = properties.getProperty(key);
		setValue(parseValue(newValue));
	}

	private T parseValue(String newValue) {
		if (type.equals(int.class) || type.equals(Integer.class)) {
			return type.cast(Integer.parseInt(newValue));
		}
		else if (type.equals(String.class)) {
			return type.cast(newValue);
		}
		else if (type.equals(File.class)) {
			return type.cast(new File(newValue));
		}
		throw new IllegalArgumentException("The type: " + type + " is not (yet) supported");
	}
	
}
