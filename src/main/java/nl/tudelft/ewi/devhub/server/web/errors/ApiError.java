package nl.tudelft.ewi.devhub.server.web.errors;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class ApiError extends Exception {

	private static final long serialVersionUID = -8806603361456459296L;

	private final String resourceKey;
	
	public ApiError(String resourceKey) {
		super();
		this.resourceKey = resourceKey;
	}
	
	public ApiError(String resourceKey, Throwable t) {
		super(t.getMessage(), t);
		this.resourceKey = resourceKey;
	}

}
