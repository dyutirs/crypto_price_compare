package org.studyeasy;

import org.studyeasy.service.PriceComparisonService;

import java.io.IOException;
import java.util.Map;
import java.util.Scanner;

public class CryptoComparisonApp {
    public static void main(String[] args) {
        PriceComparisonService service = new PriceComparisonService();
        Scanner scanner = new Scanner(System.in);
        
        System.out.println("Crypto Price Comparator");
        System.out.println("Enter cryptocurrency symbol (e.g., BTC, ETH, SOL, ADA, DOT, DOGE, XRP, LTC, LINK, UNI, BNB, MATIC, AVAX, SHIB): ");
        String symbol = scanner.nextLine().trim();
        
        try {
            System.out.println("Fetching prices for " + symbol + "...");
            Map<String, Double> prices = service.comparePrices(symbol);
            
            System.out.println("\nPrices:");
            prices.forEach((exchange, price) -> 
                System.out.printf("%s: $%.2f\n", exchange, price));
            
            String bestExchange = service.findBestExchange(prices);
            System.out.println("\nBest price available at: " + bestExchange);
            
        } catch (IOException e) {
            System.err.println("Error fetching prices: " + e.getMessage());
            e.printStackTrace();
        }
        
        scanner.close();
    }
}