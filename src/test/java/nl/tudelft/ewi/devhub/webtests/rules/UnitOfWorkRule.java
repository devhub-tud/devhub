package nl.tudelft.ewi.devhub.webtests.rules;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.persist.UnitOfWork;
import org.junit.rules.MethodRule;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

/**
 * @author Jan-Willem Gmelig Meyling
 */
public class UnitOfWorkRule implements MethodRule {

    private final Provider<UnitOfWork> unitOfWorkProvider;

    @Inject
    public UnitOfWorkRule(Provider<UnitOfWork> unitOfWorkProvider) {
        this.unitOfWorkProvider = unitOfWorkProvider;
    }

    @Override
    public Statement apply(Statement statement, FrameworkMethod frameworkMethod, Object o) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                UnitOfWork unitOfWork = unitOfWorkProvider.get();
                unitOfWork.begin();
                try {
                    statement.evaluate();
                }
                catch (Exception e) {
                    unitOfWork.end();
                    throw e;
                }
            }
        };
    }

}
