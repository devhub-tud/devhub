package nl.tudelft.ewi.devhub.server.util;

import com.google.common.collect.Queues;
import nl.tudelft.ewi.devhub.server.database.entities.Commit;

import java.util.Iterator;
import java.util.Queue;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * @author Jan-Willem Gmelig Meyling
 */
public class CommitIterator implements Iterator<Commit> {

	private final Queue<Commit> commitQueue;
	private final Predicate<Commit> predicate;

	public CommitIterator(Commit start, Predicate<Commit> predicate) {
		this.commitQueue = Queues.newArrayDeque();
		if (predicate.test(start))
			this.commitQueue.add(start);
		this.predicate = predicate;
	}

	@Override
	public synchronized boolean hasNext() {
		return !commitQueue.isEmpty();
	}

	@Override
	public synchronized Commit next() {
		Commit commit = commitQueue.remove();
		commit.getParents().stream()
			.filter(predicate::test)
			.forEach(commitQueue::add);
		return commit;
	}

	public static Stream<Commit> stream(Commit start, Predicate<Commit> predicate) {
		return StreamSupport.stream(
			Spliterators.spliteratorUnknownSize(new CommitIterator(start, predicate), Spliterator.ORDERED),
			false);
	}

}
