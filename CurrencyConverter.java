package currency.rest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.channels.FileLock;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

@RestController
public class CurrencyConverter {
    private HashMap<String, HashMap<String, Double>> conversionTable;

    public CurrencyConverter() {
        List<List<String>> lines = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader("exchange.csv"))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");
                lines.add(Arrays.asList(values));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        conversionTable = new HashMap<>();
        List<String> currencyNames = lines.get(0);

        for (int i = 1; i < lines.size(); i++) {
            List<String> rates = lines.get(i);
            String sourceCurrency = rates.get(0);
            HashMap<String, Double> targetCurrencyToDoubleMap = new HashMap<>();

            for (int j = 1; j < currencyNames.size(); j++) {
                targetCurrencyToDoubleMap.put(currencyNames.get(j), Double.valueOf(rates.get(j)));
            }
            conversionTable.put(sourceCurrency, targetCurrencyToDoubleMap);
        }
    }

    private double getConvertedAmount(String sourceCurrency, String targetCurrency, double amount) {
        return conversionTable.get(sourceCurrency).get(targetCurrency) * amount;
    }

    @RequestMapping("/{sourceCurrency}/{targetCurrency}/{amount}")
    public ResponseEntity<String> convertCurrency(@PathVariable("sourceCurrency") String sourceCurrency,
                                                 @PathVariable("targetCurrency") String targetCurrency,
                                                 @PathVariable("amount") String amount) {

        if (!conversionTable.containsKey(sourceCurrency.toUpperCase()) ||
                !conversionTable.containsKey(targetCurrency.toUpperCase())){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Conversion of these currencies is not supported.");
        }

        try{
            Double.valueOf(amount);
        }
        catch (Exception ex){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(amount + " is not a valid amount.");
        }

        return ResponseEntity.ok(targetCurrency.toUpperCase() + ": " +
                getConvertedAmount(sourceCurrency.toUpperCase(), targetCurrency.toUpperCase(), Double.valueOf(amount)));
    }
}
