package nl.devhub.client.docker;

public interface Log {
	void onNextLine(String line);
	void onClose();
}