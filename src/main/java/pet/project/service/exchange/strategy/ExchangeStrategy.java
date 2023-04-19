package pet.project.service.exchange.strategy;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Optional;

public interface ExchangeStrategy {
    Optional<BigDecimal> getExchangeRate(String baseCurrencyCode, String targetCurrencyCode) throws SQLException;
}
