package nl.devhub.client.docker;

public interface Logger {
	void onNextLine(String line);
	void onClose(int exitCode);
}