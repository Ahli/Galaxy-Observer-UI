package db.migration;

import lombok.extern.log4j.Log4j2;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;
import org.flywaydb.core.internal.jdbc.JdbcTemplate;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;

@Log4j2
@SuppressWarnings("java:S101")
public class V3__ImportV1data extends BaseJavaMigration {
	
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
				log.error("Failed to clear H2DB V1 migration script.", e);
			}
		}
	}
}
