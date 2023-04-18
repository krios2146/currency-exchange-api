package pet.project.repository;

import pet.project.model.Currency;
import pet.project.model.ExchangeRate;
import pet.project.utils.ConfiguredDataSource;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcExchangeRepository implements ExchangeRepository {
    private final DataSource dataSource = ConfiguredDataSource.getInstance();

    @Override
    public Optional<ExchangeRate> findById(Long id) {
        // @formatter:off
        final String query =
            """
                SELECT
                    er.id AS id,
                    bc.id AS base_id,
                    bc.code AS base_code,
                    bc.full_name AS base_name,
                    bc.sign AS base_sign,
                    tc.id AS target_id,
                    tc.code AS target_code,
                    tc.full_name AS target_name,
                    tc.sign AS target_sign,
                    er.rate AS rate
                FROM exchange_rates er
                JOIN currency bc ON er.base_currency_id = bc.id
                JOIN currency tc ON er.target_currency_id = tc.id
                WHERE er.id = ?
            """;

        // @formatter:on
        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setLong(1, id);
            statement.execute();
            ResultSet resultSet = statement.getResultSet();

            if (!resultSet.next()) {
                return Optional.empty();
            }

            return Optional.of(getExchangeRate(resultSet));

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<ExchangeRate> findAll() {
        // @formatter:off
        final String query =
            """
                SELECT
                    er.id AS id,
                    bc.id AS base_id,
                    bc.code AS base_code,
                    bc.full_name AS base_name,
                    bc.sign AS base_sign,
                    tc.id AS target_id,
                    tc.code AS target_code,
                    tc.full_name AS target_name,
                    tc.sign AS target_sign,
                    er.rate AS rate
                FROM exchange_rates er
                JOIN currency bc ON er.base_currency_id = bc.id
                JOIN currency tc ON er.target_currency_id = tc.id
            """;

        // @formatter:on
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
    public Long save(ExchangeRate entity) {
        final String query = "INSERT INTO exchange_rates (base_currency_id, target_currency_id, rate) VALUES (?, ?, ?)";

        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);
            PreparedStatement statement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);

            statement.setLong(1, entity.getBaseCurrency().getId());
            statement.setLong(2, entity.getTargetCurrency().getId());
            statement.setBigDecimal(3, entity.getRate());

            statement.execute();

            ResultSet savedExchangeRate = statement.getGeneratedKeys();
            savedExchangeRate.next();

            return savedExchangeRate.getLong("id");

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void update(ExchangeRate entity) {
        // @formatter:off
        final String query =
                "UPDATE exchange_rates " +
                "SET (base_currency_id, target_currency_id, rate) = (?, ?, ?)" +
                "WHERE id = ?";

        // @formatter:on
        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement statement = connection.prepareStatement(query);

            statement.setLong(1, entity.getBaseCurrency().getId());
            statement.setLong(2, entity.getTargetCurrency().getId());
            statement.setBigDecimal(3, entity.getRate());
            statement.setLong(4, entity.getId());

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

    @Override
    public Optional<ExchangeRate> findByCodes(String baseCurrencyCode, String targetCurrencyCode) {
        // @formatter:off
        final String query =
            """
                SELECT
                    er.id AS id,
                    bc.id AS base_id,
                    bc.code AS base_code,
                    bc.full_name AS base_name,
                    bc.sign AS base_sign,
                    tc.id AS target_id,
                    tc.code AS target_code,
                    tc.full_name AS target_name,
                    tc.sign AS target_sign,
                    er.rate AS rate
                FROM exchange_rates er
                JOIN currency bc ON er.base_currency_id = bc.id
                JOIN currency tc ON er.target_currency_id = tc.id
                WHERE (
                    base_currency_id = (SELECT c.id FROM currency c WHERE c.code = ?) AND
                    target_currency_id = (SELECT c2.id FROM currency c2 WHERE c2.code = ?)
                )
            """;

        // @formatter:on
        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement statement = connection.prepareStatement(query);

            statement.setString(1, baseCurrencyCode);
            statement.setString(2, targetCurrencyCode);
            statement.execute();

            ResultSet resultSet = statement.getResultSet();

            if (!resultSet.next()) {
                return Optional.empty();
            }

            return Optional.of(getExchangeRate(resultSet));

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // may be naming is not good enough
    @Override
    public List<ExchangeRate> findByCodesWithUsdBase(String baseCurrencyCode, String targetCurrencyCode) {
        // @formatter:off
        final String query =
                """
                    SELECT
                        er.id AS id,
                        bc.id AS base_id,
                        bc.code AS base_code,
                        bc.full_name AS base_name,
                        bc.sign AS base_sign,
                        tc.id AS target_id,
                        tc.code AS target_code,
                        tc.full_name AS target_name,
                        tc.sign AS target_sign,
                        er.rate AS rate
                    FROM exchange_rates er
                    JOIN currency bc ON er.base_currency_id = bc.id
                    JOIN currency tc ON er.target_currency_id = tc.id
                    WHERE (
                        base_currency_id = (SELECT c.id FROM currency c WHERE c.code = 'USD') AND
                        target_currency_id = (SELECT c2.id FROM currency c2 WHERE c2.code = ?) OR
                        target_currency_id = (SELECT c3.id FROM currency c3 WHERE c3.code = ?)
                    )
                """;

        // @formatter:on
        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement statement = connection.prepareStatement(query);

            statement.setString(1, baseCurrencyCode);
            statement.setString(2, targetCurrencyCode);
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

    private static ExchangeRate getExchangeRate(ResultSet resultSet) {
        try {
            return new ExchangeRate(
                    resultSet.getLong("id"),
                    new Currency(
                            resultSet.getLong("base_id"),
                            resultSet.getString("base_id"),
                            resultSet.getString("base_id"),
                            resultSet.getString("base_id")
                    ),
                    new Currency(
                            resultSet.getLong("target_id"),
                            resultSet.getString("target_id"),
                            resultSet.getString("target_id"),
                            resultSet.getString("target_id")
                    ),
                    resultSet.getBigDecimal("rate")
            );
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
