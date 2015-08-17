package nl.tudelft.ewi.devhub.server.database.entities.comments;

import lombok.Data;
import lombok.EqualsAndHashCode;
import nl.tudelft.ewi.devhub.server.database.entities.TimestampEvent;
import nl.tudelft.ewi.devhub.server.database.entities.User;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Type;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;
import java.util.Date;

/**
 * @author Jan-Willem Gmelig Meyling
 */
@Data
@Entity
@Inheritance(strategy= InheritanceType.TABLE_PER_CLASS)
@EqualsAndHashCode(of={"commentId"}, callSuper = false)
public abstract class Comment extends TimestampEvent {

    @Id
    @Column(name = "id")
    @GeneratedValue(generator="increment")
    @GenericGenerator(name="increment", strategy = "increment")
    private long commentId;

    @Column(name = "content")
    @Type(type = "org.hibernate.type.TextType")
    private String content;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

}
