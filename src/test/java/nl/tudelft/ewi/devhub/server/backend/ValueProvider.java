package nl.tudelft.ewi.devhub.server.backend;

import com.google.inject.Provider;

class ValueProvider<T> implements Provider<T> {
	
	private final T value;
	
	public ValueProvider(T value) {
		this.value = value;
	}

	@Override
	public T get() {
		return value;
	}
	
}