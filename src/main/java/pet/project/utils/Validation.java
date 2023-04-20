package pet.project.utils;

import java.util.Currency;
import java.util.Set;
import java.util.stream.Collectors;

public class Validation {
    private static Set<String> currencyCodes;

    /**
     * Checks if the given currency code is valid based on the available currencies provided by the
     * {@link Currency#getAvailableCurrencies()} method. The list of valid currency codes is cached for performance
     * optimization, and only retrieved from the Currency class if not already available.
     *
     * @param code The currency code to be checked for validity.
     * @return {@code true} if the currency code is valid, {@code false} otherwise.
     */
    public static boolean isValidCurrencyCode(String code) {
        if (currencyCodes == null) {
            Set<Currency> currencies = Currency.getAvailableCurrencies();
            currencyCodes = currencies.stream()
                    .map(Currency::getCurrencyCode)
                    .collect(Collectors.toSet());
        }

        return currencyCodes.contains(code);
    }
}
