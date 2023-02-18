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
import java.util.NoSuchElementException;
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
        String baseCurrencyCode = req.getParameter("baseCurrencyCode");
        String targetCurrencyCode = req.getParameter("targetCurrencyCode");
        String rate = req.getParameter("rate");

        if (baseCurrencyCode == null || targetCurrencyCode == null || rate == null ||
                baseCurrencyCode.isBlank() || targetCurrencyCode.isBlank() || rate.isBlank()) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing parameters: baseCurrencyCode, targetCurrencyCode or rate");
            return;
        }

        if (baseCurrencyCode.length() != 3 || targetCurrencyCode.length() != 3) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Currency codes must be in ISO 4217 format");
            return;
        }

        Optional<ExchangeRate> exchangeRateOptional = exchangeRepository.findByCodes(baseCurrencyCode, targetCurrencyCode);
        if (exchangeRateOptional.isPresent()) {
            resp.sendError(HttpServletResponse.SC_CONFLICT,
                    "The exchange rate you are trying to add already exists, id = " + exchangeRateOptional.get().getId());
            return;
        }

        try {
            ExchangeRate exchangeRateToAdd = new ExchangeRate(
                    currencyRepository.findByCode(baseCurrencyCode).get(),
                    currencyRepository.findByCode(targetCurrencyCode).get(),
                    Double.parseDouble(rate)
            );

            exchangeRepository.save(exchangeRateToAdd);
            ExchangeRate addedExchangeRate = exchangeRepository.findByCodes(baseCurrencyCode, targetCurrencyCode).get();

            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.writeValue(resp.getWriter(), addedExchangeRate);

        } catch (NumberFormatException e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Incorrect value of rate parameter");
        } catch (NoSuchElementException e) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND,
                    "One or both currencies for which you are trying to add an exchange rate does not exist in the database");
        }
    }
}
