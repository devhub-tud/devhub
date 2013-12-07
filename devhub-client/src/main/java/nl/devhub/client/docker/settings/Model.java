package nl.devhub.client.docker.settings;

import java.util.Set;

import com.google.common.collect.Sets;

public class Model<T> {
	
	public static interface Listener<T> {
		void onChange(T newValue);
	}

	private T value;
	private Set<Listener<T>> listeners;
	
	public Model(T defaultValue) {
		this.value = defaultValue;
		this.listeners = Sets.newHashSet();
	}
	
	public T getValue() {
		return value;
	}
	
	public void setValue(T value) {
		if (!this.value.equals(value)) {
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
	
}
