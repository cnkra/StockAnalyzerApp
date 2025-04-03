package data;

import java.time.LocalDate;
import java.util.*;

public class TechnicalIndicators {

    public static Map<LocalDate, Double> calculateRSI(Map<LocalDate, Double> priceData, int period) {
        Map<LocalDate, Double> rsiValues = new LinkedHashMap<>();
        List<LocalDate> dates = new ArrayList<>(priceData.keySet());
        Collections.sort(dates); // artan sıraya sok

        List<Double> gains = new ArrayList<>();
        List<Double> losses = new ArrayList<>();

        for (int i = 1; i < dates.size(); i++) {
            double change = priceData.get(dates.get(i)) - priceData.get(dates.get(i - 1));
            if (change >= 0) {
                gains.add(change);
                losses.add(0.0);
            } else {
                gains.add(0.0);
                losses.add(-change);
            }
        }

        if (gains.size() < period) {
            System.out.println("RSI için yeterli veri yok.");
            return rsiValues;
        }

        double avgGain = gains.subList(0, period).stream().mapToDouble(Double::doubleValue).sum() / period;
        double avgLoss = losses.subList(0, period).stream().mapToDouble(Double::doubleValue).sum() / period;

        for (int i = period; i < gains.size(); i++) {
            avgGain = (avgGain * (period - 1) + gains.get(i)) / period;
            avgLoss = (avgLoss * (period - 1) + losses.get(i)) / period;

            double rs = avgLoss == 0 ? 100 : avgGain / avgLoss;
            double rsi = 100 - (100 / (1 + rs));
            rsiValues.put(dates.get(i + 1), rsi);
        }

        return rsiValues;
    }

    public static Map<LocalDate, Double> calculateEMA(Map<LocalDate, Double> prices, int period) {
        Map<LocalDate, Double> emaValues = new LinkedHashMap<>();
        List<LocalDate> dates = new ArrayList<>(prices.keySet());
        Collections.sort(dates);

        if (dates.size() < period) return emaValues;

        double multiplier = 2.0 / (period + 1);
        double previousEMA = 0;

        for (int i = 0; i < dates.size(); i++) {
            LocalDate date = dates.get(i);
            double price = prices.get(date);

            if (i == period - 1) {
                double sum = 0;
                for (int j = 0; j < period; j++) {
                    sum += prices.get(dates.get(j));
                }
                previousEMA = sum / period;
                emaValues.put(date, previousEMA);
            } else if (i >= period) {
                double ema = (price - previousEMA) * multiplier + previousEMA;
                emaValues.put(date, ema);
                previousEMA = ema;
            }
        }

        return emaValues;
    }

    public static Map<LocalDate, Double> calculateSMA(Map<LocalDate, Double> prices, int period) {
        Map<LocalDate, Double> smaValues = new LinkedHashMap<>();
        List<LocalDate> dates = new ArrayList<>(prices.keySet());
        Collections.sort(dates);

        for (int i = period - 1; i < dates.size(); i++) {
            double sum = 0;
            for (int j = i - period + 1; j <= i; j++) {
                sum += prices.get(dates.get(j));
            }
            double sma = sum / period;
            smaValues.put(dates.get(i), sma);
        }

        return smaValues;
    }

    public static Map<LocalDate, Double> calculateMACD(Map<LocalDate, Double> prices) {
        Map<LocalDate, Double> macdValues = new LinkedHashMap<>();
        Map<LocalDate, Double> ema12 = calculateEMA(prices, 12);
        Map<LocalDate, Double> ema26 = calculateEMA(prices, 26);

        for (LocalDate date : ema12.keySet()) {
            if (ema26.containsKey(date)) {
                double macd = ema12.get(date) - ema26.get(date);
                macdValues.put(date, macd);
            }
        }

        return macdValues;
    }

    public static Map<LocalDate, Double[]> calculateBollingerBands(Map<LocalDate, Double> prices, int period) {
        Map<LocalDate, Double[]> bands = new LinkedHashMap<>();
        List<LocalDate> dates = new ArrayList<>(prices.keySet());
        Collections.sort(dates);

        for (int i = period - 1; i < dates.size(); i++) {
            double sum = 0;
            for (int j = i - period + 1; j <= i; j++) {
                sum += prices.get(dates.get(j));
            }
            double sma = sum / period;

            double variance = 0;
            for (int j = i - period + 1; j <= i; j++) {
                variance += Math.pow(prices.get(dates.get(j)) - sma, 2);
            }

            double stddev = Math.sqrt(variance / period);
            double upper = sma + (2 * stddev);
            double lower = sma - (2 * stddev);

            bands.put(dates.get(i), new Double[]{upper, sma, lower});
        }

        return bands;
    }


    public static Map<LocalDate, Double[]> calculateStochasticOscillator(List<PriceCandle> candles, int periodK, int periodD) {
        Map<LocalDate, Double[]> stochMap = new LinkedHashMap<>();

        List<Double> kValues = new ArrayList<>();

        for (int i = periodK - 1; i < candles.size(); i++) {
            double highestHigh = candles.subList(i - periodK + 1, i + 1)
                    .stream().mapToDouble(c -> c.high).max().orElse(0);
            double lowestLow = candles.subList(i - periodK + 1, i + 1)
                    .stream().mapToDouble(c -> c.low).min().orElse(0);
            double close = candles.get(i).close;

            double percentK = 100 * ((close - lowestLow) / (highestHigh - lowestLow));
            kValues.add(percentK);

            if (kValues.size() >= periodD) {
                double percentD = kValues.subList(kValues.size() - periodD, kValues.size())
                        .stream().mapToDouble(d -> d).average().orElse(0);
                stochMap.put(candles.get(i).date, new Double[]{percentK, percentD});
            }
        }

        return stochMap;
    }


    public static Map<LocalDate, Double> calculateCCI(List<PriceCandle> candles, int period) {
        Map<LocalDate, Double> cciMap = new LinkedHashMap<>();

        for (int i = period - 1; i < candles.size(); i++) {
            List<Double> typicalPrices = new ArrayList<>();
            for (int j = i - period + 1; j <= i; j++) {
                PriceCandle c = candles.get(j);
                double tp = (c.high + c.low + c.close) / 3;
                typicalPrices.add(tp);
            }

            double avgTP = typicalPrices.stream().mapToDouble(d -> d).average().orElse(0);

            double meanDeviation = typicalPrices.stream()
                    .mapToDouble(tp -> Math.abs(tp - avgTP))
                    .average().orElse(0);

            double lastTP = typicalPrices.get(typicalPrices.size() - 1);

            double cci = (lastTP - avgTP) / (0.015 * meanDeviation);
            cciMap.put(candles.get(i).date, cci);
        }

        return cciMap;
    }


    public static Map<LocalDate, Double> calculateADX(List<PriceCandle> candles, int period) {
        Map<LocalDate, Double> adxValues = new LinkedHashMap<>();
        List<Double> trList = new ArrayList<>();
        List<Double> plusDMList = new ArrayList<>();
        List<Double> minusDMList = new ArrayList<>();

        for (int i = 1; i < candles.size(); i++) {
            double highDiff = candles.get(i).high - candles.get(i - 1).high;
            double lowDiff = candles.get(i - 1).low - candles.get(i).low;

            double plusDM = (highDiff > lowDiff && highDiff > 0) ? highDiff : 0;
            double minusDM = (lowDiff > highDiff && lowDiff > 0) ? lowDiff : 0;

            double tr = Math.max(candles.get(i).high - candles.get(i).low,
                    Math.max(Math.abs(candles.get(i).high - candles.get(i - 1).close),
                            Math.abs(candles.get(i).low - candles.get(i - 1).close)));

            plusDMList.add(plusDM);
            minusDMList.add(minusDM);
            trList.add(tr);
        }

        List<Double> dxList = new ArrayList<>();

        for (int i = period; i < trList.size(); i++) {
            double sumTR = 0, sumPlusDM = 0, sumMinusDM = 0;
            for (int j = i - period; j < i; j++) {
                sumTR += trList.get(j);
                sumPlusDM += plusDMList.get(j);
                sumMinusDM += minusDMList.get(j);
            }

            double plusDI = 100 * (sumPlusDM / sumTR);
            double minusDI = 100 * (sumMinusDM / sumTR);
            double dx = 100 * Math.abs(plusDI - minusDI) / (plusDI + minusDI);
            dxList.add(dx);

            if (dxList.size() >= period) {
                double adx = dxList.subList(dxList.size() - period, dxList.size()).stream().mapToDouble(x -> x).average().orElse(0);
                adxValues.put(candles.get(i + 1).date, adx);
            }
        }

        return adxValues;
    }


    public static Map<LocalDate, Double[]> calculateIchimokuCloud(List<PriceCandle> candles) {
        Map<LocalDate, Double[]> ichimokuMap = new LinkedHashMap<>();

        for (int i = 52; i < candles.size() - 26; i++) {
            double high9 = candles.subList(i - 9, i).stream().mapToDouble(c -> c.high).max().orElse(0);
            double low9 = candles.subList(i - 9, i).stream().mapToDouble(c -> c.low).min().orElse(0);
            double tenkan = (high9 + low9) / 2;

            double high26 = candles.subList(i - 26, i).stream().mapToDouble(c -> c.high).max().orElse(0);
            double low26 = candles.subList(i - 26, i).stream().mapToDouble(c -> c.low).min().orElse(0);
            double kijun = (high26 + low26) / 2;

            double spanA = (tenkan + kijun) / 2;

            double high52 = candles.subList(i - 52, i).stream().mapToDouble(c -> c.high).max().orElse(0);
            double low52 = candles.subList(i - 52, i).stream().mapToDouble(c -> c.low).min().orElse(0);
            double spanB = (high52 + low52) / 2;

            // forward shifted by 26
            LocalDate futureDate = candles.get(i + 26).date;

            ichimokuMap.put(futureDate, new Double[]{tenkan, kijun, spanA, spanB});
        }

        return ichimokuMap;
    }


    public static Map<LocalDate, Double> calculateParabolicSAR(List<PriceCandle> candles) {
        Map<LocalDate, Double> sarMap = new LinkedHashMap<>();

        boolean isUptrend = true;
        double af = 0.02;
        double maxAf = 0.2;
        double ep = candles.get(0).high;
        double sar = candles.get(0).low;

        for (int i = 1; i < candles.size(); i++) {
            PriceCandle current = candles.get(i);
            PriceCandle prev = candles.get(i - 1);

            sar = sar + af * (ep - sar);
            sarMap.put(current.date, sar);

            if (isUptrend) {
                if (current.low < sar) {
                    isUptrend = false;
                    sar = ep;
                    ep = current.low;
                    af = 0.02;
                } else {
                    if (current.high > ep) {
                        ep = current.high;
                        af = Math.min(af + 0.02, maxAf);
                    }
                }
            } else {
                if (current.high > sar) {
                    isUptrend = true;
                    sar = ep;
                    ep = current.high;
                    af = 0.02;
                } else {
                    if (current.low < ep) {
                        ep = current.low;
                        af = Math.min(af + 0.02, maxAf);
                    }
                }
            }
        }

        return sarMap;
    }
}