# Contributing to Devhub

## Development Environment Setup
To succesfully run and test Devhub, a couple of steps should be followed to setup various dependencies and configurations. *These steps were only tested for Eclipse and IntelliJ.*

1. Fork the Devhub repository.
1. Open the preferred IDE.
1. Import the project as Maven project.
1. Set `target/metamodel` as *source* directory.
1. Execute the following Maven commands:
	* `mvn generate-resources`, this command should create various classes in `target/metamodel`.
	* `mvn install`, this command should install all Maven dependencies.
1. Install Lombok.
	* For **Eclilpse** follow these steps:
		1. Close Eclipse.
		1. Run the following JAR: `.m2/repository/org/projectlombok/lombok/LATEST_LOMBOK_VERSION/lombok-LATEST_LOMBOK_VERSION.jar`. 
		1. The installation will provide the required steps to attach Lombok to Eclipse.
	* For **IntelliJ** follow these steps:
		1. "File"
		1. "Settings..."
		1. "Plugins"
		1. "Browse repositories..."
		1. Search for "lombok"
		1. Install "Lombok Plugin"

