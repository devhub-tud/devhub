package nl.tudelft.ewi.devhub.server.database.entities.identity;

import org.hibernate.HibernateException;
import org.hibernate.LockMode;
import org.hibernate.LockOptions;
import org.hibernate.MappingException;
import org.hibernate.cfg.Environment;
import org.hibernate.cfg.ObjectNameNormalizer;
import org.hibernate.dialect.Dialect;
import org.hibernate.engine.jdbc.internal.FormatStyle;
import org.hibernate.engine.jdbc.spi.JdbcServices;
import org.hibernate.engine.jdbc.spi.SqlStatementLogger;
import org.hibernate.engine.spi.SessionEventListenerManager;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.id.Configurable;
import org.hibernate.id.ForeignGenerator;
import org.hibernate.id.IdentifierGeneratorHelper;
import org.hibernate.id.IntegralDataTypeHolder;
import org.hibernate.id.PersistentIdentifierGenerator;
import org.hibernate.id.enhanced.AccessCallback;
import org.hibernate.id.enhanced.Optimizer;
import org.hibernate.id.enhanced.OptimizerFactory;
import org.hibernate.id.enhanced.StandardOptimizerDescriptor;
import org.hibernate.internal.util.StringHelper;
import org.hibernate.internal.util.config.ConfigurationHelper;
import org.hibernate.jdbc.AbstractReturningWork;
import org.hibernate.mapping.Table;
import org.hibernate.type.Type;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;

/**
 * The {@code FKSegmentedIdentifierGenerator} can be used for generating an auto incremented
 * part of a composite primary key. The {@code FKSegmentedIdentifierGenerator} is largely
 * based on the {@link org.hibernate.id.enhanced.TableGenerator} and the
 * {@link org.hibernate.id.ForeignGenerator}.
 *
 * Usage example:
 *
 * {@code <pre>
 *     class Entity {
 *
 *        @Id
 *        @ManyToOne(optional = false)
 *        @JoinColumn(name = "course_edition_id")
 *        private CourseEdition courseEdition;
 *
 *        @Id
 *        @GenericGenerator(name = "seq_group_number", strategy = "nl.tudelft.ewi.devhub.server.database.entities.identity.FKSegmentedIdentifierGenerator", parameters = {
 *          @Parameter(name = FKSegmentedIdentifierGenerator.TABLE_PARAM, value = "seq_group_number"),
 *          @Parameter(name = FKSegmentedIdentifierGenerator.PROPERTY_PARAM, value = "courseEdition")
 *        })
 *        @GeneratedValue(generator = "seq_group_number")
 *        @Column(name = "group_number", nullable = false)
 *        private long groupNumber;
 *
 *    }
 * </pre>}
 *
 * <p>The parameters for this {@code IdentifierGenerator} are as follows.
 *
 * <table>
 *    <tr>
 *        <td>{@link FKSegmentedIdentifierGenerator#TABLE_PARAM}</td>
 *        <td><strong>Required.</strong>
 *        The name for the table in which to store the next values for the keys.</td>
 *    </tr>
 *    <tr>
 *        <td>{@link FKSegmentedIdentifierGenerator#PROPERTY_PARAM}</td>
 *        <td><strong>Required.</strong>
 *        The property of the to-one relation to use for the foreign key.</td>
 *    </tr>
 *    <tr>
 *        <td>{@link FKSegmentedIdentifierGenerator#CLUSER_COLUMN_PARAM}</td>
 *        <td>The used column name for the segment in the generated table.
 *        Defaults to {@link FKSegmentedIdentifierGenerator#DEFAULT_CLUSER_COLUMN}</td>
 *    </tr>
 *    <tr>
 *        <td>{@link FKSegmentedIdentifierGenerator#INCREMENT_PARAM}</td>
 *        <td>Amount to increment.
 *        Defaults to {@link FKSegmentedIdentifierGenerator#DEFAULT_INCREMENT_SIZE}.</td>
 *    </tr>
 *	  <tr>
 *	      <td>{@link FKSegmentedIdentifierGenerator#INITIAL_PARAM}</td>
 *	      <td>Initial value for the generator.
 *	      Defaults to {@link FKSegmentedIdentifierGenerator#DEFAULT_INITIAL_VALUE}.</td>
 *	  </tr>
 * </table>
 *
 * @author Jan-Willem Gmelig Meyling
 * @see org.hibernate.id.enhanced.TableGenerator
 * @see org.hibernate.id.ForeignGenerator
 */
public class FKSegmentedIdentifierGenerator implements PersistentIdentifierGenerator, Configurable {

	/**
	 * Configures the name of the table to use.  The default value is {@link #DEF_TABLE}
	 */
	public static final String TABLE_PARAM = "table_name";

	/**
	 * The default {@link #TABLE_PARAM} value
	 */
	public static final String DEF_TABLE = "hibernate_sequences";

	/**
	 * The name of column which holds the sequence value.  The default value is {@link #DEF_VALUE_COLUMN}
	 */
	public static final String VALUE_COLUMN_PARAM = "value_column_name";

	/**
	 * The default {@link #VALUE_COLUMN_PARAM} value
	 */
	public static final String DEF_VALUE_COLUMN = "next_val";

	/**
	 * Indicates the initial value to use.  The default value is {@link #DEFAULT_INITIAL_VALUE}
	 */
	public static final String INITIAL_PARAM = "initial_value";

	/**
	 * The default {@link #INITIAL_PARAM} value
	 */
	public static final int DEFAULT_INITIAL_VALUE = 1;

	/**
	 * Indicates the increment size to use.  The default value is {@link #DEFAULT_INCREMENT_SIZE}
	 */
	public static final String INCREMENT_PARAM = "increment_size";

	/**
	 * The default {@link #INCREMENT_PARAM} value.
	 */
	public static final int DEFAULT_INCREMENT_SIZE = 1;

	/**
	 * Indicates the optimizer to use, either naming a {@link Optimizer} implementation class or by naming
	 * a {@link StandardOptimizerDescriptor} by name.
	 */
	public static final String OPT_PARAM = "optimizer";

	/**
	 * Cluster cloumn property.
	 */
	public static final String CLUSER_COLUMN_PARAM = "cluster_column";

	/**
	 * Default cluster column name.
	 */
	public static final String DEFAULT_CLUSER_COLUMN = "cluster";

	/**
	 * Property name for the to-one relationship.
	 */
	public static final String PROPERTY_PARAM = "property";

	private Type identifierType;
	private String tableName;
	private String valueColumnName;
	private long initialValue;
	private int incrementSize;
	private String selectQuery;
	private String insertQuery;
	private String updateQuery;
	private Optimizer optimizer;
	private String targetTableName;
	private String clusterColumn;

	private final ForeignGenerator foreignGenerator = new ForeignGenerator();

	@Override
	public Object generatorKey() {
		return tableName;
	}

	@Override
	public void configure(final Type type, final Properties params, final Dialect dialect) throws MappingException {
		foreignGenerator.configure(type, params, dialect);

		identifierType = type;

		tableName = determineGeneratorTableName(params, dialect);
		valueColumnName = determineValueColumnName(params, dialect);

		initialValue = determineInitialValue(params);
		incrementSize = determineIncrementSize(params);

		targetTableName = ConfigurationHelper.getString(PersistentIdentifierGenerator.TABLE, params);
		clusterColumn = ConfigurationHelper.getString(CLUSER_COLUMN_PARAM, params, DEFAULT_CLUSER_COLUMN);

		selectQuery = buildSelectQuery(dialect);
		updateQuery = buildUpdateQuery();
		insertQuery = buildInsertQuery();

		// if the increment size is greater than one, we prefer pooled optimization; but we
		// need to see if the user prefers POOL or POOL_LO...
		final String defaultPooledOptimizerStrategy = ConfigurationHelper.getBoolean(Environment.PREFER_POOLED_VALUES_LO, params, false)
			? StandardOptimizerDescriptor.POOLED_LO.getExternalName()
			: StandardOptimizerDescriptor.POOLED.getExternalName();
		final String defaultOptimizerStrategy = incrementSize <= 1
			? StandardOptimizerDescriptor.NONE.getExternalName()
			: defaultPooledOptimizerStrategy;
		final String optimizationStrategy = ConfigurationHelper.getString(OPT_PARAM, params, defaultOptimizerStrategy);
		optimizer = OptimizerFactory.buildOptimizer(
			optimizationStrategy,
			identifierType.getReturnedClass(),
			incrementSize,
			ConfigurationHelper.getInt(INITIAL_PARAM, params, -1)
		);
	}

	/**
	 * Determine the table name to use for the generator values.
	 * <p/>
	 * Called during {@link #configure configuration}.
	 *
	 * @param params The params supplied in the generator config (plus some standard useful extras).
	 * @param dialect The dialect in effect
	 * @return The table name to use.
	 */
	protected String determineGeneratorTableName(final Properties params, final Dialect dialect) {
		String name = ConfigurationHelper.getString(TABLE_PARAM, params, DEF_TABLE);
		final boolean isGivenNameUnqualified = name.indexOf('.') < 0;
		if (isGivenNameUnqualified) {
			final ObjectNameNormalizer normalizer = (ObjectNameNormalizer) params.get(IDENTIFIER_NORMALIZER);
			name = normalizer.normalizeIdentifierQuoting(name);
			// if the given name is un-qualified we may neen to qualify it
			final String schemaName = normalizer.normalizeIdentifierQuoting(params.getProperty(SCHEMA));
			final String catalogName = normalizer.normalizeIdentifierQuoting(params.getProperty(CATALOG));
			name = Table.qualify(
				dialect.quote(catalogName),
				dialect.quote(schemaName),
				dialect.quote(name)
			);
		}
		// if already qualified there is not much we can do in a portable manner so we pass it
		// through and assume the user has set up the name correctly.

		return name;
	}

	/**
	 * Determine the name of the column in which we will store the generator persistent value.
	 * <p/>
	 * Called during {@link #configure configuration}.
	 *
	 * @param params The params supplied in the generator config (plus some standard useful extras).
	 * @param dialect The dialect in effect
	 * @return The name of the value column
	 */
	protected String determineValueColumnName(Properties params, Dialect dialect) {
		final ObjectNameNormalizer normalizer = (ObjectNameNormalizer) params.get(IDENTIFIER_NORMALIZER);
		final String name = ConfigurationHelper.getString(VALUE_COLUMN_PARAM, params, DEF_VALUE_COLUMN);
		return dialect.quote(normalizer.normalizeIdentifierQuoting(name));
	}

	protected int determineInitialValue(final Properties params) {
		return ConfigurationHelper.getInt(INITIAL_PARAM, params, DEFAULT_INITIAL_VALUE);
	}

	protected int determineIncrementSize(final Properties params) {
		return ConfigurationHelper.getInt(INCREMENT_PARAM, params, DEFAULT_INCREMENT_SIZE);
	}

	protected String buildSelectQuery(final Dialect dialect) {
		final String alias = "tbl";
		final String query = "select " + StringHelper.qualify(alias, valueColumnName) +
			" from " + tableName + ' ' + alias +
			" where " + StringHelper.qualify(alias, clusterColumn) + "=?";
		final LockOptions lockOptions = new LockOptions(LockMode.PESSIMISTIC_WRITE);
		lockOptions.setAliasSpecificLockMode(alias, LockMode.PESSIMISTIC_WRITE);
		final Map updateTargetColumnsMap = Collections.singletonMap(alias, new String[]{valueColumnName});
		return dialect.applyLocksToSql(query, lockOptions, updateTargetColumnsMap);
	}

	protected String buildUpdateQuery() {
		return "update " + tableName +
			" set " + valueColumnName + "=? " +
			" where " + valueColumnName + "=? and " + clusterColumn + "=?";
	}

	protected String buildFetchCurValueQuery() {
		return "select MAX(" + clusterColumn + ") + 1 as " + valueColumnName +
		" from " + targetTableName + " where " + clusterColumn +" =? group by " + clusterColumn;
	}

	protected String buildInsertQuery() {
		return "insert into " + tableName + " (" + clusterColumn + ", " + valueColumnName + ") values (?,?)";
	}

	private IntegralDataTypeHolder makeValue() {
		return IdentifierGeneratorHelper.getIntegralDataTypeHolder(identifierType.getReturnedClass());
	}

	@Override
	public Serializable generate(final SessionImplementor session, final Object obj) {
		Serializable segmentValueSerialized = foreignGenerator.generate(session, obj);
		
		if(segmentValueSerialized.equals(IdentifierGeneratorHelper.SHORT_CIRCUIT_INDICATOR)) {
			return IdentifierGeneratorHelper.SHORT_CIRCUIT_INDICATOR;
		}
		
		final long segmentValue = (Long) segmentValueSerialized;
		
		final SqlStatementLogger statementLogger = session.getFactory().getServiceRegistry()
			.getService(JdbcServices.class)
			.getSqlStatementLogger();
		final SessionEventListenerManager statsCollector = session.getEventListenerManager();

		return optimizer.generate(new AccessCallback() {
			@Override
			public IntegralDataTypeHolder getNextValue() {
				return session.getTransactionCoordinator().getTransaction().createIsolationDelegate().delegateWork(
					new IntegralDataTypeHolderAbstractReturningWork(statementLogger, statsCollector, segmentValue),
					true
				);
			}

			@Override
			public String getTenantIdentifier() {
				return session.getTenantIdentifier();
			}
		});
	}

	private PreparedStatement prepareStatement(
		final Connection connection,
		final String sql,
		final SqlStatementLogger statementLogger,
		final SessionEventListenerManager statsCollector) throws SQLException {
		statementLogger.logStatement(sql, FormatStyle.BASIC.getFormatter());
		try {
			statsCollector.jdbcPrepareStatementStart();
			return connection.prepareStatement(sql);
		}
		finally {
			statsCollector.jdbcPrepareStatementEnd();
		}
	}

	protected int executeUpdate(final PreparedStatement ps, final SessionEventListenerManager statsCollector) throws SQLException {
		try {
			statsCollector.jdbcExecuteStatementStart();
			return ps.executeUpdate();
		}
		finally {
			statsCollector.jdbcExecuteStatementEnd();
		}

	}

	protected ResultSet executeQuery(final PreparedStatement ps, final SessionEventListenerManager statsCollector) throws SQLException {
		try {
			statsCollector.jdbcExecuteStatementStart();
			return ps.executeQuery();
		}
		finally {
			statsCollector.jdbcExecuteStatementEnd();
		}
	}

	@Override
	public String[] sqlCreateStrings(final Dialect dialect) throws HibernateException {
		return new String[] {
			dialect.getCreateTableString() + ' ' + tableName + " ("
				+ clusterColumn + ' ' + dialect.getTypeName(Types.BIGINT) + " not null "
				+ ", " + valueColumnName + ' ' + dialect.getTypeName(Types.BIGINT)
				+ ", primary key (" + clusterColumn + "))" + dialect.getTableTypeString()
		};
	}

	@Override
	public String[] sqlDropStrings(final Dialect dialect) throws HibernateException {
		return new String[] { dialect.getDropTableString(tableName) };
	}

	protected class IntegralDataTypeHolderAbstractReturningWork extends AbstractReturningWork<IntegralDataTypeHolder> {

		protected final IntegralDataTypeHolder value;
		protected final SqlStatementLogger statementLogger;
		protected final SessionEventListenerManager statsCollector;
		protected final long segmentValue;

		public IntegralDataTypeHolderAbstractReturningWork(SqlStatementLogger statementLogger, SessionEventListenerManager statsCollector, long segmentValue) {
			this.statementLogger = statementLogger;
			this.statsCollector = statsCollector;
			this.segmentValue = segmentValue;
			value = makeValue();
		}

		@Override
		public IntegralDataTypeHolder execute(final Connection connection) throws SQLException {
			int rows;
			do {
				try(final PreparedStatement selectPS = prepareStatement(connection, selectQuery, statementLogger, statsCollector)) {
					selectPS.setLong(1, segmentValue);
					try(final ResultSet selectRS = executeQuery(selectPS, statsCollector)) {
						if (!selectRS.next()) {
							createInitialValue(connection);
						}
						else {
							value.initialize(selectRS, 1);
						}
					}
				}
				rows = incrementAndUpdateValue(connection);
			}
			while (rows == 0);

			return value;
		}

		protected void createInitialValue(final Connection connection) throws SQLException {
			try(final PreparedStatement initValStmt = prepareStatement(connection, buildFetchCurValueQuery(), statementLogger, statsCollector)) {
				initValStmt.setLong(1, segmentValue);
				try(final ResultSet initValRes = executeQuery(initValStmt, statsCollector)) {
					if(initValRes.next()) {
						value.initialize(initValRes, 1);
					}
					else {
						value.initialize(initialValue);
					}
					persistInitialValue(connection);
				}
			}
		}

		protected int incrementAndUpdateValue(final Connection connection) throws SQLException {
			try(final PreparedStatement updatePS = prepareStatement(connection, updateQuery, statementLogger, statsCollector)) {
				final IntegralDataTypeHolder updateValue = value.copy();
				if (optimizer.applyIncrementSizeToSourceValues()) {
					updateValue.add(incrementSize);
				}
				else {
					updateValue.increment();
				}
				updateValue.bind(updatePS, 1);
				value.bind(updatePS, 2);
				updatePS.setLong(3, segmentValue);
				return executeUpdate(updatePS, statsCollector);
			}
		}

		protected void persistInitialValue(final Connection connection) throws SQLException {
			try(final PreparedStatement insertPS = prepareStatement(connection, insertQuery, statementLogger, statsCollector)) {
				insertPS.setLong(1, segmentValue);
				value.bind(insertPS, 2);
				if(executeUpdate(insertPS, statsCollector) == 0) {
					throw new IllegalStateException("Failed to insert initial key value");
				}
			}
		}
	}

}
