package nl.tudelft.ewi.devhub.server.database;

enum Strategy {
	DROP_CREATE("drop-create"), UPDATE("update");

	private final String value;

	private Strategy(String value) {
		this.value = value;
	}

	static Strategy getStrategy(String value) {
		for (Strategy strategy : values()) {
			if (strategy.value.equals(value)) {
				return strategy;
			}
		}
		return null;
	}
}