package pet.project.service.exchange.context;

import lombok.Setter;
import pet.project.service.exchange.strategy.ExchangeStrategy;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Optional;

@Setter
public class ExchangeContext {
    private ExchangeStrategy strategy;

    public Optional<BigDecimal> getExchangeRate(String baseCurrencyCode, String targetCurrencyCode) throws SQLException {
        return strategy.getExchangeRate(baseCurrencyCode, targetCurrencyCode);
    }
}
