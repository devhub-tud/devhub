package nl.devhub.server.web.templating;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import lombok.SneakyThrows;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;

import freemarker.template.Configuration;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;

public class TemplateEngine {

	private final Configuration conf;
	private final TranslatorFactory translatorFactory;

	@Inject
	@SneakyThrows
	public TemplateEngine(@Named("directory.templates") final File templatesDirectory, TranslatorFactory translatorFactory) {
		this.translatorFactory = translatorFactory;
		this.conf = new Configuration() {
			{
				setDirectoryForTemplateLoading(templatesDirectory);
				setDefaultEncoding(Charsets.UTF_8.displayName());
				setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
				setTagSyntax(Configuration.SQUARE_BRACKET_TAG_SYNTAX);
			}
		};
	}

	public String process(String template, List<Locale> locales) throws IOException {
		return process(template, locales, null);
	}

	public String process(String template, List<Locale> locales, Map<String, ?> parameters) throws IOException {
		Translator translator = translatorFactory.create(locales);
		
		try {
			Builder<String, Object> builder = ImmutableMap.<String, Object> builder();
			builder.put("i18n", translator);
			if (parameters != null) {
				builder.putAll(parameters);
			}
			
			StringWriter out = new StringWriter();
			conf.getTemplate(template).process(builder.build(), out);
			return out.toString();
		}
		catch (TemplateException e) {
			throw new IOException(e);
		}
	}

}