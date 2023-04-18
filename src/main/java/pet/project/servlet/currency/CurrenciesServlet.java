package pet.project.servlet.currency;

import com.fasterxml.jackson.databind.ObjectMapper;
import pet.project.model.Currency;
import pet.project.repository.CurrencyRepository;
import pet.project.repository.JdbcCurrencyRepository;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static pet.project.utils.Validation.isValidCurrencyCode;

@WebServlet(name = "CurrenciesServlet", urlPatterns = "/currencies")
public class CurrenciesServlet extends HttpServlet {
    private final CurrencyRepository currencyRepository = new JdbcCurrencyRepository();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        List<Currency> currencyList = currencyRepository.findAll();

        objectMapper.writeValue(resp.getWriter(), currencyList);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String name = req.getParameter("name");
        String code = req.getParameter("code");
        String symbol = req.getParameter("symbol");

        if (name == null || name.isBlank()) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing parameter - name");
            return;
        }
        if (code == null || code.isBlank()) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing parameter - code");
            return;
        }
        if (symbol == null || symbol.isBlank()) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing parameters - symbol");
            return;
        }

        if (!isValidCurrencyCode(code)) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Currency code must be in ISO 4217 format");
            return;
        }

        Optional<Currency> currencyOptional = currencyRepository.findByCode(code);

        if (currencyOptional.isPresent()) {
            resp.sendError(
                    HttpServletResponse.SC_CONFLICT,
                    "The currency you are trying to add already exists, id = " + currencyOptional.get().getId()
            );
            return;
        }

        Currency currency = new Currency(code, name, symbol);
        Long savedCurrencyId = currencyRepository.save(currency);

        currency.setId(savedCurrencyId);

        objectMapper.writeValue(resp.getWriter(), currency);
    }
}
