# Contributing to Devhub

Devhub is organized in three components: `devhub-server`, `git-server` and `build-server`.
Both the `git-server` and `git-server` expose REST APIs, for which a Java client interfaces are available.
This is the repository for `devhub-server`.

### Clone Devhub
The web dependencies ([jQuery](http://api.jquery.com), [Highlight.js](https://highlightjs.org), [Bootstrap](http://getbootstrap.com) and [Octicons](https://octicons.github.com)) are loaded through submodules.
After cloning the repository, you have to initialize these submodules.

```sh
git clone git@github.com:devhub-tud/devhub.git
git submodules init
git submodules update
```

### Dependencies
* Requires Maven 3.2 or higher, tested on Apache Maven 3.2.1 and 3.3.3.

### Setting up your IDE
*These steps were only tested for Eclipse and IntelliJ.*
To succesfully run and test Devhub, a couple of steps should be followed to setup various dependencies and configurations.


1. Fork the Devhub repository.
1. Open the preferred IDE.
1. Import the project as Maven project.
1. Make sure that JDK 1.8 is being used for building Devhub.
1. Run `mvn generate-resources`, this command should create various classes in `target/metamodel`.
1. (For Eclipse) Set `target/metamodel` as *source* directory in the IDE. You can do this under Properties > Build Path). Sometimes it is required to first remove the folder, and then include it again.
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
		
When the JPA plugin for Eclipse generates a lot of false errors, you can disable the validation under `Preferences > Validation > JPA Validator`.
		
### Code Formatting
We do not write getters/setters and equals/hashcode implementations ourselves, but instead use the Lombok `@Data` and `@EqualsAndHashcode` annotations for that. Prevent overusage of Lombok, try to limit it to JPA classes and Jackson models.

### Schema changes
We use Liquibase to perform the required schema changes in deployment.
When a schema update is required for your changes, add a new entry at the bottom of the `changelog.xml` file.
Do **NOT** alter existing changelog entries.

### Less CSS changes
We use Less in order to generate CSS.
The Less is transfored to CSS using the [`lesscss-maven-plugin`](https://github.com/marceloverdijk/lesscss-maven-plugin).
You can recompile the less file using `mvn lesscss:compile`.
There is no need to restart the Devhub server afterwards.

### Running Devhub
In the test source folder (`src/test`) you will find `DevhubInMockedEnvironment`. It uses almost the same bindings as Devhub in *production*, but with small modifications:

* A temporary embedded database (H2) is used
* The temporary embedded database is filled with data from the `Bootstrapper`, which parses a JSON configuration file (`simple-environment.json` by default).
* The authentication does not fall back to LDAP, but only checks against the values in the database.
* Instead of an actual git-server, it uses the git server classes against local file based repositories.


#### Using a real Git server
First start up a real Git server. In the `git-server` repository we can find a `vagrantfile` file which can be used to set up a Gitolite VM.
[Gitolite](http://gitolite.com/gitolite/index.html) is the actual Git daemon we use.
In order to configure a Virtual Machine from a `vagrantfile`, you need to have [VirtualBox](https://www.virtualbox.org) and [Vagrant](https://www.vagrantup.com) installed.

```sh
cd git-server
vagrant up
java -jar git-server/target/git-server-distribution/git-server/git-server.jar 
```

The `GitServerClientImpl` connects to the address read from the `config.properties` file (default: `http://localhost:8081`).

### Further reading
Before contributing to Devhub it's hugely recommended to read upon JAX-RS (we use the [Resteasy](http://docs.jboss.org/resteasy/docs/3.0.9.Final/userguide/html/index.html) implementation), JPA (we use [Hibernate](http://docs.jboss.org/hibernate/orm/4.3/manual/en-US/html/)), [Freemarker](http://freemarker.org/docs/index.html) and [Google Guice](https://github.com/google/guice/wiki/Motivation) dependency injection.
