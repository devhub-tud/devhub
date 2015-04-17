package nl.tudelft.ewi.devhub.server.database.entities;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.validator.constraints.NotEmpty;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;
import java.util.Date;

/**
 * @author Jan-Willem Gmelig Meyling
 */
@Data
@MappedSuperclass
@EqualsAndHashCode(of={"commentId"})
public class Comment implements Comparable<Comment> {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long commentId;

    @Lob
    @NotEmpty
    @Basic(fetch= FetchType.LAZY)
    @Column(name = "content")
    private String content;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @NotNull
    @Column(name="time")
    @Temporal(TemporalType.TIMESTAMP)
    private Date time;

    @Override
    public int compareTo(Comment o) {
        return getTime().compareTo(o.getTime());
    }

}
