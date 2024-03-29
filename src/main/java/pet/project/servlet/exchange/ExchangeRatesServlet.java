package pet.project.servlet.exchange;

import com.fasterxml.jackson.databind.ObjectMapper;
import pet.project.model.ExchangeRate;
import pet.project.model.response.ErrorResponse;
import pet.project.repository.CurrencyRepository;
import pet.project.repository.ExchangeRepository;
import pet.project.repository.JdbcCurrencyRepository;
import pet.project.repository.JdbcExchangeRepository;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.NoSuchElementException;

import static javax.servlet.http.HttpServletResponse.*;
import static pet.project.utils.Validation.isValidCurrencyCode;

@WebServlet(name = "ExchangeRatesServlet", urlPatterns = "/exchangeRates")
public class ExchangeRatesServlet extends HttpServlet {
    private final ExchangeRepository exchangeRepository = new JdbcExchangeRepository();
    private final CurrencyRepository currencyRepository = new JdbcCurrencyRepository();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final String INTEGRITY_CONSTRAINT_VIOLATION_CODE = "23505";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            List<ExchangeRate> exchangeRates = exchangeRepository.findAll();
            objectMapper.writeValue(resp.getWriter(), exchangeRates);

        } catch (SQLException e) {
            resp.setStatus(SC_INTERNAL_SERVER_ERROR);
            objectMapper.writeValue(resp.getWriter(), new ErrorResponse(
                    SC_INTERNAL_SERVER_ERROR,
                    "Something happened with the database, try again later!"
            ));
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String baseCurrencyCode = req.getParameter("baseCurrencyCode");
        String targetCurrencyCode = req.getParameter("targetCurrencyCode");
        String rateParam = req.getParameter("rate");

        if (baseCurrencyCode == null || baseCurrencyCode.isBlank()) {
            resp.setStatus(SC_BAD_REQUEST);
            objectMapper.writeValue(resp.getWriter(), new ErrorResponse(
                    SC_BAD_REQUEST,
                    "Missing parameter - baseCurrencyCode"
            ));
            return;
        }
        if (targetCurrencyCode == null || targetCurrencyCode.isBlank()) {
            resp.setStatus(SC_BAD_REQUEST);
            objectMapper.writeValue(resp.getWriter(), new ErrorResponse(
                    SC_BAD_REQUEST,
                    "Missing parameter - targetCurrencyCode"
            ));
            return;
        }
        if (rateParam == null || rateParam.isBlank()) {
            resp.setStatus(SC_BAD_REQUEST);
            objectMapper.writeValue(resp.getWriter(), new ErrorResponse(
                    SC_BAD_REQUEST,
                    "Missing parameter - rate"
            ));
            return;
        }

        if (!isValidCurrencyCode(baseCurrencyCode)) {
            resp.setStatus(SC_BAD_REQUEST);
            objectMapper.writeValue(resp.getWriter(), new ErrorResponse(
                    SC_BAD_REQUEST,
                    "Base currency code must be in ISO 4217 format"
            ));
            return;
        }
        if (!isValidCurrencyCode(targetCurrencyCode)) {
            resp.setStatus(SC_BAD_REQUEST);
            objectMapper.writeValue(resp.getWriter(), new ErrorResponse(
                    SC_BAD_REQUEST,
                    "Target currency code must be in ISO 4217 format"
            ));
            return;
        }

        BigDecimal rate;
        try {
            rate = BigDecimal.valueOf(Double.parseDouble(rateParam));
        } catch (NumberFormatException e) {
            resp.setStatus(SC_BAD_REQUEST);
            objectMapper.writeValue(resp.getWriter(), new ErrorResponse(
                    SC_BAD_REQUEST,
                    "Incorrect value of rate parameter"
            ));
            return;
        }

        try {
            ExchangeRate exchangeRate = new ExchangeRate(
                    currencyRepository.findByCode(baseCurrencyCode).orElseThrow(),
                    currencyRepository.findByCode(targetCurrencyCode).orElseThrow(),
                    rate
            );

            Long savedExchangeRateId = exchangeRepository.save(exchangeRate);
            exchangeRate.setId(savedExchangeRateId);

            objectMapper.writeValue(resp.getWriter(), exchangeRate);

        } catch (NoSuchElementException e) {
            resp.setStatus(SC_NOT_FOUND);
            objectMapper.writeValue(resp.getWriter(), new ErrorResponse(
                    SC_NOT_FOUND,
                    "One or both currencies for which you are trying to add an exchange rate does not exist in the database"
            ));
        } catch (SQLException e) {
            if (e.getSQLState().equals(INTEGRITY_CONSTRAINT_VIOLATION_CODE)) {
                resp.setStatus(SC_CONFLICT);
                objectMapper.writeValue(resp.getWriter(), new ErrorResponse(
                        SC_CONFLICT,
                        e.getMessage()
                ));
            }
            resp.setStatus(SC_INTERNAL_SERVER_ERROR);
            objectMapper.writeValue(resp.getWriter(), new ErrorResponse(
                    SC_INTERNAL_SERVER_ERROR,
                    "Something happened with the database, try again later!"
            ));
        }
    }
}
