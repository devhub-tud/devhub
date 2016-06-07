package nl.tudelft.ewi.devhub.server.util;

import com.google.common.html.HtmlEscapers;
import com.vdurmont.emoji.EmojiParser;
import freemarker.core.Environment;
import freemarker.template.TemplateDirectiveBody;
import freemarker.template.TemplateDirectiveModel;
import freemarker.template.TemplateException;
import freemarker.template.TemplateModel;
import lombok.NonNull;

import org.parboiled.errors.ParserRuntimeException;
import org.pegdown.PegDownProcessor;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Map;
import java.util.Optional;

/**
 * Created by Douwe Koopmans on 1-6-16.
 */
// TODO: 2-6-16 add preview panel when writing markdown
public final class MarkDownParser implements TemplateDirectiveModel {

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
        String escapedMd = HtmlEscapers.htmlEscaper().escape(md);
        escapedMd = EmojiParser.parseToUnicode(escapedMd);
        try {
            return pegDownProcessor.markdownToHtml(escapedMd);
        }
        catch (ParserRuntimeException ex) {
            return escapedMd;
        }
    }

    @Override
    public void execute(Environment env, Map params, TemplateModel[] loopVars, TemplateDirectiveBody body) throws TemplateException, IOException {
        StringWriter stringWriter = new StringWriter();
        if (body != null) {
            body.render(stringWriter);
        }
        Optional<String> message = Optional.ofNullable(params.get("message")).map(Object::toString);
        env.getOut().write(markdownToHtml(message.orElse(stringWriter.toString())));
    }

}
