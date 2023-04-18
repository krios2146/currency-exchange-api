package pet.project.servlet.currency;

import com.fasterxml.jackson.databind.ObjectMapper;
import pet.project.model.Currency;
import pet.project.model.response.ErrorResponse;
import pet.project.repository.CurrencyRepository;
import pet.project.repository.JdbcCurrencyRepository;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import static pet.project.utils.Validation.isValidCurrencyCode;

@WebServlet(name = "CurrenciesServlet", urlPatterns = "/currencies")
public class CurrenciesServlet extends HttpServlet {
    private final CurrencyRepository currencyRepository = new JdbcCurrencyRepository();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final String INTEGRITY_CONSTRAINT_VIOLATION_CODE = "23505";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            List<Currency> currencyList = currencyRepository.findAll();
            objectMapper.writeValue(resp.getWriter(), currencyList);

        } catch (SQLException e) {
            objectMapper.writeValue(resp.getWriter(), new ErrorResponse(
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Something happened with the database, try again later!"
            ));
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String name = req.getParameter("name");
        String code = req.getParameter("code");
        String symbol = req.getParameter("symbol");

        if (name == null || name.isBlank()) {
            objectMapper.writeValue(resp.getWriter(), new ErrorResponse(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Missing parameter - name"
            ));
            return;
        }
        if (code == null || code.isBlank()) {
            objectMapper.writeValue(resp.getWriter(), new ErrorResponse(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Missing parameter - code"
            ));
            return;
        }
        if (symbol == null || symbol.isBlank()) {
            objectMapper.writeValue(resp.getWriter(), new ErrorResponse(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Missing parameters - symbol"
            ));
            return;
        }

        if (!isValidCurrencyCode(code)) {
            objectMapper.writeValue(resp.getWriter(), new ErrorResponse(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Currency code must be in ISO 4217 format"
            ));
            return;
        }

        try {
            Currency currency = new Currency(code, name, symbol);
            Long savedCurrencyId = currencyRepository.save(currency);
            currency.setId(savedCurrencyId);

            objectMapper.writeValue(resp.getWriter(), currency);

        } catch (SQLException e) {
            if (e.getSQLState().equals(INTEGRITY_CONSTRAINT_VIOLATION_CODE)) {
                objectMapper.writeValue(resp.getWriter(), new ErrorResponse(
                        HttpServletResponse.SC_CONFLICT,
                        e.getMessage()
                ));
            }
            objectMapper.writeValue(resp.getWriter(), new ErrorResponse(
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Something happened with the database, try again later!"
            ));
        }
    }
}
