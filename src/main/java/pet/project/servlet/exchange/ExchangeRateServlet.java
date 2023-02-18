package pet.project.servlet.exchange;

import com.fasterxml.jackson.databind.ObjectMapper;
import pet.project.model.ExchangeRate;
import pet.project.repository.ExchangeRepository;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;

@WebServlet(name = "ExchangeRateServlet", urlPatterns = "/exchangeRate/*")
public class ExchangeRateServlet extends HttpServlet {
    private final ExchangeRepository exchangeRepository = new ExchangeRepository();

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

        // TODO: Check Optional
        Optional<ExchangeRate> exchangeRate = exchangeRepository.findByCodes(baseCurrencyCode, targetCurrencyCode);

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.writeValue(resp.getWriter(), exchangeRate.get());
    }

    protected void doPatch(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String url = req.getPathInfo().replaceAll("/", "");
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
        String paramValue = parameter.replace("rate=", "");

        try {
            Double.parseDouble(paramValue);
        } catch (NumberFormatException e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Incorrect value of rate parameter");
            return;
        }

        // TODO: Check Optional
        Optional<ExchangeRate> exchangeRateOptional = exchangeRepository.findByCodes(baseCurrencyCode, targetCurrencyCode);
        exchangeRateOptional.get().setRate(Double.parseDouble(paramValue));
        exchangeRepository.update(exchangeRateOptional.get());

        doGet(req, resp);
    }

}
