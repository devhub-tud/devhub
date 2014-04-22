package nl.tudelft.ewi.devhub.server.web.templating;

import java.io.IOException;
import java.io.Reader;

import lombok.extern.slf4j.Slf4j;

/**
 * This class is responsible for wrapping all FreeMarker template files with an HTML escaping
 * directive for safety.
 */
@Slf4j
class WrappedReader extends Reader {

	private final Reader originalReader;

	private final char[] prologue;
	private final char[] epilogue;

	private int pos = 0;
	private int firstEpilogueChar = -1;
	private boolean closed = false;

	/**
	 * Constructs a new {@link WrappedReader} object.
	 * 
	 * @param originalReader
	 *            The original {@link Reader} to wrap.
	 * @param prologue
	 *            The prefix wrapping directive.
	 * @param epilogue
	 *            The postfix wrapping directive.
	 */
	public WrappedReader(Reader originalReader, String prologue, String epilogue) {
		this.originalReader = originalReader;
		this.prologue = prologue.toCharArray();
		this.epilogue = epilogue.toCharArray();
	}

	@Override
	public int read(char[] cbuf, int off, int len) throws IOException {
		if (closed) {
			throw new IOException("Reader has been closed already");
		}

		int oldPos = pos;
		log.trace("Reading {} characters from position {}", len, pos);

		if (pos < this.prologue.length) {
			int toCopy = Math.min(this.prologue.length - pos, len);

			log.trace("Copying {} characters from prologue", toCopy);
			System.arraycopy(this.prologue, pos, cbuf, off, toCopy);
			pos += toCopy;
			if (toCopy == len) {
				log.trace("Copied from prologue only");
				return len;
			}
		}

		if (firstEpilogueChar == -1) {
			int copiedSoFar = pos - oldPos;
			int read = originalReader.read(cbuf, off + copiedSoFar, len - copiedSoFar);
			log.trace("Got {} characters from delegate", read);
			if (read != -1) {
				pos += read;
				if (pos - oldPos == len) {
					log.trace("We do not reach epilogue");
					return len;
				}
			}
			firstEpilogueChar = pos;
		}

		int copiedSoFar = pos - oldPos;
		int epiloguePos = pos - firstEpilogueChar;
		int toCopy = Math.min(this.epilogue.length - epiloguePos, len - copiedSoFar);

		if (toCopy <= 0 && copiedSoFar == 0) {
			return -1;
		}

		log.trace("Copying {} characters from epilogue", toCopy);
		System.arraycopy(this.epilogue, epiloguePos, cbuf, off + copiedSoFar, toCopy);

		pos += toCopy;
		log.trace("Copied {} characters, now at position {}", pos - oldPos, pos);
		return pos - oldPos;
	}

	@Override
	public void close() throws IOException {
		originalReader.close();
		closed = true;
	}
}
