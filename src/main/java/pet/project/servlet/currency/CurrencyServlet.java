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
import java.util.Optional;

@WebServlet(name = "CurrencyServlet", urlPatterns = "/currency/*")
public class CurrencyServlet extends HttpServlet {
    private final CurrencyRepository currencyRepository = new CurrencyRepository();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // TODO: Validation of path
        String code = req.getPathInfo().replaceAll("/", "").toUpperCase();
        // TODO: Check Optional
        Optional<Currency> currency = currencyRepository.getByCode(code);

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.writeValue(resp.getWriter(), currency.get());
    }
}
