package pet.project.repository;

import pet.project.model.Currency;
import pet.project.utils.ConfiguredDataSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CurrencyRepository implements CrudRepository<Currency> {

    private final DataSource dataSource = ConfiguredDataSource.getInstance();

    @Override
    public Optional<Currency> findById(Long id) {
        final String query = "SELECT * FROM currencies WHERE id = ?";

        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setLong(1, id);
            statement.execute();
            ResultSet resultSet = statement.getResultSet();

            if (resultSet.next()) {
                return Optional.of(getCurrency(resultSet));
            }
            return Optional.empty();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Currency> findAll() {
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

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void save(Currency entity) {
        final String query = "INSERT INTO currencies (code, full_name, symbol) VALUES (?, ?, ?)";

        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement statement = connection.prepareStatement(query);

            statement.setString(1, entity.getCode());
            statement.setString(2, entity.getFullName());
            statement.setString(3, entity.getSign());

            statement.execute();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void update(Currency entity) {
        final String query = "UPDATE currencies SET (code, full_name, symbol) = (?, ?, ?)" +
                "WHERE id =" + entity.getId();

        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement statement = connection.prepareStatement(query);

            statement.setString(1, entity.getCode());
            statement.setString(2, entity.getFullName());
            statement.setString(3, entity.getSign());

            statement.execute();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void delete(Long id) {
        final String query = "DELETE FROM currencies WHERE id = ?";

        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setLong(1, id);
            statement.execute();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Optional<Currency> findByCode(String code) {
        final String query = "SELECT * FROM currencies WHERE code = ?";

        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, code);
            statement.execute();
            ResultSet resultSet = statement.getResultSet();

            if (resultSet.next()) {
                return Optional.of(getCurrency(resultSet));
            }
            return Optional.empty();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private static Currency getCurrency(ResultSet resultSet) {
        try {
            return new Currency(
                    resultSet.getLong("id"),
                    resultSet.getString("code"),
                    resultSet.getString("full_name"),
                    resultSet.getString("symbol")
            );
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
