package nl.tudelft.ewi.devhub.server;

import lombok.extern.slf4j.Slf4j;

import com.google.inject.Singleton;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Properties;

/**
 * The {@link Config} class can be used to retrieve configuration settings from the
 * <code>config.properties</code> file.
 */
@Slf4j
@Singleton
public class Config {

	private final Properties properties;

	/**
	 * Constructs a new {@link Config} object and attempts to load properties from the
	 * <code>config.properties</code> file.
	 */
	public Config() {
		this.properties = new Properties();
		reload();
	}

	/**
	 * Retrieves the configuration from the <code>config.properties</code> file and updates this
	 * {@link Config} object with the new values.
	 */
	public void reload() {
		try (InputStreamReader reader = new InputStreamReader(getClass().getResourceAsStream("/config.properties"))) {
			properties.load(reader);
		}
		catch (IOException e) {
			log.error(e.getMessage());
		}
	}

	/**
	 * @return The HTTP port on which to run the DEVHUB server.
	 */
	public int getHttpPort() {
		return Integer.parseInt(properties.getProperty("http.port", "8080"));
	}

	/**
	 * @return The primary HTTP URL on which the DEVHUB server is accessible.
	 */
	public String getHttpUrl() {
		return properties.getProperty("http.url");
	}

	/**
	 * @return The HTTP URL on which we can reach the GIT server.
	 */
	public String getGitServerHost() {
		return properties.getProperty("git-server.host");
	}

	/**
	 * @return The hostname of the SMTP server.
	 */
	public String getSmtpHost() {
		return properties.getProperty("smtp-server.host");
	}

	/**
	 * @return The username to use when authenticating with the SMTP server.
	 */
	public String getSmtpUser() {
		return properties.getProperty("smtp-server.user");
	}

	/**
	 * @return The password to use when authenticating with the SMTP server.
	 */
	public String getSmtpPass() {
		return properties.getProperty("smtp-server.pass");
	}

	/**
	 * @return The no-reply address to use in the <code>From</code> field in sent e-mails.
	 */
	public String getSmtpOrigin() {
		return properties.getProperty("smtp-server.origin");
	}
	
	/**
	 * @return the LDAP host address, for example ldaps.tudelft.nl
	 */
	public String getLDAPHost() {
		return properties.getProperty("ldap-server.host"); 
	}
	
	/**
	 * @return return the LDAP port, for example 636
	 */
	public int getLDAPPort() {
		return Integer.parseInt(properties.getProperty("ldap-server.port", "636"));
	}

	/**
	 * @return git server connection pool size
	 */
	public int getGitServerConnectionPoolSize() {
		return Integer.parseInt(properties.getProperty("git-server.connection.pool-size", "25"));
	}

	/**
	 * @return whether or not to use SSL for this LDAP connection
	 */
	public boolean isLDAPSSL() {
		return properties.getProperty("ldap-server.usessl", "true")
				.equalsIgnoreCase("true");
	}
	
	/**
	 * @return the LDAP extension. This is what follows after the username in
	 *         order to logon to the domain. For example {@code &#64;tudelft.nl}
	 */
	public String getLDAPExtension() {
		return properties.getProperty("ldap.extension", "");
	}
	
	/**
	 * @return the LDAP tree in which users can be found
	 */
	public String getLDAPPrimaryDomain() {
		return properties.getProperty("ldap.primarydomain");
	}

    /**
     * @return the storage folder used for storing files
     */
    public File getStorageFolder() {
        return new File(properties.getProperty("storage.folder"));
    }

}
