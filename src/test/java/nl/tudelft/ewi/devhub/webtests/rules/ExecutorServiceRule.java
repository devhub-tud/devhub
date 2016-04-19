package nl.tudelft.ewi.devhub.webtests.rules;

import lombok.Getter;
import org.junit.rules.ExternalResource;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author Jan-Willem Gmelig Meyling
 */
public class ExecutorServiceRule extends ExternalResource {

    @Getter
    private ExecutorService executorService;

    @Override
    protected void before() throws Throwable {
        executorService = Executors.newCachedThreadPool();
    }

    @Override
    protected void after() {
        executorService.shutdownNow();
    }

}
