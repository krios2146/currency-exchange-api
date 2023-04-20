package pet.project.repository;

import pet.project.model.Currency;
import pet.project.utils.ConfiguredDataSource;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcCurrencyRepository implements CurrencyRepository {
    private final DataSource dataSource = ConfiguredDataSource.getInstance();

    @Override
    public Optional<Currency> findById(Long id) throws SQLException {
        final String query = "SELECT * FROM currencies WHERE id = ?";

        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setLong(1, id);
            statement.execute();
            ResultSet resultSet = statement.getResultSet();

            if (!resultSet.next()) {
                return Optional.empty();
            }

            return Optional.of(getCurrency(resultSet));
        }
    }

    @Override
    public List<Currency> findAll() throws SQLException {
        final String query = "SELECT * FROM currencies";

        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement statement = connection.prepareStatement(query);
            statement.execute();
            ResultSet resultSet = statement.getResultSet();

            List<Currency> currencyList = new ArrayList<>();
            while (resultSet.next()) {
                currencyList.add(getCurrency(resultSet));
            }
            return currencyList;
        }
    }

    @Override
    public Long save(Currency entity) throws SQLException {
        final String query = "INSERT INTO currencies (code, full_name, sign) VALUES (?, ?, ?)";

        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);
            PreparedStatement statement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);

            statement.setString(1, entity.getCode());
            statement.setString(2, entity.getFullName());
            statement.setString(3, entity.getSign());

            statement.execute();

            ResultSet savedCurrency = statement.getGeneratedKeys();
            savedCurrency.next();
            long savedId = savedCurrency.getLong("id");

            connection.commit();

            return savedId;
        }
    }

    @Override
    public void update(Currency entity) throws SQLException {
        final String query = "UPDATE currencies SET (code, full_name, sign) = (?, ?, ?) WHERE id = ?";

        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement statement = connection.prepareStatement(query);

            statement.setString(1, entity.getCode());
            statement.setString(2, entity.getFullName());
            statement.setString(3, entity.getSign());
            statement.setLong(4, entity.getId());

            statement.execute();
        }
    }

    @Override
    public void delete(Long id) throws SQLException {
        final String query = "DELETE FROM currencies WHERE id = ?";

        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setLong(1, id);
            statement.execute();
        }
    }

    @Override
    public Optional<Currency> findByCode(String code) throws SQLException {
        final String query = "SELECT * FROM currencies WHERE code = ?";

        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, code);
            statement.execute();
            ResultSet resultSet = statement.getResultSet();

            if (!resultSet.next()) {
                return Optional.empty();
            }

            return Optional.of(getCurrency(resultSet));
        }
    }

    private static Currency getCurrency(ResultSet resultSet) throws SQLException {
        return new Currency(
                resultSet.getLong("id"),
                resultSet.getString("code"),
                resultSet.getString("full_name"),
                resultSet.getString("sign")
        );
    }
}
