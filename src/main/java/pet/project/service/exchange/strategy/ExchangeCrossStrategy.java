package pet.project.service.exchange.strategy;

import pet.project.model.ExchangeRate;
import pet.project.repository.ExchangeRepository;
import pet.project.repository.JdbcExchangeRepository;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import static java.math.MathContext.DECIMAL64;

public class ExchangeCrossStrategy implements ExchangeStrategy {
    private final ExchangeRepository repository = new JdbcExchangeRepository();

    @Override
    public Optional<BigDecimal> getExchangeRate(String baseCurrencyCode, String targetCurrencyCode) throws SQLException {
        List<ExchangeRate> ratesWithUsdBase = repository.findByCodesWithUsdBase(baseCurrencyCode, targetCurrencyCode);

        if (ratesWithUsdBase.size() != 2) {
            return Optional.empty();
        }

        BigDecimal rateBaseToUsd = ratesWithUsdBase.get(0).getRate();
        BigDecimal rateTargetToUsd = ratesWithUsdBase.get(1).getRate();

        return Optional.of(rateTargetToUsd.divide(rateBaseToUsd, DECIMAL64));
    }
}
