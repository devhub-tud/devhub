package nl.tudelft.ewi.devhub.server.web.models.statistics;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

public class AreaChartData {

    @Getter
    @Setter
    private Date commitDate;

    @Getter
    @Setter
    private Integer commitAmount;

}

