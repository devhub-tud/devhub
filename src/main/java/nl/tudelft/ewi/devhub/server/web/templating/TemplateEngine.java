package nl.tudelft.ewi.devhub.server.web.templating;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import freemarker.cache.FileTemplateLoader;
import freemarker.ext.beans.BeansWrapper;
import freemarker.template.*;
import lombok.SneakyThrows;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

public class TemplateEngine {

	private final Configuration conf;
	private final TranslatorFactory translatorFactory;

	@Inject
	@SneakyThrows
	public TemplateEngine(@Named("directory.templates") final File templatesDirectory, TranslatorFactory translatorFactory) {
		this.translatorFactory = translatorFactory;
		final BeansWrapper wrapper = new DefaultObjectWrapperBuilder(Configuration.DEFAULT_INCOMPATIBLE_IMPROVEMENTS)
		        .build();

		this.conf = new Configuration() {
			{
				setDirectoryForTemplateLoading(templatesDirectory);
				setDefaultEncoding(Charsets.UTF_8.displayName());
				setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
				setTagSyntax(Configuration.SQUARE_BRACKET_TAG_SYNTAX);
				setTemplateLoader(new FileTemplateLoader(templatesDirectory) {
					public Reader getReader(Object templateSource, String encoding) throws IOException {
						return new WrappedReader(super.getReader(templateSource, encoding), "[#escape x as x?html]", "[/#escape]");
					}
				});
				setObjectWrapper(wrapper);
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

			TemplateHashModel staticModels = ((BeansWrapper) this.conf.getObjectWrapper()).getStaticModels();
			TemplateHashModel markdownParserStatics = (TemplateHashModel) staticModels.get("nl.tudelft.ewi.devhub.server.util.MarkDownParser");

			builder.put("MarkDownParser", markdownParserStatics);

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