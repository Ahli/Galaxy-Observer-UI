package db.migration;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;
import org.flywaydb.core.internal.jdbc.JdbcTemplate;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;

@SuppressWarnings("java:S101")
public class V3__ImportV1data extends BaseJavaMigration {
	private static final Logger logger = LogManager.getLogger(V3__ImportV1data.class);
	
	@Override
	public void migrate(final Context context) throws SQLException {
		final Path migrationScriptPath =
				Path.of(System.getProperty("user.home"), ".GalaxyObsUI", "database", "exportMigrate.zip");
		if (Files.exists(migrationScriptPath)) {
			final JdbcTemplate jdbcTemplate =
					new JdbcTemplate(new SingleConnectionDataSource(context.getConnection(), true).getConnection());
			jdbcTemplate.executeStatement("DROP ALL OBJECTS");
			jdbcTemplate.execute("RUNSCRIPT FROM '~/.GalaxyObsUI/database/exportMigrate.zip' COMPRESSION ZIP FROM_1X");
			try {
				Files.delete(migrationScriptPath);
			} catch (final IOException e) {
				logger.error("Failed to clear H2DB V1 migration script.", e);
			}
		}
	}
}
