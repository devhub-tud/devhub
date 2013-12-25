package nl.devhub.client.jobs;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import nl.devhub.client.jobs.JobManager.JobResult;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

public class JobTracker {

	private final Cache<UUID, JobResult> results;
	
	public JobTracker() {
		this.results = CacheBuilder.newBuilder().expireAfterWrite(1, TimeUnit.HOURS).build();
	}
	
	public void submitResult(UUID jobId, JobResult result) {
		results.put(jobId, result);
	}
	
	public JobResult getResult(UUID jobId) {
		return results.getIfPresent(jobId);
	}
	
	public boolean hasTerminated(UUID jobId) {
		JobResult result = getResult(jobId);
		return result.getExitCode().get() != null;
	}
	
}
