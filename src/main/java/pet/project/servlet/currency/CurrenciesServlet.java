package pet.project.servlet.currency;

import com.fasterxml.jackson.databind.ObjectMapper;
import pet.project.model.Currency;
import pet.project.repository.CurrencyRepository;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@WebServlet(name = "CurrenciesServlet", urlPatterns = "/currencies")
public class CurrenciesServlet extends HttpServlet {
    private final CurrencyRepository currencyRepository = new CurrencyRepository();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        List<Currency> currencyList = currencyRepository.findAll();

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.writeValue(resp.getWriter(), currencyList);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String name = req.getParameter("name");
        String code = req.getParameter("code");
        String symbol = req.getParameter("symbol");

        if (name == null || code == null || symbol == null ||
                name.isBlank() || code.isBlank() || symbol.isBlank()) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing parameters: name, code or symbol");
            return;
        }

        if (code.length() != 3) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Currency code must be in ISO 4217 format");
            return;
        }

        Currency currency = new Currency(code, name, symbol);
        // TODO: Check if already exists
        currencyRepository.save(currency);
    }
}
