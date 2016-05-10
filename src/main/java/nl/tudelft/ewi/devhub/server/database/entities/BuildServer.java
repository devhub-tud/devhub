package nl.tudelft.ewi.devhub.server.database.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import org.hibernate.validator.constraints.NotEmpty;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Data
@Entity
@Table(name = "build_servers")
@JsonIgnoreProperties(ignoreUnknown = true)
public class BuildServer {
	
	@Id
	@Column(name = "id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;
	
	@NotEmpty
	@Column(name = "name")
	private String name;

	@NotEmpty
	@Column(name = "secret")
	private String secret;

	@NotEmpty
	@Column(name = "host")
	private String host;

}
