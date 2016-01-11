package nl.tudelft.ewi.devhub.server.database.entities;

import lombok.Data;
import lombok.SneakyThrows;
import nl.tudelft.ewi.devhub.server.database.Base;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.io.File;
import java.net.URI;
import java.net.URLEncoder;

/**
 * Created by jgmeligmeyling on 04/03/15.
 */
@Data
@Entity
@Table(name = "assignment_delivery_attachments")
public class DeliveryAttachment implements Base {

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

    @Override
    public URI getURI() {
        return getDelivery().getURI().resolve("attachment/").resolve(encode(getPath()));
    }

    @SneakyThrows
    static String encode(String value) {
        return URLEncoder.encode(value, "UTF-8");
    }

}
