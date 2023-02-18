package pet.project.servlet.exchange;

import com.fasterxml.jackson.databind.ObjectMapper;
import pet.project.model.ExchangeRate;
import pet.project.model.ExchangeResponse;
import pet.project.repository.ExchangeRepository;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;

@WebServlet(name = "ExchangeServlet", urlPatterns = "/exchange")
public class ExchangeServlet extends HttpServlet {
    private final ExchangeRepository exchangeRepository = new ExchangeRepository();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String baseCurrencyCode = req.getParameter("from");
        String targetCurrencyCode = req.getParameter("to");
        String amount = req.getParameter("amount");

        if (baseCurrencyCode == null || targetCurrencyCode == null || amount == null ||
                baseCurrencyCode.isBlank() || targetCurrencyCode.isBlank() || amount.isBlank()) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing parameters: from, to or amount");
            return;
        }

        if (baseCurrencyCode.length() != 3 || targetCurrencyCode.length() != 3) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Currency codes must be in ISO 4217 format");
            return;
        }

        // TODO: Check Optional
        Optional<ExchangeRate> exchangeRate = exchangeRepository.findByCodes(baseCurrencyCode, targetCurrencyCode);
        double currencyExchangeRate = exchangeRate.get().getRate();
        double convertedAmount = Double.parseDouble(amount) * currencyExchangeRate;

        ExchangeResponse response = new ExchangeResponse(
                exchangeRate.get().getBaseCurrency(),
                exchangeRate.get().getTargetCurrency(),
                currencyExchangeRate,
                Double.parseDouble(amount),
                convertedAmount
        );

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.writeValue(resp.getWriter(), response);
    }
}
