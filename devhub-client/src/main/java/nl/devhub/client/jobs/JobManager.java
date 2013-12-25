package nl.devhub.client.jobs;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicReference;

import javax.inject.Inject;
import javax.inject.Singleton;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import nl.devhub.client.docker.DockerJob;
import nl.devhub.client.docker.DockerManager;
import nl.devhub.client.docker.Logger;
import nl.devhub.client.settings.Settings;

import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.CheckoutCommand;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

@Slf4j
@Singleton
public class JobManager {
	
	private final ScheduledThreadPoolExecutor executor;
	private final JobTracker tracker;
	private final DockerManager dockerManager;
	private final Settings settings;
	
	@Inject
	public JobManager(DockerManager dockerManager, JobTracker tracker, Settings settings) {
		this.executor = new ScheduledThreadPoolExecutor(1);
		this.dockerManager = dockerManager;
		this.tracker = tracker;
		this.settings = settings;
	}
	
	public UUID schedule(Job<?> job) {
		log.info("Submitted job: " + job);
		UUID id = UUID.randomUUID();
		executor.submit(new JobRunner(dockerManager, settings, id, job, tracker));
		return id;
	}
	
	@Data
	@Slf4j
	public static class JobRunner implements Runnable {
		
		private final DockerManager dockerManager;
		private final Settings settings;
		private final UUID id;
		private final Job<?> job;
		private final JobTracker tracker;
		
		@Override
		public void run() {
			try {
				tracker.submitResult(id, job.run(dockerManager, settings, id));
			}
			catch (Exception e) {
				log.error(e.getMessage(), e);
			}
		}
	}
	
	public static interface Job<T extends JobResult> {
		T run(DockerManager docker, Settings settings, UUID jobId) throws Exception;
	}

	@Slf4j
	public static class MavenJob implements Job<JobResult> {
		
		private final String repositoryUrl;
		private final String branch;
		private final String commit;

		public MavenJob(String repositoryUrl, String branch, String commit) {
			this.repositoryUrl = repositoryUrl;
			this.branch = branch;
			this.commit = commit;
		}

		private final AtomicReference<Integer> exitValue = new AtomicReference<Integer>();
		
		public JobResult run(DockerManager docker, Settings settings, UUID jobId) throws InterruptedException, GitAPIException {
			final List<String> logLines = Lists.<String>newArrayList();
			final File workingDirectory = new File(settings.getStagingDirectory().getValue(), jobId.toString());
			
			log.info("Creating working directory: {}", workingDirectory);
			workingDirectory.mkdir();
			
			log.info("Cloning: {} to directory: {}", repositoryUrl, workingDirectory);
			CloneCommand clone = Git.cloneRepository();
			clone.setBare(false);
            clone.setDirectory(workingDirectory);
            clone.setCloneAllBranches(true);
            clone.setURI(repositoryUrl);
            Git git = clone.call();
            
            log.info("Checking out revision: {} on branch: {}", commit, branch);
            CheckoutCommand checkout = git.checkout();
            checkout.setStartPoint(commit);
            checkout.setName(branch);
            checkout.call();
			
			Logger logger = new Logger() {
				@Override
				public void onNextLine(String line) {
					log.info(line);
					logLines.add(line);
				}

				@Override
				public void onClose(int exitCode) {
					log.info("Docker job terminated with exit code: {}", exitCode);
					exitValue.set(exitCode);

					try {
						log.info("Removing working directory: {}", workingDirectory);
						FileUtils.deleteDirectory(workingDirectory);
					}
					catch (IOException e) {
						log.error(e.getMessage(), e);
					}
				}
			};
			
			log.info("Starting docker job...");
			File remoteDirectory = new File(settings.getRemoteDirectory().getValue(), jobId.toString());
			Map<String, String> mounts = ImmutableMap.<String, String>of(remoteDirectory.getAbsolutePath(), "/workspace");
			docker.run(new DockerJob("devhub/builder:latest", "/workspace", "mvn clean package", mounts, logger));
			return new JobResult(jobId, exitValue, logLines);
		}
		
	}
	
	@Data
	public static class JobResult {
		private final UUID id;
		private final AtomicReference<Integer> exitCode;
		private final List<String> logLines;
	}
	
}
