package pet.project.servlet.exchange;

import com.fasterxml.jackson.databind.ObjectMapper;
import pet.project.model.ExchangeRate;
import pet.project.model.response.ExchangeResponse;
import pet.project.repository.ExchangeRepository;
import pet.project.repository.JdbcExchangeRepository;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Optional;

@WebServlet(name = "ExchangeServlet", urlPatterns = "/exchange")
public class ExchangeServlet extends HttpServlet {
    private final ExchangeRepository exchangeRepository = new JdbcExchangeRepository();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String baseCurrencyCode = req.getParameter("from");
        String targetCurrencyCode = req.getParameter("to");
        String amountToConvertParam = req.getParameter("amount");

        if (baseCurrencyCode == null || targetCurrencyCode == null || amountToConvertParam == null ||
                baseCurrencyCode.isBlank() || targetCurrencyCode.isBlank() || amountToConvertParam.isBlank()) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing parameters: from, to or amount");
            return;
        }

        if (baseCurrencyCode.length() != 3 || targetCurrencyCode.length() != 3) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Currency codes must be in ISO 4217 format");
            return;
        }

        Optional<ExchangeRate> exchangeRateOptional = exchangeRepository.findByCodes(baseCurrencyCode, targetCurrencyCode);
        if (exchangeRateOptional.isEmpty()) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Exchange rate for this pair of currency does not exist");
            return;
        }

        ExchangeRate exchangeRate = exchangeRateOptional.get();

        BigDecimal currencyExchangeRate = exchangeRate.getRate();
        BigDecimal amountToConvert;
        try {
            amountToConvert = BigDecimal.valueOf(Double.parseDouble(amountToConvertParam));
        } catch (NumberFormatException e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Incorrect value of amount parameter");
            return;
        }

        BigDecimal convertedAmount = amountToConvert.multiply(currencyExchangeRate);

        ExchangeResponse response = new ExchangeResponse(
                exchangeRate.getBaseCurrency(),
                exchangeRate.getTargetCurrency(),
                currencyExchangeRate,
                amountToConvert,
                convertedAmount
        );

        objectMapper.writeValue(resp.getWriter(), response);
    }
}
