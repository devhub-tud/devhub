package nl.tudelft.ewi.devhub.server.util;

import lombok.NonNull;
import lombok.Synchronized;
import org.pegdown.PegDownProcessor;

/**
 * Created by Douwe Koopmans on 1-6-16.
 */
public final class MarkDownParser {

    // initialising the processor takes a little bit, so it is smarter to reuse one initialisation
    private static PegDownProcessor processor = new PegDownProcessor();

    public static String markdownToHtml(@NonNull String md) {
        return getProcessor().markdownToHtml(md);
    }

    // a bit extensive maybe, but this need to be synchronized properly
    @Synchronized
    private static PegDownProcessor getProcessor() {
        return processor;
    }
}
