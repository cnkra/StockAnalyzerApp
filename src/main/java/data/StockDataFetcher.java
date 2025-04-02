package data;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDate;
import java.util.*;
import org.json.JSONObject;

// Fetches historical daily stock data from Alpha Vantage API.

public class StockDataFetcher
{
    private static final String API_KEY = "38K5G0K9RTXXNMVP"; // TODO: Replace with your own key :)
    private static final String BASE_URL = "https://www.alphavantage.co/query";

    /*
    Fetches daily close prices for the given stock symbol.
    @param symbol Stock ticker symbol
    @return Map with LocalDate keys and closing price values
     */

    public Map<LocalDate, Double> fetchDailyClosingPrices(String symbol) throws Exception{
        String function = "TIME_SERIES_DAILY";
        String url = String.format("%s?function=%s&symbol=%s&apikey=%s", BASE_URL, function, symbol, API_KEY);

        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setRequestMethod("GET");

        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        StringBuilder responseBuilder = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null)
            responseBuilder.append(line);
        reader.close();

        JSONObject json = new JSONObject(responseBuilder.toString());
        System.out.println(json.toString(2)); // API'den dönen cevabı ekrana yazdır
        JSONObject timeSeries = json.getJSONObject("Time Series (Daily)");

        Map<LocalDate, Double> priceData = new TreeMap<>(Collections.reverseOrder());
        for (String date : timeSeries.keySet()) {
            JSONObject dayData = timeSeries.getJSONObject(date);
            double close= dayData.getDouble("4. close");
            priceData.put(LocalDate.parse(date), close);
        }
        return priceData;
    }
}









