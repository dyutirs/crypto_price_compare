package org.studyeasy.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.io.entity.EntityUtils;
// Remove the org.apache.hc.core5.http.ParseException import

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.text.ParseException; // Keep only this ParseException
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.concurrent.TimeUnit;

public class PriceComparisonService {
    private final ObjectMapper mapper = new ObjectMapper();
    
    public Map<String, Double> comparePrices(String cryptoSymbol) throws IOException {
        Map<String, Double> prices = new HashMap<>();
        
        try {
            // Get Binance price
            prices.put("Binance", getBinancePrice(cryptoSymbol));
        } catch (Exception e) {
            System.err.println("Error fetching from Binance: " + e.getMessage());
        }
        
        try {
            // Get CoinGecko price
            prices.put("CoinGecko", getCoinGeckoPrice(cryptoSymbol));
        } catch (Exception e) {
            System.err.println("Error fetching from CoinGecko: " + e.getMessage());
        }
        
        try {
            // Get Kraken price
            prices.put("Kraken", getKrakenPrice(cryptoSymbol));
        } catch (Exception e) {
            System.err.println("Error fetching from Kraken: " + e.getMessage());
        }
        
        try {
            // Get Coinbase price
            prices.put("Coinbase", getCoinbasePrice(cryptoSymbol));
        } catch (Exception e) {
            System.err.println("Error fetching from Coinbase: " + e.getMessage());
        }
        
        return prices;
    }
    
    private Double getBinancePrice(String symbol) throws IOException {
        String url = "https://api.binance.com/api/v3/ticker/price?symbol=" + symbol.toUpperCase() + "USDT";
        String response = executeHttpGet(url);
        JsonNode root = mapper.readTree(response);
        return root.get("price").asDouble();
    }
    
    private Double getCoinGeckoPrice(String symbol) throws IOException {
        // Convert common symbols to CoinGecko IDs
        String coinId = convertToCoinGeckoId(symbol);
        String url = "https://api.coingecko.com/api/v3/simple/price?ids=" + coinId + "&vs_currencies=usd";
        String response = executeHttpGet(url);
        JsonNode root = mapper.readTree(response);
        return root.get(coinId).get("usd").asDouble();
    }
    
    private String convertToCoinGeckoId(String symbol) {
        // Expanded mapping for common cryptocurrencies
        return switch (symbol.toLowerCase()) {
            case "btc" -> "bitcoin";
            case "eth" -> "ethereum";
            case "sol" -> "solana";
            case "ada" -> "cardano";
            case "dot" -> "polkadot";
            case "doge" -> "dogecoin";
            case "xrp" -> "ripple";
            case "ltc" -> "litecoin";
            case "link" -> "chainlink";
            case "uni" -> "uniswap";
            case "bnb" -> "binancecoin";
            case "matic", "poly" -> "polygon";
            case "avax" -> "avalanche-2";
            case "shib" -> "shiba-inu";
            // Add more mappings as needed
            default -> symbol.toLowerCase();
        };
    }
    
    private String executeHttpGet(String url) throws IOException {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpGet request = new HttpGet(url);
            try (CloseableHttpResponse response = client.execute(request)) {
                try {
                    return EntityUtils.toString(response.getEntity());
                } catch (org.apache.hc.core5.http.ParseException e) {
                    throw new IOException("Failed to parse HTTP response", e);
                }
            }
        }
    }
    
    public String findBestExchange(Map<String, Double> prices) {
        return prices.entrySet().stream()
                .min(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("Unknown");
    }

    private Double getKrakenPrice(String symbol) throws IOException {
        // Fix Kraken symbol format
        String krakenSymbol = convertToKrakenSymbol(symbol);
        String url = "https://api.kraken.com/0/public/Ticker?pair=" + krakenSymbol + "USD";
        String response = executeHttpGet(url);
        JsonNode root = mapper.readTree(response);
        
        // Check for errors
        if (root.has("error") && root.get("error").size() > 0) {
            throw new IOException("Kraken API error: " + root.get("error").toString());
        }
        
        // Handle Kraken's specific response format
        JsonNode result = root.get("result");
        if (result == null || result.isEmpty()) {
            throw new IOException("No result data from Kraken API");
        }
        
        // Kraken uses different pair naming conventions
        String pairName = null;
        Iterator<String> fieldNames = result.fieldNames();
        if (fieldNames.hasNext()) {
            pairName = fieldNames.next();
        } else {
            throw new IOException("No trading pair found in Kraken response");
        }
        
        return result.get(pairName).get("c").get(0).asDouble();
    }

    private String convertToKrakenSymbol(String symbol) {
        // Kraken has specific symbol formats
        return switch (symbol.toUpperCase()) {
            case "BTC" -> "XBT";  // Kraken uses XBT instead of BTC
            case "DOGE" -> "XDG"; // Kraken uses XDG for Dogecoin
            case "ADA" -> "ADA";
            case "ETH" -> "ETH";
            case "SOL" -> "SOL";
            case "DOT" -> "DOT";
            case "XRP" -> "XRP";
            case "LTC" -> "LTC";
            case "LINK" -> "LINK";
            case "UNI" -> "UNI";
            case "MATIC" -> "MATIC";
            case "AVAX" -> "AVAX";
            default -> symbol.toUpperCase();
        };
    }

    private Double getCoinbasePrice(String symbol) throws IOException {
        String url = "https://api.coinbase.com/v2/prices/" + symbol.toUpperCase() + "-USD/spot";
        String response = executeHttpGet(url);
        JsonNode root = mapper.readTree(response);
        return root.get("data").get("amount").asDouble();
    }

    public Map<Date, Double> getHistoricalPrices(String symbol, int days) throws IOException {
        // Use CoinGecko for historical data
        String coinId = convertToCoinGeckoId(symbol);
        long endTime = System.currentTimeMillis() / 1000;
        long startTime = endTime - TimeUnit.DAYS.toSeconds(days);
        
        String url = "https://api.coingecko.com/api/v3/coins/" + coinId + 
                "/market_chart/range?vs_currency=usd&from=" + startTime + "&to=" + endTime;
        
        String response = executeHttpGet(url);
        JsonNode root = mapper.readTree(response);
        JsonNode prices = root.get("prices");
        
        Map<Date, Double> historicalData = new LinkedHashMap<>();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        
        for (JsonNode pricePoint : prices) {
            try {
                long timestamp = pricePoint.get(0).asLong();
                double price = pricePoint.get(1).asDouble();
                
                Date date = new Date(timestamp);
                String dateKey = dateFormat.format(date);
                
                // Group by day (take the last price of each day)
                historicalData.put(dateFormat.parse(dateKey), price);
            } catch (ParseException e) {
                // Log the error but continue processing other data points
                System.err.println("Error parsing date: " + e.getMessage());
            }
        }
        
        return historicalData;
    }
}
