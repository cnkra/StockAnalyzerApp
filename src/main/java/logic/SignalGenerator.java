package logic;

public class SignalGenerator {

    // RSI: overbought > 70, oversold < 30
    public static String getRSISignal(double latestRSI) {
        if (latestRSI > 70) return "SELL";
        else if (latestRSI < 30) return "BUY";
        else return "HOLD";
    }

    // EMA: price > EMA → uptrend (BUY), price < EMA → downtrend (SELL)
    public static String getEMASignal(double price, double ema) {
        if (price > ema) return "BUY";
        else if (price < ema) return "SELL";
        else return "HOLD";
    }

    // SMA: price > SMA → BUY, price < SMA → SELL
    public static String getSMASignal(double price, double sma) {
        if (price > sma) return "BUY";
        else if (price < sma) return "SELL";
        else return "HOLD";
    }

    // MACD: MACD > 0 → momentum upward (BUY), MACD < 0 → downward (SELL)
    public static String getMACDSignal(double latestMACD) {
        if (latestMACD > 0) return "BUY";
        else if (latestMACD < 0) return "SELL";
        else return "HOLD";
    }

    public static String getBollingerSignal(double price, double upper, double lower) {
        if (price > upper) return "SELL";
        else if (price < lower) return "BUY";
        else return "HOLD";
    }

    public static String getStochasticSignal(double percentK, double percentD) {
        if (percentK > 80 && percentK > percentD) return "SELL";
        else if (percentK < 20 && percentK < percentD) return "BUY";
        else return "HOLD";
    }

    public static String getCCISignal(double cci) {
        if (cci > 100) return "BUY";
        else if (cci < -100) return "SELL";
        else return "HOLD";
    }

    public static String getADXSignal(double adx, double plusDI, double minusDI) {
        if (adx < 20) return "HOLD";
        if (plusDI > minusDI) return "BUY";
        if (minusDI > plusDI) return "SELL";
        return "HOLD";
    }


    public static String getIchimokuSignal(double tenkan, double kijun) {
        if (tenkan > kijun) return "BUY";
        else if (tenkan < kijun) return "SELL";
        else return "HOLD";
    }


    public static String getParabolicSARSignal(double price, double sar) {
        if (price > sar) return "BUY";
        else if (price < sar) return "SELL";
        else return "HOLD";
    }
}