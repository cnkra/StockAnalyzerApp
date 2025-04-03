package data;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDate;
import java.util.*;

import org.json.JSONObject;
import config.ApiConfig;
import model.ApiProvider;

public class StockDataFetcher {

    public List<PriceCandle> fetchPriceCandles(String symbol) throws Exception {
        String url;

        if (ApiConfig.getProvider() == ApiProvider.ALPHA_VANTAGE) {
            String baseUrl = "https://www.alphavantage.co/query";
            String function = "TIME_SERIES_DAILY";
            url = String.format("%s?function=%s&symbol=%s&apikey=%s", baseUrl, function, symbol, ApiConfig.getApiKey());
        } else {
            String baseUrl = "https://api.twelvedata.com/time_series";
            url = String.format("%s?symbol=%s&interval=1day&outputsize=5000&apikey=%s", baseUrl, symbol, ApiConfig.getApiKey());
        }

        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setRequestMethod("GET");

        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        StringBuilder responseBuilder = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null)
            responseBuilder.append(line);
        reader.close();

        JSONObject json = new JSONObject(responseBuilder.toString());

        List<PriceCandle> candles = new ArrayList<>();

        if (ApiConfig.getProvider() == ApiProvider.ALPHA_VANTAGE) {
            JSONObject timeSeries = json.getJSONObject("Time Series (Daily)");
            for (String date : timeSeries.keySet()) {
                JSONObject dayData = timeSeries.getJSONObject(date);
                double open = dayData.getDouble("1. open");
                double high = dayData.getDouble("2. high");
                double low = dayData.getDouble("3. low");
                double close = dayData.getDouble("4. close");
                candles.add(new PriceCandle(LocalDate.parse(date), open, high, low, close));
            }
        } else {
            for (Object o : json.getJSONArray("values")) {
                JSONObject entry = (JSONObject) o;
                LocalDate date = LocalDate.parse(entry.getString("datetime"));
                double open = entry.getDouble("open");
                double high = entry.getDouble("high");
                double low = entry.getDouble("low");
                double close = entry.getDouble("close");
                candles.add(new PriceCandle(date, open, high, low, close));
            }
        }

        // Sort descending (most recent first)
        candles.sort((a, b) -> b.date.compareTo(a.date));

        return candles;
    }



    public Map<LocalDate, Double> fetchDailyClosingPrices(String symbol) throws Exception {
        String url;

        if (ApiConfig.getProvider() == ApiProvider.ALPHA_VANTAGE) {
            String baseUrl = "https://www.alphavantage.co/query";
            String function = "TIME_SERIES_DAILY";
            url = String.format("%s?function=%s&symbol=%s&apikey=%s",
                    baseUrl, function, symbol, ApiConfig.getApiKey());
        } else {
            String baseUrl = "https://api.twelvedata.com/time_series";
            url = String.format("%s?symbol=%s&interval=1day&apikey=%s",
                    baseUrl, symbol, ApiConfig.getApiKey());
        }

        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setRequestMethod("GET");

        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        StringBuilder responseBuilder = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null)
            responseBuilder.append(line);
        reader.close();

        JSONObject json = new JSONObject(responseBuilder.toString());
        System.out.println(json.toString(2)); // Cevabı yaz

        Map<LocalDate, Double> priceData = new TreeMap<>(Collections.reverseOrder());

        if (ApiConfig.getProvider() == ApiProvider.ALPHA_VANTAGE) {
            JSONObject timeSeries = json.getJSONObject("Time Series (Daily)");
            for (String date : timeSeries.keySet()) {
                JSONObject dayData = timeSeries.getJSONObject(date);
                double close = dayData.getDouble("4. close");
                priceData.put(LocalDate.parse(date), close);
            }
        } else {
            for (Object o : json.getJSONArray("values")) {
                JSONObject entry = (JSONObject) o;
                LocalDate date = LocalDate.parse(entry.getString("datetime"));
                double close = entry.getDouble("close");
                priceData.put(date, close);
            }
        }

        return priceData;
    }
}