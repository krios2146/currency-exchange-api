package pet.project.service;

import pet.project.model.ExchangeRate;
import pet.project.model.response.ExchangeResponse;
import pet.project.repository.ExchangeRepository;
import pet.project.repository.JdbcExchangeRepository;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import static java.math.MathContext.DECIMAL64;

public class ExchangeService {
    private final ExchangeRepository exchangeRepository = new JdbcExchangeRepository();

    // probably mixing logic and view
    public ExchangeResponse convertCurrency(String baseCurrencyCode, String targetCurrencyCode, BigDecimal amount) throws SQLException {
        ExchangeRate exchangeRate = getExchangeRate(baseCurrencyCode, targetCurrencyCode).orElseThrow();

        BigDecimal convertedAmount = amount.multiply(exchangeRate.getRate());

        return new ExchangeResponse(
                exchangeRate.getBaseCurrency(),
                exchangeRate.getTargetCurrency(),
                exchangeRate.getRate(),
                amount,
                convertedAmount
        );
    }
    
    private Optional<ExchangeRate> getExchangeRate(String baseCurrencyCode, String targetCurrencyCode) throws SQLException {
        Optional<ExchangeRate> exchangeRateOptional = getDirectExchangeRate(baseCurrencyCode, targetCurrencyCode);

        if (exchangeRateOptional.isEmpty()) {
            exchangeRateOptional = getReverseExchangeRate(baseCurrencyCode, targetCurrencyCode);
        }

        if (exchangeRateOptional.isEmpty()) {
            exchangeRateOptional = getCrossExchangeRate(baseCurrencyCode, targetCurrencyCode);
        }

        return exchangeRateOptional;
    }

    private Optional<ExchangeRate> getDirectExchangeRate(String baseCurrencyCode, String targetCurrencyCode) throws SQLException {
        return exchangeRepository.findByCodes(baseCurrencyCode, targetCurrencyCode);
    }

    private Optional<ExchangeRate> getReverseExchangeRate(String baseCurrencyCode, String targetCurrencyCode) throws SQLException {
        Optional<ExchangeRate> exchangeRateOptional = exchangeRepository.findByCodes(targetCurrencyCode, baseCurrencyCode);

        if (exchangeRateOptional.isEmpty()) {
            return Optional.empty();
        }

        ExchangeRate reversedExchangeRate = exchangeRateOptional.get();

        ExchangeRate directExchangeRate = new ExchangeRate(
                reversedExchangeRate.getTargetCurrency(),
                reversedExchangeRate.getBaseCurrency(),
                BigDecimal.ONE.divide(reversedExchangeRate.getRate(), DECIMAL64)
        );

        return Optional.of(directExchangeRate);
    }

    private Optional<ExchangeRate> getCrossExchangeRate(String baseCurrencyCode, String targetCurrencyCode) throws SQLException {
        List<ExchangeRate> ratesWithUsdBase = exchangeRepository.findByCodesWithUsdBase(baseCurrencyCode, targetCurrencyCode);

        if (ratesWithUsdBase.size() != 2) {
            return Optional.empty();
        }

        ExchangeRate usdToBaseExchangeRate = ratesWithUsdBase.get(0);
        ExchangeRate usdToTargetExchangeRate = ratesWithUsdBase.get(1);

        BigDecimal usdToBaseRate = usdToBaseExchangeRate.getRate();
        BigDecimal usdToTargetRate = usdToTargetExchangeRate.getRate();

        BigDecimal baseToTargetRate = usdToTargetRate.divide(usdToBaseRate, DECIMAL64);

        ExchangeRate exchangeRate = new ExchangeRate(
                usdToBaseExchangeRate.getTargetCurrency(),
                usdToTargetExchangeRate.getTargetCurrency(),
                baseToTargetRate
        );

        return Optional.of(exchangeRate);
    }
}
