package ui;

import data.StockDataFetcher;
import data.TechnicalIndicators;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.time.LocalDate;
import java.util.Map;

public class MainView extends Application {

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Stock RSI Analyzer");

        TextField symbolField = new TextField();
        symbolField.setPromptText("Enter stock symbol (e.g., AAPL)");

        Button fetchButton = new Button("Fetch & Calculate RSI");

        TextArea outputArea = new TextArea();
        outputArea.setEditable(false);

        fetchButton.setOnAction(e -> {
            String symbol = symbolField.getText().trim();
            if (symbol.isEmpty()) {
                outputArea.setText("Please enter a stock symbol.");
                return;
            }

            try {
                StockDataFetcher fetcher = new StockDataFetcher();
                Map<LocalDate, Double> prices = fetcher.fetchDailyClosingPrices(symbol);

                Map<LocalDate, Double> rsi = TechnicalIndicators.calculateRSI(prices, 14);
                StringBuilder sb = new StringBuilder();

                for (Map.Entry<LocalDate, Double> entry : rsi.entrySet()) {
                    sb.append(entry.getKey()).append(" → RSI: ").append(String.format("%.2f", entry.getValue())).append("\n");
                }

                outputArea.setText(sb.toString());
            } catch (Exception ex) {
                outputArea.setText("Error: " + ex.getMessage());
                ex.printStackTrace();
            }
        });

        VBox layout = new VBox(10);
        layout.setPadding(new Insets(15));
        layout.getChildren().addAll(new Label("Stock Symbol:"), symbolField, fetchButton, outputArea);

        Scene scene = new Scene(layout, 500, 400);
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}
