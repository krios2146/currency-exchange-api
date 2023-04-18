package pet.project.repository;

import pet.project.model.ExchangeRate;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface ExchangeRepository extends CrudRepository<ExchangeRate> {
    List<ExchangeRate> findByCodesWithUsdBase(String baseCurrencyCode, String targetCurrencyCode) throws SQLException;

    Optional<ExchangeRate> findByCodes(String baseCurrencyCode, String targetCurrencyCode) throws SQLException;
}
