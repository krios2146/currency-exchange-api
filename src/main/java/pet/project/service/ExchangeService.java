package pet.project.service;

import pet.project.model.ExchangeRate;
import pet.project.model.response.ExchangeResponse;
import pet.project.repository.ExchangeRepository;
import pet.project.repository.JdbcExchangeRepository;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static java.math.MathContext.DECIMAL64;

public class ExchangeService {
    private final ExchangeRepository exchangeRepository = new JdbcExchangeRepository();

    /**
     * Converts the given amount from one currency to another using the exchange rate
     * retrieved from a data source.
     *
     * @param baseCurrencyCode   The currency code of the base currency to convert from.
     * @param targetCurrencyCode The currency code of the target currency to convert to.
     * @param amount             The amount to be converted.
     * @return An {@link ExchangeResponse} object containing the conversion details, such as the
     * base currency, target currency, exchange rate, original amount, and converted amount.
     * @throws SQLException           If there is an error accessing the data source.
     * @throws NoSuchElementException If no exchange rate is found for the given currency codes.
     */
    // probably mixing logic and view
    public ExchangeResponse convertCurrency(String baseCurrencyCode, String targetCurrencyCode, BigDecimal amount) throws SQLException, NoSuchElementException {
        ExchangeRate exchangeRate = getExchangeRate(baseCurrencyCode, targetCurrencyCode).orElseThrow();

        BigDecimal convertedAmount = amount.multiply(exchangeRate.getRate()).setScale(2, HALF_EVEN);

        return new ExchangeResponse(
                exchangeRate.getBaseCurrency(),
                exchangeRate.getTargetCurrency(),
                exchangeRate.getRate(),
                amount,
                convertedAmount
        );
    }

    /**
     * Retrieves the exchange rate between two currencies.
     * <p>
     * The method first tries to fetch the direct exchange rate, if not found,
     * then it tries reverse exchange rate, and finally cross exchange rate.
     *
     * @param baseCurrencyCode   The currency code of the base currency.
     * @param targetCurrencyCode The currency code of the target currency.
     * @return An Optional of  {@link ExchangeRate} object containing the exchange rate if found, or empty if not found.
     * @throws SQLException If there is an error accessing the data source.
     */
    private Optional<ExchangeRate> getExchangeRate(String baseCurrencyCode, String targetCurrencyCode) throws SQLException {
        Optional<ExchangeRate> exchangeRate = getFromDirectExchangeRate(baseCurrencyCode, targetCurrencyCode);

        if (exchangeRate.isEmpty()) {
            exchangeRate = getFromReverseExchangeRate(baseCurrencyCode, targetCurrencyCode);
        }

        if (exchangeRate.isEmpty()) {
            exchangeRate = getFromCrossExchangeRate(baseCurrencyCode, targetCurrencyCode);
        }

        return exchangeRate;
    }

    /**
     * Retrieves the exchange rate between two currencies directly from the exchange repository,
     * based on the given base currency and target currency codes.
     *
     * @param baseCurrencyCode   The currency code of the base currency.
     * @param targetCurrencyCode The currency code of the target currency.
     * @return An Optional of {@link ExchangeRate} object containing the exchange rate if found, or empty if not found.
     * @throws SQLException If there is an error accessing the data source.
     */
    private Optional<ExchangeRate> getFromDirectExchangeRate(String baseCurrencyCode, String targetCurrencyCode) throws SQLException {
        return exchangeRepository.findByCodes(baseCurrencyCode, targetCurrencyCode);
    }

    /**
     * Retrieves the reversed exchange rate between two currencies from the exchange repository,
     * based on the given base currency and target currency codes, and returns the corresponding
     * direct exchange rate.
     *
     * @param baseCurrencyCode   The currency code of the base currency.
     * @param targetCurrencyCode The currency code of the target currency.
     * @return An Optional of {@link ExchangeRate} object containing the reverse exchange rate if found, or empty if not found.
     * @throws SQLException If there is an error accessing the data source.
     */
    private Optional<ExchangeRate> getFromReverseExchangeRate(String baseCurrencyCode, String targetCurrencyCode) throws SQLException {
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

    /**
     * Retrieves the cross exchange rate between two currencies from the exchange repository,
     * based on the given base currency and target currency codes. This method searches for
     * the USD to base currency and USD to target currency rates, and calculates the corresponding
     * base currency to target currency exchange rate.
     *
     * @param baseCurrencyCode   The currency code of the base currency.
     * @param targetCurrencyCode The currency code of the target currency.
     * @return An Optional of {@link ExchangeRate} object containing the cross exchange rate if found, or empty if not found.
     * @throws SQLException If there is an error accessing the data source.
     */
    private Optional<ExchangeRate> getFromCrossExchangeRate(String baseCurrencyCode, String targetCurrencyCode) throws SQLException {
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
