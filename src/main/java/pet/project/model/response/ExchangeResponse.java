package pet.project.model.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import pet.project.model.Currency;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ExchangeResponse {
    private Currency baseCurrency;

    private Currency targetCurrency;

    private BigDecimal rate;

    private BigDecimal amount;

    private BigDecimal convertedAmount;
}
