package nl.tudelft.ewi.devhub.server.util;

import com.google.common.html.HtmlEscapers;
import lombok.NonNull;
import lombok.Synchronized;
import org.parboiled.errors.ParserRuntimeException;
import org.pegdown.PegDownProcessor;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;

/**
 * Created by Douwe Koopmans on 1-6-16.
 */
// TODO: 2-6-16 add preview panel when writing markdown
public final class MarkDownParser {

    private final PegDownProcessor pegDownProcessor;

    @Inject
    public MarkDownParser(PegDownProcessor pegDownProcessor) {
        this.pegDownProcessor = pegDownProcessor;
    }

    /**
     * converts the given markdown string into a valid html representation
     * @param md nonnull raw markdown string
     * @return the html representation of the given markdown, when processing the markdown takes too long, the original
     * raw markdown is returned instead
     * @throws NullPointerException when the given markdown string input is null
     */
    public @NotNull String markdownToHtml(@NonNull final String md) throws NullPointerException {
        final String escapedMd = HtmlEscapers.htmlEscaper().escape(md);

        try {
            return pegDownProcessor.markdownToHtml(escapedMd);
        }
        catch (ParserRuntimeException ex) {
            return escapedMd;
        }
    }

}
