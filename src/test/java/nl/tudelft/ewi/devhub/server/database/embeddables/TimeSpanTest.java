package nl.tudelft.ewi.devhub.server.database.embeddables;

import org.junit.Test;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

public class TimeSpanTest {
	@Test
	public void courseThatEnded() {
		LocalDateTime thisYear = LocalDateTime.of(2017,1,1,0,0);
		LocalDateTime oneYearFromNow = thisYear.plusYears(1);

		TimeSpan timeSpan = new TimeSpan(fromLocalDateTime(thisYear), fromLocalDateTime(oneYearFromNow));
		assertThat(timeSpan.intervalString()).isEqualTo("2017-2018");
	}

	@Test
	public void courseThatOnlyHasStart() {
		LocalDateTime thisYear = LocalDateTime.of(2017,1,1,0,0);
		TimeSpan timeSpan = new TimeSpan();
		timeSpan.setStart(fromLocalDateTime(thisYear));

		assertThat(timeSpan.intervalString()).isEqualTo("2017");
	}


	private static Date fromLocalDateTime(LocalDateTime localDateTime) {
		return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
	}

}