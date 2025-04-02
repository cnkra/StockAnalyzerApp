package data;

import java.time.LocalDate;
import java.util.*;

/**
 * Utility class to calculate technical indicators such as RSI (Relative Strength Index).
 */
public class TechnicalIndicators {

    /**
     * Calculates the RSI for a given set of price data.
     *
     * @param priceData Map of LocalDate to closing prices, sorted by date.
     * @param period    Number of days to use for RSI calculation (typically 14).
     * @return Map of LocalDate to RSI values.
     */
    public static Map<LocalDate, Double> calculateRSI(Map<LocalDate, Double> priceData, int period) {
        Map<LocalDate, Double> rsiValues = new LinkedHashMap<>();
        List<LocalDate> dates = new ArrayList<>(priceData.keySet());
        Collections.sort(dates); // ensure ascending order

        List<Double> gains = new ArrayList<>();
        List<Double> losses = new ArrayList<>();

        // Compute daily gains and losses
        for (int i = 1; i < dates.size(); i++)
        {
            double change = priceData.get(dates.get(i)) - priceData.get(dates.get(i - 1));
            if (change < 0) {
                gains.add(change);
                losses.add(0.00);
            }
            else {
                losses.add(-change);
                gains.add(0.00);
            }
        }

        // Guard clause: insufficient data to calculate RSI
        if (gains.size() < period) {
            System.out.println("Not enough data to calculate RSI.");
            return rsiValues; // returns empty map
        }

        // Calculate RSI using the specified period
        for (int i = period; i < gains.size(); i++) {
            double avgGain = 0.0;
            double avgLoss = 0.0;

            for (int j = i - period; j < i; j++) {
                avgGain += gains.get(j);
                avgLoss += losses.get(j);
            }

            avgGain /= period;
            avgLoss /= period;

            double rs = avgLoss == 0 ? 100 : avgGain / avgLoss;
            double rsi = 100 - (100 / (1 + rs));

            rsiValues.put(dates.get(i), rsi);
        }

        return rsiValues;
    }
}
