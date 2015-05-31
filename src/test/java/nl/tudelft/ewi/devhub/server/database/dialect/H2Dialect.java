package nl.tudelft.ewi.devhub.server.database.dialect;

import java.sql.Types;

/**
 * Modified version of the {@link H2Dialect} for testing under H2
 *
 * @author Jan-Willem Gmelig Meyling
 */
public class H2Dialect extends org.hibernate.dialect.H2Dialect {

    public H2Dialect() {
        super();
        registerColumnType(Types.LONGVARCHAR, "clob");
    }

}
