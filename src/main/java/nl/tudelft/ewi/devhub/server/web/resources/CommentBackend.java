package nl.tudelft.ewi.devhub.server.web.resources;

import java.util.List;

import nl.tudelft.ewi.devhub.server.backend.GitBackend;
import nl.tudelft.ewi.devhub.server.database.controllers.Commits;
import nl.tudelft.ewi.devhub.server.database.entities.Commit;
import nl.tudelft.ewi.devhub.server.database.entities.CommitComment;
import nl.tudelft.ewi.devhub.server.database.entities.Group;
import nl.tudelft.ewi.devhub.server.web.errors.ApiError;
import nl.tudelft.ewi.git.models.CommitModel;
import nl.tudelft.ewi.git.models.DiffContext;
import nl.tudelft.ewi.git.models.DiffLine;
import nl.tudelft.ewi.git.models.DiffModel;
import nl.tudelft.ewi.git.models.DiffResponse;
import nl.tudelft.ewi.git.models.RepositoryModel;

import com.google.common.collect.Lists;
import com.google.inject.Inject;

public class CommentBackend {

	private final Commits commits;
	private final GitBackend gitBackend;
	
	@Inject
	public CommentBackend(final Commits commits, final GitBackend gitBackend) {
		this.commits = commits;
		this.gitBackend = gitBackend;
	}
	
	public CommentChecker newComments(final Group group, final RepositoryModel repository,
			final CommitModel commit, final DiffResponse diffResponse) throws ApiError {
		return new CommentChecker(group, repository, commit, diffResponse);
	}
	
	public class CommentChecker {
		
		private final Group group;
		private final RepositoryModel repository;
		private final List<CommitComment> comments;
		
		public CommentChecker(final Group group, final RepositoryModel repository,
                              final CommitModel commit, final DiffResponse diffResponse) throws ApiError {
			this.group = group;
			this.repository = repository;
			this.comments = calculateCommits(commit, diffResponse.getCommits());
		}
		
		public List<CommitComment> getComments() {
			return comments;
		}
		
		public List<CommitComment> commentsForLine(String oldPath, Integer oldLineNumber, String newPath, Integer newLineNumber) {
			List<CommitComment> result = Lists.<CommitComment> newArrayList();
			for(CommitComment comment : comments) {
				if ((oldPath != null && oldLineNumber != null
						&& oldPath.equals(comment.getOldFilePath())
						&& oldLineNumber.equals(comment.getOldLineNumber()))
				||  (newPath != null && newLineNumber != null
						&& newPath.equals(comment.getNewFilePath())
						&& newLineNumber.equals(comment.getNewLineNumber()))) {
					result.add(comment);
				}
			}
			return result;
		}

		/**
		 * From the first commit
		 *   if comments
		 *      fetch comments
		 * 	For each parent in ahead
		 *     get recursive comments
		 *     translate comments against diff, remove removed comments
		 *     add translated comments
		 */
		protected List<CommitComment> calculateCommits(final CommitModel commitModel, final List<CommitModel> parents) throws ApiError {
			List<CommitComment> comments = Lists.<CommitComment> newArrayList();
			
			for(String parentId : commitModel.getParents()) {
				for(CommitModel parentModel : parents) {
					if(parentModel.getCommit().equals(parentId)) {
						List<CommitComment> parentComments = calculateCommits(parentModel, parents);
						comments.addAll(translatedComments(commitModel, parentModel, parentComments));
					}
				}
			}
			
			Commit commit = commits.retrieve(group, commitModel.getCommit());
			if(commit != null) {
				comments.addAll(commit.getComments());
			}
			return comments;
		}
		
		/**
		 * We have the following options:
		 * 
		 * <ul>
		 * 	<li>modified/added in old commit, file unchanged in new commit
		 * 	  <ul>
		 * 	    <li>keep linenumbers</li>
		 *      <li>new file path in old commit should equal old file path</li>
		 *    </ul>
		 *  </li>
		 *  <li>modified/added in old commit, file changed in commit
		 *    <ul>
		 *      <li>calculate new position, omit if line changed</li>
		 *      <li>new file path in old commit should equal old file path, set to
		 *          new file path for renames</li>
		 *    </ul>
		 *  </li>
		 *  <li>removed in old commit, unchanged
		 *    <ul>
		 *      <li>keep as is</li>
		 *      <li>new file path is null</li>
		 *    </ul>
		 *  </li>
		 *  <li>removed in old commit, then changed
		 *    <ul>
		 *      <li>omit</li>
		 *      <li>new file path is null</li>
		 *      <li>old file path exists in diff</li>
		 *    </ul>
		 *  </li>
		 * </ul>
		 */
		protected List<CommitComment> translatedComments(
				final CommitModel source, final CommitModel parent,
				final List<CommitComment> comments) throws ApiError {
			final DiffResponse diff = gitBackend.fetchDiffs(repository, parent.getCommit(), source.getCommit());
			final List<CommitComment> resultComments = Lists.newArrayListWithCapacity(comments.size());
			COMMENTS : for(CommitComment input : comments) {
				String oldPath = input.getOldFilePath();
				String newPath = input.getNewFilePath();
				
				if(equalsNullPath(newPath)) {
					// Comment was on a deleted file
					for(DiffModel diffModel : diff.getDiffs()) {
						if(diffModel.getNewPath() != null && diffModel.getNewPath().equals(oldPath)) {
							// File was added again, omit comment
							continue COMMENTS;
						}
					}
					// File was unchanged, keep comment as is
					resultComments.add(input);
					continue COMMENTS;
				}
				else {
					// File of comment was not removed
					for(DiffModel diffModel : diff.getDiffs()) {
						if(diffModel.getOldPath() != null && diffModel.getOldPath().equals(newPath)) {
							if(diffModel.getNewPath() == null) {
								// File was modified, but is now removed, omit comment
								continue COMMENTS;
							}
							else if (input.getNewLineNumber() == null) {
								// Line was removed, but file still exists
								// Compared to the merge base, the old number is the same
								resultComments.add(input);
								continue COMMENTS;
							}
							else {
								// File also modified in this commit
								CommitComment result = new CommitComment();
								// Copy base comment
								result.setCommit(input.getCommit());
								result.setContent(input.getContent());
								result.setTime(input.getTime());
								result.setUser(input.getUser());
								result.setOldFilePath(input.getOldFilePath());
								result.setOldLineNumber(input.getOldLineNumber());
								// Set the file path if renamed
								result.setNewFilePath(diffModel.getNewPath());

								Integer newLineNumber = input.getNewLineNumber();
								
								// Calculate new file line number
								for(DiffContext diffContext : diffModel.getDiffContexts()) {
									if(newLineNumber != null) {
										if(diffContext.getOldEnd() < newLineNumber) {
											// A new block was inserted at the top
											// Increment the line number with the length difference
											newLineNumber += diffContext.getAddedCount() - diffContext.getRemovedCount();
										}
										else if (diffContext.getOldStart() <= newLineNumber && newLineNumber < diffContext.getOldEnd()) {
											// Inserted with this line is in the block
											// Increment new line number for every modification
											int oldIndex = diffContext.getOldStart();
											int insertedLines = 0;
											// Find the current position of the line by 
											// incrementing old line index for every not added line
											// and then at that amount to the current line number
											for(DiffLine diffLine : diffContext.getDiffLines()) {
												if(diffLine.isAdded()) {
													oldIndex++;
												}
												else {
													insertedLines++;
												}
												if(oldIndex == newLineNumber) {
													newLineNumber += insertedLines;
													break;
												}
											}
										}
									}
								} // End iterating over diff context to determine newLineNumber
								
								resultComments.add(result);
								continue COMMENTS;
							}
						} // End file name check, else breaking out of loop and adding comment unchanged
					}
					// File was unchanged, keep comment as is
					resultComments.add(input);
					continue COMMENTS;
				}
			}
			return resultComments;
		}
		
		protected List<CommitComment> commitsForCommit(final CommitModel commit) {
			return commits.retrieve(group, commit.getCommit()).getComments();
		}
		
	}
	
	private static boolean equalsNullPath(String path) {
		return path == null || path.equals("/dev/null");
	}
	
}
