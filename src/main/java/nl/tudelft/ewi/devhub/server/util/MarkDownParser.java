package nl.tudelft.ewi.devhub.server.util;

import lombok.NonNull;
import lombok.Synchronized;
import org.parboiled.errors.ParserRuntimeException;
import org.pegdown.PegDownProcessor;

import javax.validation.constraints.NotNull;

/**
 * Created by Douwe Koopmans on 1-6-16.
 */
// TODO: 2-6-16 add preview panel when writing markdown
public final class MarkDownParser {
    private static long timeout = 2000L;

    // initialising the processor takes a little bit, so it is smarter to reuse one initialisation
    // default parsing timeout is 2 seconds
    // TODO: 3-6-16 look into setting the parsing timeout through the properties
    private static PegDownProcessor processor = new PegDownProcessor(timeout);

	/**
     * converts the given markdown string into a valid html representation
     * @param md nonnull raw markdown string
     * @return the html representation of the given markdown, when processing the markdown takes too long, the original
     * raw markdown is returned instead
     * @throws NullPointerException when the given markdown string input is null
     */
    public static @NotNull String markdownToHtml(@NonNull final String md) throws NullPointerException {
        try {
            return getProcessor().markdownToHtml(md);
        } catch (ParserRuntimeException ex) {
            return md;
        }
    }

    // a bit extensive maybe, but this need to be thread-safe
    @Synchronized
    private static PegDownProcessor getProcessor() {
        return processor;
    }

    @Synchronized
    public static void setProcessorTimeout(long newTimeout) {
        timeout = newTimeout;
        processor = new PegDownProcessor(timeout);
    }
}
