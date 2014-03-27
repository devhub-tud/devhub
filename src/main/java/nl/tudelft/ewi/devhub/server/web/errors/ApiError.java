package nl.tudelft.ewi.devhub.server.web.errors;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class ApiError extends Exception {

	private static final long serialVersionUID = -8806603361456459296L;
	
	private final String resourceKey;
	
}
