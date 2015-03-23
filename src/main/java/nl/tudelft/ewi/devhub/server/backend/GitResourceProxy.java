package nl.tudelft.ewi.devhub.server.backend;

import java.net.URI;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import nl.tudelft.ewi.devhub.server.Config;

import org.eclipse.jetty.proxy.ProxyServlet.Transparent;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class GitResourceProxy extends Transparent {
	
	private static final long serialVersionUID = 2483225678350698884L;
	
	private final Pattern pattern;
	private final Config config;
	
	@Inject
	public GitResourceProxy(final Config config) {
		super(config.getGitServerHost(), "/");
		this.config = config;
		this.pattern = Pattern.compile("^/courses/([^/]+)/groups/(\\d+)/(\\w+)/raw/(.*)$");
	}

    protected URI rewriteURI(HttpServletRequest request) {
        String uri = request.getRequestURI();
        Matcher matcher = pattern.matcher(uri);
        
        StringBuffer url = new StringBuffer(config.getGitServerHost());
        url.append("/api/repositories");
        if(matcher.matches()) {
        	// Repository name
			url.append('/').append("courses%2F")
					.append(matcher.group(1).toLowerCase()).append("%2Fgroup-")
					.append(matcher.group(2));
        	url.append("/file");
        	// Commit id
        	url.append('/').append(matcher.group(3));
        	// Path
        	url.append('/').append(matcher.group(4));
        }
        else {
        	throw new IllegalArgumentException("No match was found for " + uri);
        }

        return URI.create(url.toString());
    }
	
}
