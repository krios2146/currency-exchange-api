package pet.project.servlet.exchange;

import com.fasterxml.jackson.databind.ObjectMapper;
import pet.project.model.ExchangeRate;
import pet.project.model.ExchangeResponse;
import pet.project.repository.ExchangeRepository;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;

@WebServlet(name = "ExchangeServlet", urlPatterns = "/exchange")
public class ExchangeServlet extends HttpServlet {
    private final ExchangeRepository exchangeRepository = new ExchangeRepository();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // TODO: Validate parameters
        String from = req.getParameter("from");
        String to = req.getParameter("to");
        double amount = Double.parseDouble(req.getParameter("amount"));

        // TODO: Check Optional
        Optional<ExchangeRate> exchangeRate = exchangeRepository.findByCodes(from, to);
        double currencyExchangeRate = exchangeRate.get().getRate();
        double convertedAmount = amount * currencyExchangeRate;

        ExchangeResponse response = new ExchangeResponse(
                exchangeRate.get().getBaseCurrency(),
                exchangeRate.get().getTargetCurrency(),
                currencyExchangeRate,
                amount,
                convertedAmount
        );

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.writeValue(resp.getWriter(), response);
    }
}
