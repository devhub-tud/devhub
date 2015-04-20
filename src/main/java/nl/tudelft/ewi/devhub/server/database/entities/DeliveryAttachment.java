package nl.tudelft.ewi.devhub.server.database.entities;

import lombok.Data;

import javax.persistence.*;
import java.io.File;

/**
 * Created by jgmeligmeyling on 04/03/15.
 */
@Data
@Entity
@Table(name = "assignment_delivery_attachments")
public class DeliveryAttachment {

    @Id
    @Column(name = "path")
    private String path;

    @ManyToOne
    @JoinColumn(name = "delivery_id")
    private Delivery delivery;

    /**
     * @return the File name for this attachment
     */
    public String getFileName() {
        return path.substring(path.lastIndexOf(File.separatorChar) + 1);
    }

}
