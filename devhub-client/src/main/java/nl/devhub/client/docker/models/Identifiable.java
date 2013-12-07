package nl.devhub.client.docker.models;

import lombok.Data;
import lombok.ToString;

import com.fasterxml.jackson.annotation.JsonProperty;

@Data
@ToString(exclude = { "Warnings" })
public class Identifiable {
	@JsonProperty(required = false)
	private String Id;
	
	@JsonProperty(required = false)
	private String[] Warnings;
}