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
        // TODO: Validation of request
        String codes = req.getPathInfo().replaceAll("/", "").toUpperCase();
        String baseCurrencyCode = codes.substring(0, 3);
        String targetCurrencyCode = codes.substring(3);

        // TODO: Check Optional
        Optional<ExchangeRate> exchangeRate = exchangeRepository.findByCodes(baseCurrencyCode, targetCurrencyCode);

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.writeValue(resp.getWriter(), exchangeRate.get());
    }

    protected void doPatch(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // TODO: Validation of request
        String newExchangeRate = req.getReader().readLine().replace("rate=", "");
        String codes = req.getPathInfo().replaceAll("/", "").toUpperCase();
        String baseCurrencyCode = codes.substring(0, 3);
        String targetCurrencyCode = codes.substring(3);

        // TODO: Check Optional
        Optional<ExchangeRate> exchangeRateOptional = exchangeRepository.findByCodes(baseCurrencyCode, targetCurrencyCode);
        exchangeRateOptional.get().setRate(Double.parseDouble(newExchangeRate));
        exchangeRepository.update(exchangeRateOptional.get());

        doGet(req, resp);
    }

}
