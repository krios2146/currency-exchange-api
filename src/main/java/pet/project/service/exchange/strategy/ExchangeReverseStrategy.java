package pet.project.service.exchange.strategy;

import pet.project.model.ExchangeRate;
import pet.project.repository.ExchangeRepository;
import pet.project.repository.JdbcExchangeRepository;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Optional;

import static java.math.MathContext.DECIMAL64;

public class ExchangeReverseStrategy implements ExchangeStrategy {
    private final ExchangeRepository repository = new JdbcExchangeRepository();

    @Override
    public Optional<BigDecimal> getExchangeRate(String baseCurrencyCode, String targetCurrencyCode) throws SQLException {
        Optional<ExchangeRate> exchangeRateOptional = repository.findByCodes(targetCurrencyCode, baseCurrencyCode);

        return exchangeRateOptional.map(exchangeRate -> BigDecimal.ONE.divide(exchangeRate.getRate(), DECIMAL64));
    }
}
