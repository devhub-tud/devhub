package nl.tudelft.ewi.devhub.server.database.embeddables;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.Duration;
import java.time.temporal.TemporalAmount;
import java.time.temporal.TemporalUnit;
import java.util.Date;
import java.util.List;

/**
 * Created by Jan-Willem on 8/11/2015.
 */
@Data
@Embeddable
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class TimeSpan implements Serializable, Comparable<TimeSpan>, TemporalAmount {

    @NotNull
    @Column(name="start_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date start;

    @NotNull
    @Column(name="end_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date end;

    public boolean withinInterval(Date date) {
        return (!date.before(getStart())) && (!date.after(getEnd()));
    }

    @Override
    public int compareTo(TimeSpan o) {
        return getDuration().compareTo(o.getDuration());
    }

    public Duration getDuration() {
        return Duration.ofMillis(end.getTime() - start.getTime());
    }

    @Override
    public long get(TemporalUnit unit) {
        return getDuration().get(unit);
    }

    @Override
    public List<TemporalUnit> getUnits() {
        return getDuration().getUnits();
    }

    @Override
    public java.time.temporal.Temporal addTo(java.time.temporal.Temporal temporal) {
        return getDuration().addTo(temporal);
    }

    @Override
    public java.time.temporal.Temporal subtractFrom(java.time.temporal.Temporal temporal) {
        return getDuration().subtractFrom(temporal);
    }

}
