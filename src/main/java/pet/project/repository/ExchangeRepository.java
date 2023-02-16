package pet.project.repository;

import pet.project.Utils;
import pet.project.model.ExchangeRate;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ExchangeRepository implements CrudRepository<ExchangeRate> {

    private final DataSource dataSource;
    private final CurrencyRepository currencyRepository = new CurrencyRepository(Utils.getConfiguredDataSource());

    public ExchangeRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Optional<ExchangeRate> findById(Long id) {
        final String query = "SELECT * FROM exchange_rates WHERE id = ?";

        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setLong(1, id);
            statement.execute();
            ResultSet resultSet = statement.getResultSet();

            if (resultSet.next()) {
                return Optional.of(getExchangeRate(resultSet));
            }
            return Optional.empty();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<ExchangeRate> findAll() {
        final String query = "SELECT * FROM exchange_rates";

        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement statement = connection.prepareStatement(query);
            statement.execute();
            ResultSet resultSet = statement.getResultSet();

            List<ExchangeRate> exchangeRatesList = new ArrayList<>();
            while (resultSet.next()) {
                exchangeRatesList.add(getExchangeRate(resultSet));
            }
            return exchangeRatesList;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void save(ExchangeRate entity) {
        final String query = "INSERT INTO exchange_rates (base_currency_id, target_currency_id, rate) VALUES (?, ?, ?)";

        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement statement = connection.prepareStatement(query);

            statement.setLong(1, entity.getBaseCurrencyId());
            statement.setLong(2, entity.getTargetCurrencyId());
            statement.setDouble(3, entity.getRate());

            statement.execute();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void update(ExchangeRate entity) {
        final String query = "UPDATE exchange_rates SET (base_currency_id, target_currency_id, rate) = (?, ?, ?)" +
                "WHERE id =" + entity.getId();

        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement statement = connection.prepareStatement(query);

            statement.setLong(1, entity.getBaseCurrencyId());
            statement.setLong(2, entity.getTargetCurrencyId());
            statement.setDouble(3, entity.getRate());

            statement.execute();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void delete(Long id) {
        final String query = "DELETE FROM exchange_rates WHERE id = ?";

        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setLong(1, id);
            statement.execute();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Optional<ExchangeRate> findByCodes(String baseCurrencyCode, String targetCurrencyCode) {
        final String query = "SELECT exchange_rates.id, exchange_rates.base_currency_id, " +
                "exchange_rates.target_currency_id, exchange_rates.rate " +
                "FROM exchange_rates " +
                "JOIN currencies " +
                "ON exchange_rates.base_currency_id = currencies.id " +
                "WHERE exchange_rates.base_currency_id = ? AND " +
                "exchange_rates.target_currency_id = ?";

        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement statement = connection.prepareStatement(query);
            Long baseCurrencyId = currencyRepository.getByCode(baseCurrencyCode).get().getId();
            Long targetCurrencyId = currencyRepository.getByCode(targetCurrencyCode).get().getId();

            statement.setLong(1, baseCurrencyId);
            statement.setLong(2, targetCurrencyId);
            statement.execute();

            ResultSet resultSet = statement.getResultSet();

            if (resultSet.next()) {
                return Optional.of(getExchangeRate(resultSet));
            }
            return Optional.empty();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private static ExchangeRate getExchangeRate(ResultSet resultSet) {
        try {
            return new ExchangeRate(
                    resultSet.getLong("id"),
                    resultSet.getLong("base_currency_id"),
                    resultSet.getLong("target_currency_id"),
                    resultSet.getDouble("rate")
            );
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
