package nl.tudelft.ewi.git.models;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;


@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CreateRepositoryModelFactory {

	public static CreateRepositoryModel create(String templateRepo, String name, String url) {
		CreateRepositoryModel repositoryModel = new CreateRepositoryModel();
		repositoryModel.setTemplateRepository(templateRepo);
		repositoryModel.setName(name);
		repositoryModel.setUrl(url);
		return repositoryModel;
	}
	
}
