package pet.project.servlet.currency;

import com.fasterxml.jackson.databind.ObjectMapper;
import pet.project.model.Currency;
import pet.project.repository.JdbcCurrencyRepository;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;

import static pet.project.utils.Validation.isValidCurrencyCode;

@WebServlet(name = "CurrencyServlet", urlPatterns = "/currency/*")
public class CurrencyServlet extends HttpServlet {
    private final JdbcCurrencyRepository jdbcCurrencyRepository = new JdbcCurrencyRepository();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String code = req.getPathInfo().replaceAll("/", "");

        if (!isValidCurrencyCode(code)) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Currency code must be in ISO 4217 format");
            return;
        }

        String currencyCode = code.toUpperCase();

        Optional<Currency> currencyOptional = jdbcCurrencyRepository.findByCode(currencyCode);
        if (currencyOptional.isEmpty()) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "There is no such currency in the database");
            return;
        }

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.writeValue(resp.getWriter(), currencyOptional.get());
    }
}
