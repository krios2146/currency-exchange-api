package pet.project.servlet.exchange;

import com.fasterxml.jackson.databind.ObjectMapper;
import pet.project.model.ExchangeRate;
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

@WebServlet(name = "ExchangeRateServlet", urlPatterns = "/exchangeRate/*")
public class ExchangeRateServlet extends HttpServlet {
    private final ExchangeRepository exchangeRepository = new JdbcExchangeRepository();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (req.getMethod().equalsIgnoreCase("PATCH")) {
            doPatch(req, resp);
        } else {
            super.service(req, resp);
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String url = req.getPathInfo().replaceAll("/", "");

        if (url.length() != 6) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST,
                    "Currency codes are either not provided or provided in an incorrect format");
            return;
        }

        String baseCurrencyCode = url.substring(0, 3).toUpperCase();
        String targetCurrencyCode = url.substring(3).toUpperCase();

        Optional<ExchangeRate> exchangeRateOptional = exchangeRepository.findByCodes(baseCurrencyCode, targetCurrencyCode);
        if (exchangeRateOptional.isEmpty()) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "There is no exchange rate for this currency pair");
            return;
        }

        objectMapper.writeValue(resp.getWriter(), exchangeRateOptional.get());
    }

    protected void doPatch(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String url = req.getPathInfo().replaceAll("/", "");
        // TODO: Proper validation
        if (url.length() != 6) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST,
                    "Currency codes are either not provided or provided in an incorrect format");
            return;
        }

        String parameter = req.getReader().readLine();
        if (parameter == null || !parameter.contains("rate")) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing required parameter rate");
            return;
        }

        String baseCurrencyCode = url.substring(0, 3).toUpperCase();
        String targetCurrencyCode = url.substring(3).toUpperCase();
        String paramRateValue = parameter.replace("rate=", "");

        Optional<ExchangeRate> exchangeRateToUpdateOptional = exchangeRepository.findByCodes(baseCurrencyCode, targetCurrencyCode);
        if (exchangeRateToUpdateOptional.isEmpty()) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "There is no exchange rate for this currency pair");
            return;
        }

        ExchangeRate exchangeRateToUpdate = exchangeRateToUpdateOptional.get();
        try {
            exchangeRateToUpdate.setRate(BigDecimal.valueOf(Double.parseDouble(paramRateValue)));
            exchangeRepository.update(exchangeRateToUpdate);
            doGet(req, resp);
        } catch (NumberFormatException e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Incorrect value of rate parameter");
        }
    }
}
