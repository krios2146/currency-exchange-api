package pet.project.service.exchange.context;

import lombok.Setter;
import pet.project.service.exchange.strategy.ExchangeStrategy;

import java.math.BigDecimal;
import java.sql.SQLException;

@Setter
public class ExchangeContext {
    private ExchangeStrategy strategy;

    public BigDecimal getExchangeRate(String baseCurrencyCode, String targetCurrencyCode) throws SQLException {
        return strategy.getExchangeRate(baseCurrencyCode, targetCurrencyCode);
    }
}
