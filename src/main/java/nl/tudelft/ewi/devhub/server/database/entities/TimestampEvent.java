package nl.tudelft.ewi.devhub.server.database.entities;

import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.util.Date;

/**
 * An {@link Event} where the {@link Event#getTimestamp() timestamp} value
 * is equal to the {@link CreationTimestamp} of the entity.
 */
@Data
@MappedSuperclass
public abstract class TimestampEvent extends Event {

	@CreationTimestamp
	@Column(name = "created_date")
	@Temporal(TemporalType.TIMESTAMP)
	private Date timestamp;

}
