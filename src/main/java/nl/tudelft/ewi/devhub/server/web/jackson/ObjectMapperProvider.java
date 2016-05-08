package nl.tudelft.ewi.devhub.server.web.jackson;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.fasterxml.jackson.datatype.hibernate4.Hibernate4Module;

import javax.inject.Inject;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;

/**
 * Custom {@link ContextResolver} for a {@link ObjectMapper} that registers the
 * {@link Hibernate4Module}.
 */
@Provider
public class ObjectMapperProvider implements ContextResolver<ObjectMapper> {

	private final ObjectMapper mapper;

	@Inject
	ObjectMapperProvider(ObjectMapper mapper) {
		this.mapper = mapper;
		mapper.registerModule(new Hibernate4Module());
		mapper.registerModule(new GuavaModule());
	}

	@Override
	public ObjectMapper getContext(Class<?> type) {
		return mapper;
	}

}