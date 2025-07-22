package Code;
import java.io.*;
import java.util.*;

class Stock {
    private String symbol;
    private double price;
    private String name;

    public Stock(String symbol, String name, double price) {
        this.symbol = symbol;
        this.name = name;
        this.price = price;
    }

    public String getSymbol() { return symbol; }
    public String getName() { return name; }
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
}

class Portfolio {
    private String userName;
    private double balance;
    private Map<String, Integer> holdings; // symbol -> quantity

    public Portfolio(String userName, double balance) {
        this.userName = userName;
        this.balance = balance;
        this.holdings = new HashMap<>();
    }

    public double getBalance() { return balance; }
    public Map<String, Integer> getHoldings() { return holdings; }

    public boolean buyStock(Stock stock, int quantity) {
        double cost = stock.getPrice() * quantity;
        if (cost <= balance) {
            balance -= cost;
            holdings.put(stock.getSymbol(), holdings.getOrDefault(stock.getSymbol(), 0) + quantity);
            return true;
        }
        return false;
    }

    public boolean sellStock(Stock stock, int quantity) {
        int currentQuantity = holdings.getOrDefault(stock.getSymbol(), 0);
        if (quantity <= currentQuantity) {
            balance += stock.getPrice() * quantity;
            holdings.put(stock.getSymbol(), currentQuantity - quantity);
            if (holdings.get(stock.getSymbol()) == 0) {
                holdings.remove(stock.getSymbol());
            }
            return true;
        }
        return false;
    }

    public double calculatePortfolioValue(Map<String, Stock> market) {
        double value = balance;
        for (Map.Entry<String, Integer> entry : holdings.entrySet()) {
            Stock stock = market.get(entry.getKey());
            if (stock != null) {
                value += stock.getPrice() * entry.getValue();
            }
        }
        return value;
    }

    public void saveToFile() {
        try (PrintWriter writer = new PrintWriter("portfolio_" + userName + ".txt")) {
            writer.println(balance);
            for (Map.Entry<String, Integer> entry : holdings.entrySet()) {
                writer.println(entry.getKey() + "," + entry.getValue());
            }
        } catch (IOException e) {
            System.out.println("Error saving portfolio: " + e.getMessage());
        }
    }

    public static Portfolio loadFromFile(String userName) {
        Portfolio portfolio = new Portfolio(userName, 10000.0); // Default balance
        try (Scanner scanner = new Scanner(new File("portfolio_" + userName + ".txt"))) {
            if (scanner.hasNextDouble()) {
                portfolio.balance = scanner.nextDouble();
                scanner.nextLine();
            }
            while (scanner.hasNextLine()) {
                String[] parts = scanner.nextLine().split(",");
                if (parts.length == 2) {
                    portfolio.holdings.put(parts[0], Integer.parseInt(parts[1]));
                }
            }
        } catch (FileNotFoundException e) {
            System.out.println("No existing portfolio found. Starting fresh.");
        }
        return portfolio;
    }
}

public class StockTradingPlatform {
    private static Map<String, Stock> market = new HashMap<>();
    private static Portfolio portfolio;
    private static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        initializeMarket();
        System.out.print("Enter your name: ");
        String userName = scanner.nextLine();
        portfolio = Portfolio.loadFromFile(userName);

        while (true) {
            System.out.println("\n=== Stock Trading Platform ===");
            System.out.println("1. Display Market Data");
            System.out.println("2. Buy Stock");
            System.out.println("3. Sell Stock");
            System.out.println("4. View Portfolio");
            System.out.println("5. Exit");
            System.out.print("Choose an option: ");

            int choice = scanner.nextInt();
            scanner.nextLine(); // Clear buffer

            if (choice == 1) {
                displayMarketData();
            } else if (choice == 2) {
                buyStock();
            } else if (choice == 3) {
                sellStock();
            } else if (choice == 4) {
                displayPortfolio();
            } else if (choice == 5) {
                portfolio.saveToFile();
                System.out.println("Portfolio saved. Exiting...");
                break;
            } else {
                System.out.println("Invalid option. Try again.");
            }
        }
        scanner.close();
    }
    private static void initializeMarket() {
        market.put("AAPL", new Stock("AAPL", "Apple Inc.", 150.0));
        market.put("GOOGL", new Stock("GOOGL", "Alphabet Inc.", 2800.0));
        market.put("TSLA", new Stock("TSLA", "Tesla Inc.", 700.0));
    }

    private static void displayMarketData() {
        System.out.println("\n=== Market Data ===");
        for (Stock stock : market.values()) {
            System.out.printf("%s (%s): $%.2f\n", stock.getSymbol(), stock.getName(), stock.getPrice());
        }
    }

    private static void buyStock() {
        System.out.print("Enter stock symbol (e.g., AAPL): ");
        String symbol = scanner.nextLine().toUpperCase();
        Stock stock = market.get(symbol);
        if (stock == null) {
            System.out.println("Invalid stock symbol.");
            return;
        }
        System.out.print("Enter quantity: ");
        int quantity = scanner.nextInt();
        if (quantity <= 0) {
            System.out.println("Invalid quantity.");
            return;
        }
        if (portfolio.buyStock(stock, quantity)) {
            System.out.println("Purchase successful!");
        } else {
            System.out.println("Insufficient funds.");
        }
    }

    private static void sellStock() {
        System.out.print("Enter stock symbol (e.g., AAPL): ");
        String symbol = scanner.nextLine().toUpperCase();
        Stock stock = market.get(symbol);
        if (stock == null) {
            System.out.println("Invalid stock symbol.");
            return;
        }
        System.out.print("Enter quantity: ");
        int quantity = scanner.nextInt();
        if (quantity <= 0) {
            System.out.println("Invalid quantity.");
            return;
        }
        if (portfolio.sellStock(stock, quantity)) {
            System.out.println("Sale successful!");
        } else {
            System.out.println("Insufficient shares.");
        }
    }

    private static void displayPortfolio() {
        System.out.println("\n=== Portfolio Summary ===");
        System.out.printf("Cash Balance: $%.2f\n", portfolio.getBalance());
        System.out.println("Holdings:");
        Map<String, Integer> holdings = portfolio.getHoldings();
        if (holdings.isEmpty()) {
            System.out.println("No stocks owned.");
        } else {
            for (Map.Entry<String, Integer> entry : holdings.entrySet()) {
                Stock stock = market.get(entry.getKey());
                if (stock != null) {
                    System.out.printf("%s: %d shares @ $%.2f\n", entry.getKey(), entry.getValue(), stock.getPrice());
                }
            }
        }
        System.out.printf("Total Portfolio Value: $%.2f\n", portfolio.calculatePortfolioValue(market));
    }
}


