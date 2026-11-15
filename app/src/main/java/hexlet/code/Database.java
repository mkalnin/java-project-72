package hexlet.code;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class Database {
    private static final String DEFAULT_JDBC_URL = "jdbc:h2:mem:project;DB_CLOSE_DELAY=-1";

    private static DataSource dataSource;

    public static DataSource getDataSource() {
        if (dataSource == null) {
            var config = new HikariConfig();
            config.setJdbcUrl(getJdbcUrl());
            dataSource = new HikariDataSource(config);
        }
        return dataSource;
    }

    public static void reset() throws SQLException, IOException {
        try (Connection connection = getDataSource().getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute(readSchema());
        }
    }

    private static String getJdbcUrl() {
        var url = System.getenv("JDBC_DATABASE_URL");
        return url == null || url.isBlank() ? DEFAULT_JDBC_URL : url;
    }

    private static String readSchema() throws IOException {
        try (InputStream input = Database.class.getResourceAsStream("/schema.sql")) {
            if (input == null) {
                throw new IOException("schema.sql not found in resources");
            }
            return new String(input.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
