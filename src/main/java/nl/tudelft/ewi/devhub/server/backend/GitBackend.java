package nl.tudelft.ewi.devhub.server.backend;

import java.io.File;
import java.util.Map;

import nl.tudelft.ewi.devhub.server.database.entities.Group;
import nl.tudelft.ewi.devhub.server.web.errors.ApiError;
import nl.tudelft.ewi.devhub.server.web.resources.Resource;
import nl.tudelft.ewi.git.client.GitServerClient;
import nl.tudelft.ewi.git.client.Repositories;
import nl.tudelft.ewi.git.models.BlameModel;
import nl.tudelft.ewi.git.models.BranchModel;
import nl.tudelft.ewi.git.models.CommitModel;
import nl.tudelft.ewi.git.models.DetailedBranchModel;
import nl.tudelft.ewi.git.models.DetailedCommitModel;
import nl.tudelft.ewi.git.models.DetailedRepositoryModel;
import nl.tudelft.ewi.git.models.DiffResponse;
import nl.tudelft.ewi.git.models.EntryType;
import nl.tudelft.ewi.git.models.RepositoryModel;

import com.google.inject.Inject;

/**
 * The {@link GitBackend} provides some methods for {@link Resource Resources}
 * that throw an {@link ApiError} when the request could not be handled.
 * 
 * @author Jan-Willem Gmelig Meyling
 *
 */
public class GitBackend {
	
	private final GitServerClient client;
	
	@Inject
	public GitBackend(final GitServerClient client) {
		this.client = client;
	}

	public DetailedRepositoryModel fetchRepositoryView(Group group) throws ApiError {
		try {
			Repositories repositories = client.repositories();
			return repositories.retrieve(group.getRepositoryName());
		}
		catch (Throwable e) {
			throw new ApiError("error.git-server-unavailable", e);
		}
	}

	public DetailedCommitModel fetchCommitView(DetailedRepositoryModel repository, String commitId) throws ApiError {
		try {
			Repositories repositories = client.repositories();
			return repositories.retrieveCommit(repository, commitId);
		}
		catch (Throwable e) {
			throw new ApiError("error.git-server-unavailable", e);
		}
	}
	
	public DiffResponse fetchDiffs(RepositoryModel repository, String oldCommitId, String newCommitId) throws ApiError {
		try {
			return client.repositories().listDiffs(repository, oldCommitId, newCommitId);
		} catch (Throwable e) {
			throw new ApiError("error.git-server-unavailable", e);
		}
	}
	
	public DetailedBranchModel fetchBranch(RepositoryModel repository,
			String branchName, int page, int pageSize) throws ApiError {
		try {
			return client.repositories().retrieveBranch(repository, branchName, (page - 1) * pageSize, pageSize);
		}
		catch (Throwable e) {
			throw new ApiError("error.git-server-unavailable", e);
		}
	}

	public DetailedBranchModel retrieveBranch(
			DetailedRepositoryModel repository, String branchName, int skip,
			int pageSize) throws ApiError {
		try {
			return client.repositories().retrieveBranch(repository, branchName, skip, pageSize);
		}
		catch (Throwable e) {
			throw new ApiError("error.git-server-unavailable", e);
		}
	}

	public Map<String, EntryType> listDirectoryEntries(
			DetailedRepositoryModel repository, String commitId, String path) throws ApiError {
		try {
			return client.repositories().listDirectoryEntries(repository, commitId, path);
		}
		catch (Throwable e) {
			throw new ApiError("error.git-server-unavailable", e);
		}
	}

	public File showBinFile(DetailedRepositoryModel repository,
			String commitId, String path) throws ApiError {
		try {
			return client.repositories().showBinFile(repository, commitId, path);
		}
		catch (Throwable e) {
			throw new ApiError("error.git-server-unavailable", e);
		}
	}

	public String showFile(DetailedRepositoryModel repository, String commitId,
			String path) throws ApiError {
		try {
			return client.repositories().showFile(repository, commitId, path);
		}
		catch (Throwable e) {
			throw new ApiError("error.git-server-unavailable", e);
		}
	}
	
	public CommitModel mergeBase(DetailedRepositoryModel repository, String oldCommitId, String newCommitId) throws ApiError {
		try {
			return client.repositories().mergeBase(repository, oldCommitId, newCommitId);
		}
		catch (Throwable e) {
			throw new ApiError("error.git-server-unavailable", e);
		}
	}
	
	public DiffResponse fetchDiffs(DetailedRepositoryModel repository, BranchModel branch) throws ApiError {
		CommitModel commitModel = branch.getCommit();
		BranchModel masterBranchModel = repository.getBranch("master");
		CommitModel masterCommitModel = masterBranchModel.getCommit();
		CommitModel mergeBase = mergeBase(repository,
				masterCommitModel.getCommit(), commitModel.getCommit());
		return fetchDiffs(repository, mergeBase.getCommit(), commitModel.getCommit());
	}

	public BlameModel blame(RepositoryModel repository, String commitId, String filePath) throws ApiError {
		try {
			return client.repositories().blame(repository, commitId, filePath);
		}
		catch (Throwable e) {
			throw new ApiError("error.git-server-unavailable", e);
		}
	}
	
}
