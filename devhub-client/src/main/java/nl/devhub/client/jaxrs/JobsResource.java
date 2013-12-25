package nl.devhub.client.jaxrs;

import java.util.List;
import java.util.UUID;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;

import nl.devhub.client.jobs.JobManager;
import nl.devhub.client.jobs.JobManager.MavenJob;
import nl.devhub.client.jobs.JobTracker;

@Path("jobs")
public class JobsResource {
	
	private final JobManager manager;
	private final JobTracker tracker;

	@Inject
	public JobsResource(JobManager manager, JobTracker tracker) {
		this.manager = manager;
		this.tracker = tracker;
	}
	
	@GET
	public List<JobResultModel> getJobs() {
		return null;
	}
	
	@POST
	public UUID submitJob(MavenJobModel job) {
		return manager.schedule(new MavenJob(job.getRepositoryUrl(), job.getBranch(), job.getCommit()));
	}
	
}
