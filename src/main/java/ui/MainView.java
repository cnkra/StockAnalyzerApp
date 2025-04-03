
package ui;

import data.StockDataFetcher;
import data.TechnicalIndicators;
import logic.SignalGenerator;
import config.ApiConfig;
import model.ApiProvider;
import data.PriceCandle;

import javafx.scene.layout.BorderPane;
import javafx.scene.chart.PieChart;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.layout.GridPane;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;

import java.time.LocalDate;
import java.util.*;

public class MainView extends Application {

    private final Map<String, String> signalTexts = new LinkedHashMap<>();
    private final Map<String, LineChart<Number, Number>> charts = new HashMap<>();
    private Map<LocalDate, Double> closePrices;
    private List<PriceCandle> candles;
    private VBox chartBox;
    private TextArea signalOutput;
    private PieChart signalPieChart = new PieChart();
    private int buyCount = 0;
    private int sellCount = 0;
    private int holdCount = 0;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Stock Analyzer - Full Version");

        ChoiceDialog<String> dialog = new ChoiceDialog<>("Twelve Data", "Twelve Data", "Alpha Vantage");
        dialog.setTitle("API Selection");
        dialog.setHeaderText("Choose your data provider:");
        dialog.setContentText("Select:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(choice -> {
            if (choice.equals("Alpha Vantage")) {
                ApiConfig.selectProvider(ApiProvider.ALPHA_VANTAGE);
            } else {
                ApiConfig.selectProvider(ApiProvider.TWELVE_DATA);
            }
        });

        TextField symbolField = new TextField();
        symbolField.setPromptText("Enter stock symbol (e.g., AAPL)");

        Button fetchButton = new Button("Fetch Data");

        signalOutput = new TextArea();
        signalOutput.setEditable(false);

        chartBox = new VBox(10);
        chartBox.setPadding(new Insets(10));

        ScrollPane scrollPane = new ScrollPane(chartBox);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(300);

        String[] indicators = {"RSI", "EMA", "MACD", "SMA", "BB", "Stoch", "CCI", "ADX", "Ichimoku", "SAR"};
        Map<String, CheckBox> chartChecks = new HashMap<>();
        Map<String, CheckBox> signalChecks = new HashMap<>();

        GridPane checkGrid = new GridPane();
        checkGrid.setHgap(10);
        checkGrid.setVgap(5);

        for (int i = 0; i < indicators.length; i++) {
            String ind = indicators[i];
            CheckBox chartBoxCheck = new CheckBox("Show " + ind + " chart");
            CheckBox signalBoxCheck = new CheckBox("Show " + ind + " signal");
            chartChecks.put(ind, chartBoxCheck);
            signalChecks.put(ind, signalBoxCheck);
            checkGrid.add(new Label(ind), 0, i);
            checkGrid.add(chartBoxCheck, 1, i);
            checkGrid.add(signalBoxCheck, 2, i);
        }

        // Ana layout: BorderPane
        BorderPane mainLayout = new BorderPane();

        // Sol taraf: kontrol alanı (sembol gir, buton, checkbox'lar)
        VBox controlBox = new VBox(15, symbolField, fetchButton, checkGrid, signalOutput);
        controlBox.setPadding(new Insets(10));

        // Sağ üst köşe: sabit boyutlu pie chart
        VBox pieBox = new VBox(signalPieChart);
        pieBox.setPadding(new Insets(10));
        pieBox.setPrefSize(300, 300);
        signalPieChart.setPrefSize(250, 250);
        signalPieChart.setLabelsVisible(true);  // Etiketleri göster

        // Alt: çizilecek grafikler
        chartBox.setPadding(new Insets(10));

        // Yerleştir
        mainLayout.setLeft(controlBox);
        mainLayout.setRight(pieBox);
        mainLayout.setBottom(scrollPane);

        // Sahneyi oluştur ve göster
        Scene scene = new Scene(mainLayout, 1000, 800);
        primaryStage.setScene(scene);
        primaryStage.show();

        fetchButton.setOnAction(e -> {
            String symbol = symbolField.getText().trim();
            if (symbol.isEmpty()) {
                signalOutput.setText("Please enter a stock symbol.");
                return;
            }

            try {
                StockDataFetcher fetcher = new StockDataFetcher();
                closePrices = fetcher.fetchDailyClosingPrices(symbol);
                candles = fetcher.fetchPriceCandles(symbol);
                signalOutput.setText("Data fetched. Now toggle indicators.");
                chartBox.getChildren().clear();

            } catch (Exception ex) {
                signalOutput.setText("Error fetching data: " + ex.getMessage());
                ex.printStackTrace();
            }

            signalPieChart.getData().clear();
            signalPieChart.getData().add(new PieChart.Data("BUY", buyCount));
            signalPieChart.getData().add(new PieChart.Data("SELL", sellCount));
            signalPieChart.getData().add(new PieChart.Data("HOLD", holdCount));
            signalPieChart.setTitle("Signal Distribution");
        });

        for (String ind : indicators) {
            chartChecks.get(ind).setOnAction(ev -> refreshChart(ind, chartChecks.get(ind).isSelected()));
            signalChecks.get(ind).setOnAction(ev -> refreshSignal(ind, signalChecks.get(ind).isSelected()));
        }
    }

    private void refreshChart(String ind, boolean selected) {
        if (closePrices == null || candles == null) return;

        Map<LocalDate, Double> data = null;
        Map<LocalDate, Double[]> data2D = null;
        String title = ind + " Chart";

        LineChart<Number, Number> chart = new LineChart<>(new NumberAxis(), new NumberAxis());
        chart.setTitle(title);
        chart.setMinHeight(200);
        XYChart.Series<Number, Number> series1 = new XYChart.Series<>();
        XYChart.Series<Number, Number> series2 = new XYChart.Series<>();

        try {
            switch (ind) {
                case "RSI" -> {
                    data = TechnicalIndicators.calculateRSI(closePrices, 14);
                    for (Double val : data.values()) series1.getData().add(new XYChart.Data<>(series1.getData().size(), val));
                    chart.getYAxis().setLabel("RSI");
                    chart.getXAxis().setLabel("Day");
                    series1.setName("RSI");
                    chart.getData().add(series1);
                }
                case "EMA" -> {
                    data = TechnicalIndicators.calculateEMA(closePrices, 14);
                    for (Double val : data.values()) series1.getData().add(new XYChart.Data<>(series1.getData().size(), val));
                    series1.setName("EMA");
                    chart.getData().add(series1);
                }
                case "MACD" -> {
                    data = TechnicalIndicators.calculateMACD(closePrices);
                    for (Double val : data.values()) series1.getData().add(new XYChart.Data<>(series1.getData().size(), val));
                    series1.setName("MACD");
                    chart.getData().add(series1);
                }
                case "SMA" -> {
                    data = TechnicalIndicators.calculateSMA(closePrices, 14);
                    for (Double val : data.values()) series1.getData().add(new XYChart.Data<>(series1.getData().size(), val));
                    series1.setName("SMA");
                    chart.getData().add(series1);
                }
                case "BB" -> {
                    data2D = TechnicalIndicators.calculateBollingerBands(closePrices, 20);
                    for (Double[] val : data2D.values()) {
                        series1.getData().add(new XYChart.Data<>(series1.getData().size(), val[0]));
                        series2.getData().add(new XYChart.Data<>(series2.getData().size(), val[2]));
                    }
                    series1.setName("Upper Band");
                    series2.setName("Lower Band");
                    chart.getData().addAll(series1, series2);
                }
                case "Stoch" -> {
                    data2D = TechnicalIndicators.calculateStochasticOscillator(candles, 14, 3);
                    for (Double[] val : data2D.values()) {
                        series1.getData().add(new XYChart.Data<>(series1.getData().size(), val[0]));
                        series2.getData().add(new XYChart.Data<>(series2.getData().size(), val[1]));
                    }
                    series1.setName("%K");
                    series2.setName("%D");
                    chart.getData().addAll(series1, series2);
                }
                case "CCI" -> {
                    data = TechnicalIndicators.calculateCCI(candles, 20);
                    for (Double val : data.values()) series1.getData().add(new XYChart.Data<>(series1.getData().size(), val));
                    series1.setName("CCI");
                    chart.getData().add(series1);
                }
                case "ADX" -> {
                    data = TechnicalIndicators.calculateADX(candles, 14);
                    for (Double val : data.values()) series1.getData().add(new XYChart.Data<>(series1.getData().size(), val));
                    series1.setName("ADX");
                    chart.getData().add(series1);
                }
                case "Ichimoku" -> {
                    data2D = TechnicalIndicators.calculateIchimokuCloud(candles);
                    for (Double[] val : data2D.values()) {
                        series1.getData().add(new XYChart.Data<>(series1.getData().size(), val[0]));
                        series2.getData().add(new XYChart.Data<>(series2.getData().size(), val[1]));
                    }
                    series1.setName("Tenkan-sen");
                    series2.setName("Kijun-sen");
                    chart.getData().addAll(series1, series2);
                }
                case "SAR" -> {
                    data = TechnicalIndicators.calculateParabolicSAR(candles);
                    for (Double val : data.values()) series1.getData().add(new XYChart.Data<>(series1.getData().size(), val));
                    series1.setName("SAR");
                    chart.getData().add(series1);
                }
            }

            if (selected) {
                chartBox.getChildren().add(chart);
                charts.put(ind, chart); // referansı sakla
            } else {
                LineChart<Number, Number> existing = charts.get(ind);
                if (existing != null) {
                    chartBox.getChildren().remove(existing); // doğru referansı sil
                    charts.remove(ind);
                }
            }

        } catch (Exception e) {
            signalOutput.appendText("Error plotting " + ind + ": " + e.getMessage() + "\n");
        }
    }

    private void refreshSignal(String ind, boolean selected) {
        if (closePrices == null || candles == null) return;

        try {
            String result = null;

            switch (ind) {
                case "RSI" -> {
                    Map<LocalDate, Double> rsi = TechnicalIndicators.calculateRSI(closePrices, 14);
                    Double latest = rsi.values().stream().reduce((a, b) -> b).orElse(null);
                    result = "RSI Signal: " + SignalGenerator.getRSISignal(latest);
                }
                case "EMA" -> {
                    Map<LocalDate, Double> ema = TechnicalIndicators.calculateEMA(closePrices, 14);
                    Double latest = ema.values().stream().reduce((a, b) -> b).orElse(null);
                    Double price = closePrices.values().stream().reduce((a, b) -> b).orElse(null);
                    result = "EMA Signal: " + SignalGenerator.getEMASignal(price, latest);
                }
                case "MACD" -> {
                    Map<LocalDate, Double> macd = TechnicalIndicators.calculateMACD(closePrices);
                    Double latest = macd.values().stream().reduce((a, b) -> b).orElse(null);
                    result = "MACD Signal: " + SignalGenerator.getMACDSignal(latest);
                }
                case "SMA" -> {
                    Map<LocalDate, Double> sma = TechnicalIndicators.calculateSMA(closePrices, 14);
                    Double latest = sma.values().stream().reduce((a, b) -> b).orElse(null);
                    Double price = closePrices.values().stream().reduce((a, b) -> b).orElse(null);
                    result = "SMA Signal: " + SignalGenerator.getSMASignal(price, latest);
                }
                case "BB" -> {
                    Map<LocalDate, Double[]> bb = TechnicalIndicators.calculateBollingerBands(closePrices, 20);
                    Double[] latest = bb.values().stream().reduce((a, b) -> b).orElse(null);
                    Double price = closePrices.values().stream().reduce((a, b) -> b).orElse(null);
                    result = "Bollinger Signal: " + SignalGenerator.getBollingerSignal(price, latest[0], latest[2]);
                }
                case "Stoch" -> {
                    Map<LocalDate, Double[]> stoch = TechnicalIndicators.calculateStochasticOscillator(candles, 14, 3);
                    Double[] latest = stoch.values().stream().reduce((a, b) -> b).orElse(null);
                    result = "Stochastic Signal: " + SignalGenerator.getStochasticSignal(latest[0], latest[1]);
                }
                case "CCI" -> {
                    Map<LocalDate, Double> cci = TechnicalIndicators.calculateCCI(candles, 20);
                    Double latest = cci.values().stream().reduce((a, b) -> b).orElse(null);
                    result = "CCI Signal: " + SignalGenerator.getCCISignal(latest);
                }
                case "ADX" -> {
                    Map<LocalDate, Double> adx = TechnicalIndicators.calculateADX(candles, 14);
                    Double latest = adx.values().stream().reduce((a, b) -> b).orElse(null);
                    result = "ADX Signal: " + SignalGenerator.getADXSignal(latest, 0, 0);
                }
                case "Ichimoku" -> {
                    Map<LocalDate, Double[]> ichi = TechnicalIndicators.calculateIchimokuCloud(candles);
                    Double[] latest = ichi.values().stream().reduce((a, b) -> b).orElse(null);
                    result = "Ichimoku Signal: " + SignalGenerator.getIchimokuSignal(latest[0], latest[1]);
                }
                case "SAR" -> {
                    Map<LocalDate, Double> sar = TechnicalIndicators.calculateParabolicSAR(candles);
                    Double latest = sar.values().stream().reduce((a, b) -> b).orElse(null);
                    Double price = closePrices.values().stream().reduce((a, b) -> b).orElse(null);
                    result = "Parabolic SAR Signal: " + SignalGenerator.getParabolicSARSignal(price, latest);
                }
            }

            if (selected && result != null) {
                signalTexts.put(ind, result);
            } else {
                signalTexts.remove(ind);
            }

            // Refresh full signal area
            signalOutput.clear();
            for (String text : signalTexts.values()) {
                signalOutput.appendText(text + "\n");
            }

            // Refresh pie chart counts
            buyCount = 0;
            sellCount = 0;
            holdCount = 0;

            for (String text : signalTexts.values()) {
                if (text.contains("BUY")) buyCount++;
                else if (text.contains("SELL")) sellCount++;
                else holdCount++;
            }

            // Update pie chart UI
            signalPieChart.getData().clear();
            int totalSignals = buyCount + sellCount + holdCount;

            signalPieChart.getData().add(new PieChart.Data("BUY (" + buyCount + "/" + totalSignals + ")", buyCount));
            signalPieChart.getData().add(new PieChart.Data("SELL (" + sellCount + "/" + totalSignals + ")", sellCount));
            signalPieChart.getData().add(new PieChart.Data("HOLD (" + holdCount + "/" + totalSignals + ")", holdCount));
            signalPieChart.setTitle("Signal Distribution");



        } catch (Exception ex) {
            signalOutput.appendText("Error generating signal for " + ind + ": " + ex.getMessage() + "\n");
        }
    }
}
