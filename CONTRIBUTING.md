# Contributing to Devhub

## Development Environment Setup
To succesfully run and test Devhub, a couple of steps should be followed to setup various dependencies and configurations. *These steps were only tested for Eclipse and IntelliJ.*

1. Fork the Devhub repository.
1. Open the preferred IDE.
1. Import the project as Maven project.
1. Make sure that JDK 1.8 is being used for building Devhub.
1. Run `mvn generate-resources`, this command should create various classes in `target/metamodel`.
1. Set `target/metamodel` as *source* directory in the IDE.
1. Run `mvn compile`, this compiles all application sources.
1. Install [Project Lombok](https://projectlombok.org/).
	* For **Eclipse** follow these steps:
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
