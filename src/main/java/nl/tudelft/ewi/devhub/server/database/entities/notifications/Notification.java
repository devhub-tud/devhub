package nl.tudelft.ewi.devhub.server.database.entities.notifications;

import lombok.Data;
import lombok.EqualsAndHashCode;
import nl.tudelft.ewi.devhub.server.database.entities.User;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.validator.constraints.NotEmpty;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.net.URI;
import java.util.Date;

/**
 * Created by Arjan on 7-6-2017.
 */
@Data
@Entity
@Table(name = "notification")
@EqualsAndHashCode(of = {"id"})
public class Notification {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "event")
    @NotNull
    //Change this to Enum?
    private String event;

    @Column(name = "message")
    @NotEmpty
    private String message;

    @Column(name = "link")
    // Change this to URI later
    private String link;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User sender;

    @CreationTimestamp
    @Column(name = "date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date date;

}
