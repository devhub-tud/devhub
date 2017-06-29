package nl.tudelft.ewi.devhub.server.database.entities.notifications;

import lombok.Data;
import lombok.EqualsAndHashCode;
import nl.tudelft.ewi.devhub.server.database.entities.RepositoryEntity;
import nl.tudelft.ewi.devhub.server.database.entities.comments.Comment;
import nl.tudelft.ewi.devhub.server.web.templating.Translator;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import java.net.URI;

/**
 * Created by jgmeligmeyling on 28/06/2017.
 */
@Data
@Entity
@EqualsAndHashCode(callSuper = true)
public class CommentNotification extends Notification implements RepositoryNotification, HasWatchable {

    @ManyToOne
    @JoinColumn(name = "comment_id", referencedColumnName = "id")
    private Comment comment;

    @Override
    public URI getURI() {
        return getComment().getURI();
    }

    @Override
    protected String getTitleResourceKey() {
        return "notifications.commentOnIssue.title";
    }

    @Override
    protected Object[] getTitleParameters() {
        return new Object[] {
                getSender().getName(),
                ""
        };
    }

    @Override
    public String getDescription(Translator translator) {
        return getComment().getContent();
    }

    @Override
    protected String getDescriptionResourceKey() {
        throw new UnsupportedOperationException();
    }

    @Override
    public RepositoryEntity getRepositoryEntity() {
        return getComment().getRepository();
    }

    @Override
    public Watchable getWatchable() {
        return getComment();
    }

}
