package nl.tudelft.ewi.devhub.server.backend;

import java.util.Map;
import java.util.Queue;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import nl.tudelft.ewi.devhub.server.Config;
import nl.tudelft.ewi.git.client.GitServerClient;
import nl.tudelft.ewi.git.client.GitServerClientImpl;
import nl.tudelft.ewi.git.models.CommitModel;
import nl.tudelft.ewi.git.models.DiffModel;
import nl.tudelft.ewi.git.models.DiffResponse;
import nl.tudelft.ewi.git.models.RepositoryModel;

import org.junit.BeforeClass;
import org.junit.Test;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Queues;

public class CommentBackend {

	private static GitServerClient client;
	
	@BeforeClass
	public static void prepare() {
		Config config = new Config();
		client = new GitServerClientImpl(config.getGitServerHost());
	}
	
	@Test
	public void fetchDiff() {
		RepositoryModel repository = client.repositories().retrieve("courses/ti1705/group-1");
		DiffResponse response = client.repositories().listDiffs(repository , null, "1e8307cc8892cd8bb1c257e96047b9b568ba4562");
		System.out.println(response);
	}
	
	@Data
	@EqualsAndHashCode
	@NoArgsConstructor
	@Accessors(fluent=true)
	static class Comment {
		private Integer line;
		private String file;
		private String comment;
	}
	
	@Test
	public void testDetectFirstCommit() {
		String path = "src/main/resources/static/css/devhub.css";
		Multimap<String, Comment> comments = ArrayListMultimap.<String, Comment> create();
		comments.put("97819baaae4eb7edc2e8dbf843172886079f5889",
				new Comment().comment("test")
					.file(path)
					.line(913));
		
		RepositoryModel repository = client.repositories().retrieve("courses/ti1705/group-1");
		DiffResponse response = client.repositories().listDiffs(repository , null, "97819baaae4eb7edc2e8dbf843172886079f5889");
		
		Map<String, CommitModel> commits = Maps.newHashMap();
		
		for(CommitModel commit : response.getCommits()) {
			commits.put(commit.getCommit(), commit);
		}

		CommitModel start = commits.get("97819baaae4eb7edc2e8dbf843172886079f5889");
		Queue<CommitModel> queue = Queues.<CommitModel> newArrayDeque();
		queue.add(start);
		
		while(!queue.isEmpty()) {
			CommitModel current = queue.poll();
			
			DiffResponse diffs = client.repositories().listDiffs(repository , null, current.getCommit());
			for(DiffModel diffModel : diffs.getDiffs()) {
				if(diffModel.getNewPath().equals(path)) {
					for(String commitId : current.getParents()) {
						CommitModel commit = commits.get(commitId);
						if(commit == null) continue;
						queue.add(commit);
					}
					System.out.println(current.getCommit());
					break;
				}
			}
			
		}
	}
	
	
}
