import data.StockDataFetcher;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class StockDataFetcherTest {

    @Test
    public void testMockedClosingPrices() {

        //MockData
        Map<LocalDate, Double> mockPrices = new LinkedHashMap<>();
        LocalDate today = LocalDate.now();
        for (int i = 0; i < 20; i++) {
            mockPrices.put(today.minusDays(20 - i), 100 + Math.sin(i / 2.0) * 5);
        }
        //RSI_Calculator
        Map<LocalDate, Double> rsi = data.TechnicalIndicators.calculateRSI(mockPrices, 14);

        //Controlls
        assertFalse(rsi.isEmpty(), "RSI map should not be empty.");
        assertTrue(rsi.values().stream().allMatch(val -> val >= 0 && val <= 100), "RSI values should be between 0 and 100.");

        // Printer
        rsi.forEach((date, value) -> System.out.println(date + " → RSI: " + String.format("%.2f", value)));
    }
}
