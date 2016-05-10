package nl.tudelft.ewi.devhub.server.web.resources;

import lombok.extern.slf4j.Slf4j;
import nl.tudelft.ewi.devhub.server.database.controllers.Assignments;
import nl.tudelft.ewi.devhub.server.database.controllers.CourseEditions;
import nl.tudelft.ewi.devhub.server.database.controllers.Deliveries;
import nl.tudelft.ewi.devhub.server.database.entities.Assignment;
import nl.tudelft.ewi.devhub.server.database.entities.CourseEdition;
import nl.tudelft.ewi.devhub.server.database.entities.Delivery;
import nl.tudelft.ewi.devhub.server.database.entities.User;
import nl.tudelft.ewi.devhub.server.database.entities.rubrics.Characteristic;
import nl.tudelft.ewi.devhub.server.database.entities.rubrics.Mastery;
import nl.tudelft.ewi.devhub.server.database.entities.rubrics.Task;
import nl.tudelft.ewi.devhub.server.web.errors.UnauthorizedException;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.google.inject.persist.Transactional;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Created by jgmeligmeyling on 04/03/15.
 * @author Jan-Willem Gmleig Meyling
 */
@Slf4j
@Path("courses/{courseCode}/{editionCode}/assignments")
@Produces(MediaType.TEXT_HTML + Resource.UTF8_CHARSET)
public class RubricImportResource extends Resource {

	private static final String ESCAPED_CSV_DELIMITER_PATTERN = ";(?=([^\"]*\"[^\"]*\")*[^\"]*$)";

	private static final String NEWLINE_PATTERN = "\\n";

	private static final String NUMBER_PATTERN = "\\d+";

	@Inject
    private CourseEditions courses;

    @Inject
    private Assignments assignmentsDAO;

    @Inject
    private Deliveries deliveriesDAO;

    @Inject
    @Named("current.user")
    private User currentUser;

	@Inject
	private EntityManager entityManager;

	@Context
	private HttpServletRequest request;

	@Context
	private HttpServletResponse response;

	private final static String TEXT_CSV = "text/csv";

	@POST
	@Transactional
	@Consumes(TEXT_CSV)
	@Path("{assignmentId : \\d+}/import")
	public void test(@PathParam("courseCode") String courseCode,
					 @PathParam("editionCode") String editionCode,
					 @PathParam("assignmentId") long assignmentId,
					 String input) {

		CourseEdition course = courses.find(courseCode, editionCode);
		Assignment assignment = assignmentsDAO.find(course, assignmentId);

		if(!(currentUser.isAdmin() || currentUser.isAssisting(course))) {
			throw new UnauthorizedException();
		}

		Map<Long, Delivery> groupMap = deliveriesDAO.getLastDeliveries(assignment).stream()
			.collect(Collectors.toMap(delivery -> delivery.getGroup().getGroupNumber(), Function.identity()));

		Pattern digitPattern = Pattern.compile(NUMBER_PATTERN);

		String[][] values = Stream.of(input.split(NEWLINE_PATTERN))
			.map(line -> line.split(ESCAPED_CSV_DELIMITER_PATTERN, -1))
			.toArray(String[][]::new);

		Task task = null;
		AtomicReference<Characteristic> characteristic = new AtomicReference<>();
		Map<Long, Mastery> masteryMap;
		Delivery[] deliveries =  new Delivery[values[0].length];

		for (int i = 0; i < values.length; i++) {
			log.info("Parsing line {} of {}", i, values.length);
			String[] lineParts = values[i];

			if (lineParts.length < 6) continue;

			if (i == 0) {
				for (int j = 7; j < lineParts.length; j++) {
					Matcher matcher = digitPattern.matcher(lineParts[j]);
					if (matcher.find()) {
						String value = matcher.group();
						deliveries[j] = groupMap.get(Long.parseLong(value.trim()));
						log.info("Bound group {} to {}", value, deliveries[j]);
					}
				}
				continue;
			}

			if (lineParts[0].isEmpty()) {
				log.debug("Skipping empty line");
				continue;
			}

			if (lineParts[2].concat(lineParts[3]).concat(lineParts[4]).concat(lineParts[5]).trim().isEmpty()) {
				if (task != null && task.getCharacteristics().isEmpty()) {
					entityManager.remove(task);
				}

				task = new Task();
				task.setAssignment(assignment);
				task.setDescription(lineParts[0]);
				task.setCharacteristics(Lists.newArrayList());
				entityManager.persist(task);
				log.info("Persisted {}", task);
				entityManager.flush();
				entityManager.refresh(assignment);
			}
			else {
				final boolean isPenalty = lineParts[3].concat(lineParts[4]).concat(lineParts[5]).trim().isEmpty();
				Characteristic c = new Characteristic();
				c.setTask(task);
				c.setDescription(lineParts[0]);
				if (!lineParts[1].trim().isEmpty())
					c.setWeight(Double.parseDouble(lineParts[1]));
				c.setWeightAddsToTotalWeight(!isPenalty);
				characteristic.set(c);
				entityManager.persist(characteristic);
				log.info("Persisted {}", characteristic);
				entityManager.flush();
				entityManager.refresh(task);

				final List<Mastery> masteries;

				if (isPenalty) {
					masteries = Lists.newArrayList();
					masteries.add(Mastery.build(characteristic.get(), "No penalty", 0));
					if (!lineParts[2].trim().isEmpty())
						masteries.add(Mastery.build(characteristic.get(), lineParts[2], -2));
					if (!lineParts[3].trim().isEmpty())
						masteries.add(Mastery.build(characteristic.get(), lineParts[3], -2));
				}
				else {
					masteries = IntStream.range(0, 4)
						.filter(index -> !lineParts[2+index].isEmpty())
						.mapToObj(index -> Mastery.build(characteristic.get(), lineParts[2+index], index))
						.collect(Collectors.toList());
				}

				if (masteries.isEmpty()) {
					log.info("Removing empty characteristic {}", characteristic);
					entityManager.remove(characteristic);
					continue;
				}

				masteries.forEach(mastery -> {
					entityManager.persist(mastery);
					log.info("Persisted {}", mastery);
				});

				entityManager.flush();
				entityManager.refresh(characteristic);

				masteryMap = masteries.stream()
					.collect(Collectors.toMap(mastery ->
						Math.round(mastery.getPoints()), Function.identity()));

				for (int j = 7; j < lineParts.length; j++) {
					Delivery delivery = deliveries[j];
					String linePart = lineParts[j];
					if (delivery == null || linePart.isEmpty()) continue;
					linePart = isPenalty ? linePart.equals("-1") ? "-2" : linePart.equals("-0.5") ? "-1" : linePart : linePart;
					Mastery mastery = masteryMap.get(Long.parseLong(linePart));
					delivery.getRubrics().put(characteristic.get(), mastery);
					log.info("Putting {} for {} in {}", characteristic, mastery, delivery.getGroup());
				}
			}
		}

		Stream.of(deliveries).filter(Objects::nonNull).forEach(delivery -> {
			deliveriesDAO.merge(delivery);
			log.info("Updated {}", delivery);
		});
	}

}
