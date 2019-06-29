package io.prestosql.plugin.vertica;

import io.prestosql.plugin.jdbc.*;
import io.prestosql.spi.PrestoException;
import io.prestosql.spi.connector.SchemaTableName;
import com.google.common.collect.ImmutableSet;
import com.vertica.jdbc.Driver;

import javax.inject.Inject;
import java.sql.*;
import java.util.Properties;
import java.util.Set;

import java.util.Optional;
import java.util.Collection;

import static io.prestosql.plugin.jdbc.DriverConnectionFactory.basicConnectionProperties;
import static io.prestosql.plugin.jdbc.JdbcErrorCode.JDBC_ERROR;
import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Locale.ENGLISH;


public class VerticaClient extends BaseJdbcClient {

    @Inject
    public VerticaClient(BaseJdbcConfig config) {
        super(config, "", connectionFactory(config));
    }

    private static ConnectionFactory connectionFactory(BaseJdbcConfig config) {
        checkArgument(config.getConnectionUrl() != null, "Missing JDBC URL for vertica connector");
        checkArgument(config.getConnectionUser() != null, "Invalid JDBC User for vertica connector");
        checkArgument(config.getConnectionPassword() != null, "Invalid JDBC Password for vertica connector");
        Properties connectionProperties = basicConnectionProperties(config);
        connectionProperties.setProperty("user", config.getConnectionUser());
        connectionProperties.setProperty("url", config.getConnectionUrl());
        connectionProperties.setProperty("password", config.getConnectionPassword());
        return new DriverConnectionFactory(new Driver(), config.getConnectionUrl(), Optional.empty(), Optional.empty(), connectionProperties);
    }

    @Override
    protected Collection<String> listSchemas(Connection connection) {
        try (ResultSet resultSet = connection.getMetaData().getSchemas()) {
            ImmutableSet.Builder<String> schemaNames = ImmutableSet.builder();
            while (resultSet.next()) {
                String schemaName = resultSet.getString("TABLE_SCHEM").toLowerCase(ENGLISH);
                if (!schemaName.equals("v_monitor") && !schemaName.equals("v_txtindex") && !schemaName.equals("v_catalog")) {
                    schemaNames.add(schemaName);
                }
            }
            return schemaNames.build();
        } catch (SQLException e) {
            throw new PrestoException(JDBC_ERROR, e);
        }
    }

    @Override
    protected ResultSet getTables(Connection connection, Optional<String> schemaName, Optional<String> tableName) throws SQLException {
        DatabaseMetaData metadata = connection.getMetaData();
    	Optional<String> escape = Optional.ofNullable(metadata.getSearchStringEscape());
        return metadata.getTables(
                connection.getCatalog(),
                escapeNamePattern(schemaName, escape).orElse(null),
                escapeNamePattern(tableName, escape).orElse(null),
                new String[]{"VIEW", "TABLE", "SYNONYM"});
    }

    @Override
    public PreparedStatement getPreparedStatement(Connection connection, String sql) throws SQLException {
        connection.setAutoCommit(false);
        PreparedStatement statement = connection.prepareStatement(sql);
        return statement;
    }

    @Override
        protected String getTableSchemaName(ResultSet resultSet) throws SQLException {
		return resultSet.getString("TABLE_SCHEM").toLowerCase(ENGLISH);
	}


}
