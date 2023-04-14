package pet.project.servlet.currency;

import com.fasterxml.jackson.databind.ObjectMapper;
import pet.project.model.Currency;
import pet.project.repository.CurrencyRepository;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;

@WebServlet(name = "CurrencyServlet", urlPatterns = "/currency/*")
public class CurrencyServlet extends HttpServlet {
    private final CurrencyRepository currencyRepository = new CurrencyRepository();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String code = req.getPathInfo().replaceAll("/", "");

        if (code.length() != 3) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST,
                    "Currency code are either not provided or provided in an incorrect format");
            return;
        }

        String currencyCode = code.toUpperCase();

        Optional<Currency> currencyOptional = currencyRepository.findByCode(currencyCode);
        if (currencyOptional.isEmpty()) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "There is no such currency in the database");
            return;
        }

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.writeValue(resp.getWriter(), currencyOptional.get());
    }
}
