package hexlet.code.repository;

import hexlet.code.model.UrlCheck;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class UrlCheckRepository extends BaseRepository {

    public UrlCheckRepository(DataSource dataSource) {
        super(dataSource);
    }

    public void save(UrlCheck check) throws SQLException {
        var sql = "INSERT INTO url_checks (url_id, status_code, h1, title, description, created_at) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        try (var connection = dataSource.getConnection();
             var statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            var createdAt = check.getCreatedAt() == null
                    ? new Timestamp(System.currentTimeMillis())
                    : check.getCreatedAt();
            statement.setLong(1, check.getUrlId());
            statement.setInt(2, check.getStatusCode());
            statement.setString(3, check.getH1());
            statement.setString(4, check.getTitle());
            statement.setString(5, check.getDescription());
            statement.setTimestamp(6, createdAt);
            statement.executeUpdate();

            try (var generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    check.setId(generatedKeys.getLong(1));
                }
            }
            check.setCreatedAt(createdAt);
        }
    }

    public List<UrlCheck> findByUrlId(Long urlId) throws SQLException {
        var sql = "SELECT id, url_id, status_code, h1, title, description, created_at "
                + "FROM url_checks WHERE url_id = ? ORDER BY created_at DESC, id DESC";
        try (var connection = dataSource.getConnection();
             var statement = connection.prepareStatement(sql)) {
            statement.setLong(1, urlId);
            try (var resultSet = statement.executeQuery()) {
                var checks = new ArrayList<UrlCheck>();
                while (resultSet.next()) {
                    checks.add(mapRow(resultSet));
                }
                return checks;
            }
        }
    }

    public Optional<UrlCheck> findLastByUrlId(Long urlId) throws SQLException {
        var sql = "SELECT id, url_id, status_code, h1, title, description, created_at "
                + "FROM url_checks WHERE url_id = ? ORDER BY created_at DESC, id DESC LIMIT 1";
        try (var connection = dataSource.getConnection();
             var statement = connection.prepareStatement(sql)) {
            statement.setLong(1, urlId);
            try (var resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapRow(resultSet));
                }
                return Optional.empty();
            }
        }
    }

    public Map<Long, UrlCheck> findLastChecks() throws SQLException {
        var sql = "SELECT c.id, c.url_id, c.status_code, c.h1, c.title, c.description, c.created_at "
                + "FROM url_checks c "
                + "JOIN (SELECT url_id, MAX(id) AS max_id FROM url_checks GROUP BY url_id) last "
                + "ON c.id = last.max_id";
        try (var connection = dataSource.getConnection();
             var statement = connection.prepareStatement(sql);
             var resultSet = statement.executeQuery()) {
            var checks = new HashMap<Long, UrlCheck>();
            while (resultSet.next()) {
                var check = mapRow(resultSet);
                checks.put(check.getUrlId(), check);
            }
            return checks;
        }
    }

    private static UrlCheck mapRow(ResultSet resultSet) throws SQLException {
        return new UrlCheck(
                resultSet.getLong("id"),
                resultSet.getInt("status_code"),
                resultSet.getString("title"),
                resultSet.getString("h1"),
                resultSet.getString("description"),
                resultSet.getLong("url_id"),
                resultSet.getTimestamp("created_at")
        );
    }
}
