# Crypto Price Comparator

A Java desktop application that compares cryptocurrency prices across multiple exchanges to help users find the best rates.

## Features

- **Price Comparison**: Fetch and compare cryptocurrency prices from multiple exchanges (Binance, CoinGecko, Kraken, Coinbase)
- **Best Price Finder**: Automatically identifies the exchange offering the best price
- **Price History Charts**: View historical price data for the last 30 days
- **Price Alerts**: Set price targets and receive notifications when cryptocurrencies reach your desired price
- **Auto-Refresh**: Configure automatic price updates at different intervals

## Supported Cryptocurrencies

- Bitcoin (BTC)
- Ethereum (ETH)
- Solana (SOL)
- Cardano (ADA)
- Polkadot (DOT)
- Dogecoin (DOGE)
- Ripple (XRP)
- Litecoin (LTC)
- Chainlink (LINK)
- Uniswap (UNI)
- Binance Coin (BNB)
- Polygon (MATIC)
- Avalanche (AVAX)
- Shiba Inu (SHIB)

## Requirements

- Java 17 or higher
- Maven for dependency management

## Dependencies

- Apache HttpClient 5.2.1 - For making API requests
- Jackson 2.15.2 - For JSON processing
- JFreeChart 1.5.4 - For rendering price history charts
- Java Swing - For the graphical user interface

## Installation

1. Clone the repository:
   ```
   git clone https://github.com/yourusername/crypto-price-comparer.git
   cd crypto-price-comparer
   ```

2. Build the project with Maven:
   ```
   mvn clean package
   ```

3. Run the application:
   ```
   java -jar target/crypto_price_comparer-1.0-SNAPSHOT.jar
   ```

## Usage

### Basic Price Comparison

1. Select a cryptocurrency from the dropdown menu
2. Click "Fetch Prices" to retrieve current prices from different exchanges
3. View the results in the text area, including the exchange offering the best price

### Setting Price Alerts

1. Select a cryptocurrency from the dropdown menu
2. Enter your target price in the "Set Price Alert" field
3. Click "Set Alert"
4. The application will notify you when the cryptocurrency reaches your target price

### Viewing Price History

1. Select a cryptocurrency from the dropdown menu
2. Click "Show History" to view a chart of the cryptocurrency's price over the last 30 days

### Auto-Refresh

1. Select your preferred refresh interval from the "Auto-refresh" dropdown
2. The application will automatically update prices at the selected interval

## Project Structure

- `src/main/java/org/studyeasy/CryptoComparisonApp.java` - Command-line interface
- `src/main/java/org/studyeasy/CryptoComparerGUI.java` - Graphical user interface
- `src/main/java/org/studyeasy/service/PriceComparisonService.java` - Core service for fetching and comparing prices

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Acknowledgments

- Data provided by Binance, CoinGecko, Kraken, and Coinbase public APIs
- Built with JFreeChart for data visualization
