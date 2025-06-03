package org.studyeasy;

import org.studyeasy.service.PriceComparisonService;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;
import java.util.HashMap;
import java.util.Date;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.time.Day;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;

public class CryptoComparerGUI extends JFrame {
    private final PriceComparisonService service;
    private JComboBox<String> cryptoSelector;
    private JButton fetchButton;
    private JTextArea resultArea;
    private JLabel statusLabel;
    private JTextField alertPriceField;
    private JButton setAlertButton;
    private Map<String, Double> alertPrices = new HashMap<>();
    private Timer alertCheckTimer;
    private JComboBox<String> refreshIntervalSelector;
    private Timer autoRefreshTimer;
    private JPanel headerPanel; // Add this field declaration

    // List of supported cryptocurrencies
    private final String[] cryptoOptions = {
            "BTC (Bitcoin)", "ETH (Ethereum)", "SOL (Solana)", 
            "ADA (Cardano)", "DOT (Polkadot)", "DOGE (Dogecoin)",
            "XRP (Ripple)", "LTC (Litecoin)", "LINK (Chainlink)",
            "UNI (Uniswap)", "BNB (Binance Coin)", "MATIC (Polygon)",
            "AVAX (Avalanche)", "SHIB (Shiba Inu)"
    };

    public CryptoComparerGUI() {
        service = new PriceComparisonService();
        setupUI();
    }

    private void setupUI() {
        setTitle("Crypto Price Comparator");
        setSize(500, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Create components
        JLabel titleLabel = new JLabel("Crypto Price Comparator", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));

        JLabel selectLabel = new JLabel("Select Cryptocurrency:");
        cryptoSelector = new JComboBox<>(cryptoOptions);
        fetchButton = new JButton("Fetch Prices");
        resultArea = new JTextArea();
        resultArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(resultArea);
        statusLabel = new JLabel("Ready", JLabel.CENTER);

        // Layout
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        headerPanel = new JPanel(new BorderLayout()); // Initialize headerPanel
        headerPanel.add(titleLabel, BorderLayout.NORTH);

        JPanel selectionPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        selectionPanel.add(selectLabel);
        selectionPanel.add(cryptoSelector);
        selectionPanel.add(fetchButton);
        JButton historyButton = new JButton("Show History");
        historyButton.addActionListener(e -> {
            String selectedOption = (String) cryptoSelector.getSelectedItem();
            if (selectedOption != null) {
                String symbol = selectedOption.split(" ")[0];
                showPriceHistory(symbol);
            }
        });
        selectionPanel.add(historyButton);
        headerPanel.add(selectionPanel, BorderLayout.SOUTH);

        setupAlertPanel();

        JPanel refreshPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        refreshPanel.add(new JLabel("Auto-refresh: "));

        String[] refreshOptions = {"Off", "30 seconds", "1 minute", "5 minutes"};
        refreshIntervalSelector = new JComboBox<>(refreshOptions);
        refreshPanel.add(refreshIntervalSelector);

        refreshIntervalSelector.addActionListener(e -> setRefreshInterval());

        selectionPanel.add(refreshPanel);

        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(statusLabel, BorderLayout.SOUTH);

        // Add action listener
        fetchButton.addActionListener(e -> fetchPrices());

        add(mainPanel);
        setVisible(true);
    }

    private void setupAlertPanel() {
        JPanel alertPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        alertPanel.add(new JLabel("Set Price Alert: $"));
        
        alertPriceField = new JTextField(8);
        alertPanel.add(alertPriceField);
        
        setAlertButton = new JButton("Set Alert");
        setAlertButton.addActionListener(e -> setAlert());
        alertPanel.add(setAlertButton);
        
        // Add the alert panel below the selection panel
        headerPanel.add(alertPanel, BorderLayout.CENTER);
        
        // Setup timer to check alerts every minute
        alertCheckTimer = new Timer(60000, e -> checkAlerts());
        alertCheckTimer.start();
    }

    private void fetchPrices() {
        String selectedOption = (String) cryptoSelector.getSelectedItem();
        if (selectedOption == null) return;
        
        // Extract symbol from the option (e.g., "BTC (Bitcoin)" -> "BTC")
        String symbol = selectedOption.split(" ")[0];
        
        statusLabel.setText("Fetching prices for " + symbol + "...");
        resultArea.setText("");
        
        // Use SwingWorker to prevent UI freezing
        SwingWorker<String, Void> worker = new SwingWorker<>() {
            @Override
            protected String doInBackground() {
                try {
                    Map<String, Double> prices = service.comparePrices(symbol);
                    
                    // Sort exchanges by name
                    Map<String, Double> sortedPrices = new TreeMap<>(prices);
                    
                    StringBuilder result = new StringBuilder();
                    result.append("Prices for ").append(symbol).append(":\n\n");
                    
                    for (Map.Entry<String, Double> entry : sortedPrices.entrySet()) {
                        result.append(entry.getKey())
                              .append(": $")
                              .append(String.format("%.2f", entry.getValue()))
                              .append("\n");
                    }
                    
                    result.append("\n");
                    
                    // Find best price
                    String bestExchange = service.findBestExchange(prices);
                    result.append("Best price available at: ").append(bestExchange);
                    
                    return result.toString();
                } catch (IOException e) {
                    return "Error fetching prices: " + e.getMessage();
                }
            }
            
            @Override
            protected void done() {
                try {
                    resultArea.setText(get());
                    statusLabel.setText("Ready");
                } catch (Exception e) {
                    resultArea.setText("An error occurred: " + e.getMessage());
                    statusLabel.setText("Error");
                }
            }
        };
        
        worker.execute();
    }

    private void showPriceHistory(String symbol) {
        JFrame chartFrame = new JFrame("Price History for " + symbol);
        chartFrame.setSize(800, 400);
        chartFrame.setLocationRelativeTo(this);
        
        // Create a worker to fetch historical data
        SwingWorker<ChartPanel, Void> worker = new SwingWorker<>() {
            @Override
            protected ChartPanel doInBackground() throws Exception {
                // Get historical data (implement this in PriceComparisonService)
                Map<Date, Double> historicalData = service.getHistoricalPrices(symbol, 30); // Last 30 days
                
                // Create dataset
                TimeSeries series = new TimeSeries(symbol + " Price");
                for (Map.Entry<Date, Double> entry : historicalData.entrySet()) {
                    series.add(new Day(entry.getKey()), entry.getValue());
                }
                TimeSeriesCollection dataset = new TimeSeriesCollection(series);
                
                // Create chart
                JFreeChart chart = ChartFactory.createTimeSeriesChart(
                        symbol + " Price History",
                        "Date",
                        "Price (USD)",
                        dataset,
                        true,
                        true,
                        false
                );
                
                return new ChartPanel(chart);
            }
            
            @Override
            protected void done() {
                try {
                    chartFrame.add(get());
                    chartFrame.setVisible(true);
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(CryptoComparerGUI.this, 
                            "Error loading chart: " + e.getMessage(),
                            "Chart Error", 
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        
        worker.execute();
    }

    private void setAlert() {
        try {
            String selectedOption = (String) cryptoSelector.getSelectedItem();
            if (selectedOption == null) return;
            
            String symbol = selectedOption.split(" ")[0];
            double alertPrice = Double.parseDouble(alertPriceField.getText().trim());
            
            alertPrices.put(symbol, alertPrice);
            JOptionPane.showMessageDialog(this, 
                    "Alert set for " + symbol + " at $" + alertPrice,
                    "Alert Set", 
                    JOptionPane.INFORMATION_MESSAGE);
            
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, 
                    "Please enter a valid price",
                    "Invalid Input", 
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void checkAlerts() {
        for (Map.Entry<String, Double> alert : alertPrices.entrySet()) {
            try {
                String symbol = alert.getKey();
                double targetPrice = alert.getValue();
                
                Map<String, Double> prices = service.comparePrices(symbol);
                double bestPrice = prices.values().stream()
                        .min(Double::compare)
                        .orElse(Double.MAX_VALUE);
                
                if (bestPrice <= targetPrice) {
                    // Alert triggered
                    JOptionPane.showMessageDialog(this, 
                            symbol + " price alert triggered!\nCurrent price: $" + bestPrice,
                            "Price Alert", 
                            JOptionPane.WARNING_MESSAGE);
                    
                    // Remove this alert
                    alertPrices.remove(symbol);
                    break; // To avoid multiple dialogs at once
                }
            } catch (Exception e) {
                // Silently ignore errors during background checks
            }
        }
    }

    private void setRefreshInterval() {
        if (autoRefreshTimer != null) {
            autoRefreshTimer.stop();
            autoRefreshTimer = null;
        }
        
        String selected = (String) refreshIntervalSelector.getSelectedItem();
        if (selected == null || selected.equals("Off")) {
            return;
        }
        
        int interval;
        switch (selected) {
            case "30 seconds" -> interval = 30000;
            case "1 minute" -> interval = 60000;
            case "5 minutes" -> interval = 300000;
            default -> interval = 0;
        }
        
        if (interval > 0) {
            autoRefreshTimer = new Timer(interval, e -> fetchPrices());
            autoRefreshTimer.start();
        }
    }

    public static void main(String[] args) {
        // Set look and feel to system default
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // Start the GUI
        SwingUtilities.invokeLater(CryptoComparerGUI::new);
    }
}