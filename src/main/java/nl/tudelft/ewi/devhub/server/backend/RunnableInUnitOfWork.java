package nl.tudelft.ewi.devhub.server.backend;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.google.inject.Provider;
import com.google.inject.persist.UnitOfWork;

/**
 * A wrapper for a {@link java.lang.Runnable} where the call method is run within a
 * {@link com.google.inject.persist.UnitOfWork};
 */
@Slf4j
@AllArgsConstructor
public abstract class RunnableInUnitOfWork implements Runnable {

	private final Provider<UnitOfWork> workProvider;

	@Override
	public final void run() {
		UnitOfWork work = workProvider.get();
		try {
			log.trace("Starting");
			work.begin();
			runInUnitOfWork();
		}
		catch (Throwable e) {
			log.error(e.getMessage(), e);
		}
		finally {
			work.end();
			log.trace("Finished");
		}
	}

	protected abstract void runInUnitOfWork();
}
