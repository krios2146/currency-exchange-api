package pet.project.servlet.exchange;

import com.fasterxml.jackson.databind.ObjectMapper;
import pet.project.model.ExchangeRate;
import pet.project.repository.CurrencyRepository;
import pet.project.repository.ExchangeRepository;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

@WebServlet(name = "ExchangeRatesServlet", urlPatterns = "/exchangeRates")
public class ExchangeRatesServlet extends HttpServlet {
    private final ExchangeRepository exchangeRepository = new ExchangeRepository();
    private final CurrencyRepository currencyRepository = new CurrencyRepository();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        List<ExchangeRate> exchangeRates = exchangeRepository.findAll();

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.writeValue(resp.getWriter(), exchangeRates);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String baseCurrencyCode = req.getParameter("from");
        String targetCurrencyCode = req.getParameter("to");
        String rate = req.getParameter("rate");

        if (baseCurrencyCode == null || targetCurrencyCode == null || rate == null ||
                baseCurrencyCode.isBlank() || targetCurrencyCode.isBlank() || rate.isBlank()) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing parameters: from, to or rate");
            return;
        }

        if (baseCurrencyCode.length() != 3 || targetCurrencyCode.length() != 3) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Currency codes must be in ISO 4217 format");
            return;
        }

        // TODO: Check if this rate is already present
        ExchangeRate exchangeRate = new ExchangeRate(
                currencyRepository.findByCode(baseCurrencyCode).get(),
                currencyRepository.findByCode(targetCurrencyCode).get(),
                Double.parseDouble(rate)
        );

        exchangeRepository.save(exchangeRate);
        Optional<ExchangeRate> exchangeRateOptional = exchangeRepository.findByCodes(baseCurrencyCode, targetCurrencyCode);

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.writeValue(resp.getWriter(), exchangeRateOptional.get());
    }
}
