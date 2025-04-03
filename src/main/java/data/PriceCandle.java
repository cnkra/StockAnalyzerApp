package data;

import java.time.LocalDate;

public class PriceCandle {
    public LocalDate date;
    public double open;
    public double high;
    public double low;
    public double close;

    public PriceCandle(LocalDate date, double open, double high, double low, double close) {
        this.date = date;
        this.open = open;
        this.high = high;
        this.low = low;
        this.close = close;
    }
}