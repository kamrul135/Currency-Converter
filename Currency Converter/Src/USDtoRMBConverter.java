import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Clipboard;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;
import java.util.concurrent.CompletableFuture;
import javax.swing.SwingWorker;

public class USDtoRMBConverter {
    
    // Exchange rates (in a real app, these would come from an API)
    private static final Map<String, Double> exchangeRates = new HashMap<>();
    private static ArrayList<String> conversionHistory = new ArrayList<>();
    private static boolean isDarkMode = true;
    private static LocalDateTime lastUpdated = LocalDateTime.now();
    private static JLabel timestampLabel;
    private static boolean isUpdatingRates = false;
    
    // Add favorite currency pairs feature
    private static final ArrayList<String> favoritePairs = new ArrayList<>();
    private static JComboBox<String> favoritesCombo;
    
    // Free API endpoint for exchange rates (using exchangerate-api.com)
    private static final String API_URL = "https://api.exchangerate-api.com/v4/latest/USD";
    
    // Initialize exchange rates with currencies from all continents
    static {
        // North America
        exchangeRates.put("USD", 1.0);      // US Dollar (Base)
        exchangeRates.put("CAD", 1.25);     // Canadian Dollar
        exchangeRates.put("MXN", 20.0);     // Mexican Peso
        
        // Europe
        exchangeRates.put("EUR", 0.85);     // Euro
        exchangeRates.put("GBP", 0.73);     // British Pound
        exchangeRates.put("CHF", 0.92);     // Swiss Franc
        exchangeRates.put("NOK", 8.5);      // Norwegian Krone
        exchangeRates.put("SEK", 8.8);      // Swedish Krona
        exchangeRates.put("DKK", 6.3);      // Danish Krone
        exchangeRates.put("PLN", 3.9);      // Polish Zloty
        exchangeRates.put("CZK", 21.5);     // Czech Koruna
        exchangeRates.put("HUF", 295.0);    // Hungarian Forint
        exchangeRates.put("RON", 4.2);      // Romanian Leu
        exchangeRates.put("RUB", 75.0);     // Russian Ruble
        
        // Asia
        exchangeRates.put("CNY", 6.88);     // Chinese Yuan
        exchangeRates.put("JPY", 110.0);    // Japanese Yen
        exchangeRates.put("KRW", 1180.0);   // South Korean Won
        exchangeRates.put("INR", 74.5);     // Indian Rupee
        exchangeRates.put("SGD", 1.35);     // Singapore Dollar
        exchangeRates.put("HKD", 7.8);      // Hong Kong Dollar
        exchangeRates.put("THB", 31.5);     // Thai Baht
        exchangeRates.put("MYR", 4.1);      // Malaysian Ringgit
        exchangeRates.put("IDR", 14250.0);  // Indonesian Rupiah
        exchangeRates.put("PHP", 50.5);     // Philippine Peso
        exchangeRates.put("VND", 23000.0);  // Vietnamese Dong
        exchangeRates.put("PKR", 155.0);    // Pakistani Rupee
        exchangeRates.put("BDT", 85.0);     // Bangladeshi Taka
        exchangeRates.put("LKR", 200.0);    // Sri Lankan Rupee
        exchangeRates.put("AED", 3.67);     // UAE Dirham
        exchangeRates.put("SAR", 3.75);     // Saudi Riyal
        exchangeRates.put("QAR", 3.64);     // Qatari Riyal
        exchangeRates.put("KWD", 0.30);     // Kuwaiti Dinar
        exchangeRates.put("BHD", 0.38);     // Bahraini Dinar
        exchangeRates.put("OMR", 0.38);     // Omani Rial
        exchangeRates.put("JOD", 0.71);     // Jordanian Dinar
        exchangeRates.put("ILS", 3.25);     // Israeli Shekel
        exchangeRates.put("TRY", 8.5);      // Turkish Lira
        
        // Africa
        exchangeRates.put("ZAR", 14.5);     // South African Rand
        exchangeRates.put("EGP", 15.7);     // Egyptian Pound
        exchangeRates.put("NGN", 410.0);    // Nigerian Naira
        exchangeRates.put("KES", 108.0);    // Kenyan Shilling
        exchangeRates.put("GHS", 6.1);      // Ghanaian Cedi
        exchangeRates.put("UGX", 3550.0);   // Ugandan Shilling
        exchangeRates.put("TZS", 2310.0);   // Tanzanian Shilling
        exchangeRates.put("ETB", 44.0);     // Ethiopian Birr
        exchangeRates.put("MAD", 9.0);      // Moroccan Dirham
        exchangeRates.put("TND", 2.8);      // Tunisian Dinar
        exchangeRates.put("DZD", 135.0);    // Algerian Dinar
        exchangeRates.put("XOF", 555.0);    // West African CFA Franc
        exchangeRates.put("XAF", 555.0);    // Central African CFA Franc
        
        // South America
        exchangeRates.put("BRL", 5.2);      // Brazilian Real
        exchangeRates.put("ARS", 98.0);     // Argentine Peso
        exchangeRates.put("CLP", 750.0);    // Chilean Peso
        exchangeRates.put("COP", 3850.0);   // Colombian Peso
        exchangeRates.put("PEN", 3.9);      // Peruvian Sol
        exchangeRates.put("UYU", 43.5);     // Uruguayan Peso
        exchangeRates.put("PYG", 6850.0);   // Paraguayan Guarani
        exchangeRates.put("BOB", 6.9);      // Bolivian Boliviano
        exchangeRates.put("VES", 4.2);      // Venezuelan Bolívar
        exchangeRates.put("GYD", 209.0);    // Guyanese Dollar
        exchangeRates.put("SRD", 14.2);     // Surinamese Dollar
        
        // Oceania
        exchangeRates.put("AUD", 1.35);     // Australian Dollar
        exchangeRates.put("NZD", 1.42);     // New Zealand Dollar
        exchangeRates.put("FJD", 2.1);      // Fijian Dollar
        exchangeRates.put("PGK", 3.5);      // Papua New Guinea Kina
        exchangeRates.put("TOP", 2.3);      // Tongan Pa'anga
        exchangeRates.put("WST", 2.6);      // Samoan Tala
        exchangeRates.put("VUV", 112.0);    // Vanuatu Vatu
        exchangeRates.put("SBD", 8.0);      // Solomon Islands Dollar
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> createAndShowGUI());
    }
    
    private static void createAndShowGUI() {
        // Create the main frame
        JFrame frame = new JFrame("Currency Converter");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(550, 650);
        frame.getContentPane().setBackground(new Color(30, 40, 60)); // Dark blue-gray background

        // Create components with currencies organized by continent
        String[] currencies = {
            // North America
            "USD $ - US Dollar", 
            "CAD $ - Canadian Dollar",
            "MXN $ - Mexican Peso",
            
            // Europe
            "EUR € - Euro",
            "GBP £ - British Pound",
            "CHF - Swiss Franc",
            "NOK - Norwegian Krone",
            "SEK - Swedish Krona", 
            "DKK - Danish Krone",
            "PLN - Polish Zloty",
            "CZK - Czech Koruna",
            "HUF - Hungarian Forint",
            "RON - Romanian Leu",
            "RUB ₽ - Russian Ruble",
            
            // Asia
            "CNY ¥ - Chinese Yuan",
            "JPY ¥ - Japanese Yen",
            "KRW ₩ - South Korean Won",
            "INR ₹ - Indian Rupee",
            "SGD $ - Singapore Dollar",
            "HKD $ - Hong Kong Dollar",
            "THB ฿ - Thai Baht",
            "MYR - Malaysian Ringgit",
            "IDR - Indonesian Rupiah",
            "PHP ₱ - Philippine Peso",
            "VND ₫ - Vietnamese Dong",
            "PKR ₨ - Pakistani Rupee",
            "BDT ৳ - Bangladeshi Taka",
            "LKR ₨ - Sri Lankan Rupee",
            "AED - UAE Dirham",
            "SAR ﷼ - Saudi Riyal",
            "QAR ﷼ - Qatari Riyal",
            "KWD - Kuwaiti Dinar",
            "BHD - Bahraini Dinar",
            "OMR ﷼ - Omani Rial",
            "JOD - Jordanian Dinar",
            "ILS ₪ - Israeli Shekel",
            "TRY ₺ - Turkish Lira",
            
            // Africa
            "ZAR - South African Rand",
            "EGP £ - Egyptian Pound",
            "NGN ₦ - Nigerian Naira",
            "KES - Kenyan Shilling",
            "GHS ₵ - Ghanaian Cedi",
            "UGX - Ugandan Shilling",
            "TZS - Tanzanian Shilling",
            "ETB - Ethiopian Birr",
            "MAD - Moroccan Dirham",
            "TND - Tunisian Dinar",
            "DZD - Algerian Dinar",
            "XOF - West African CFA Franc",
            "XAF - Central African CFA Franc",
            
            // South America
            "BRL R$ - Brazilian Real",
            "ARS $ - Argentine Peso",
            "CLP $ - Chilean Peso",
            "COP $ - Colombian Peso",
            "PEN - Peruvian Sol",
            "UYU $ - Uruguayan Peso",
            "PYG ₲ - Paraguayan Guarani",
            "BOB - Bolivian Boliviano",
            "VES - Venezuelan Bolívar",
            "GYD $ - Guyanese Dollar",
            "SRD $ - Surinamese Dollar",
            
            // Oceania
            "AUD $ - Australian Dollar",
            "NZD $ - New Zealand Dollar",
            "FJD $ - Fijian Dollar",
            "PGK - Papua New Guinea Kina",
            "TOP - Tongan Pa'anga",
            "WST - Samoan Tala",
            "VUV - Vanuatu Vatu",
            "SBD $ - Solomon Islands Dollar"
        };
        JComboBox<String> fromCurrency = new JComboBox<>(currencies);
        JComboBox<String> toCurrency = new JComboBox<>(currencies);

        // Set default selection: USD to CNY
        fromCurrency.setSelectedIndex(0); // USD
        toCurrency.setSelectedIndex(1);   // CNY

        JTextField amountField = new JTextField(10);
        JTextField resultField = new JTextField(10);
        resultField.setEditable(false);
        
        // New components
        JTextArea historyArea = new JTextArea(5, 20);
        historyArea.setEditable(false);
        historyArea.setBackground(new Color(45, 55, 75));
        historyArea.setForeground(Color.WHITE);
        JScrollPane historyScroll = new JScrollPane(historyArea);
        
        JLabel rateLabel = new JLabel("Exchange Rate: ");
        rateLabel.setForeground(Color.WHITE);
        
        JLabel timestampLabel = new JLabel("Last Updated: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
        timestampLabel.setForeground(Color.LIGHT_GRAY);
        timestampLabel.setFont(new Font("Arial", Font.ITALIC, 10));
        
        // Store reference for updates
        USDtoRMBConverter.timestampLabel = timestampLabel;

        JButton switchButton = new JButton("⇄");
        switchButton.setBackground(new Color(70, 130, 180));  // Steel Blue
        switchButton.setForeground(Color.WHITE);
        switchButton.setFocusPainted(false);
        
        JButton convertButton = new JButton("Convert");
        convertButton.setBackground(new Color(60, 179, 113));  // Medium Sea Green
        convertButton.setForeground(Color.WHITE);
        convertButton.setFocusPainted(false);
        
        // New buttons
        JButton copyButton = new JButton("Copy Result");
        copyButton.setBackground(new Color(255, 140, 0));  // Dark Orange
        copyButton.setForeground(Color.WHITE);
        copyButton.setFocusPainted(false);
        
        JButton clearButton = new JButton("Clear");
        clearButton.setBackground(new Color(220, 20, 60));  // Crimson
        clearButton.setForeground(Color.WHITE);
        clearButton.setFocusPainted(false);
        
        JButton themeButton = new JButton("🌙 Dark");
        themeButton.setBackground(new Color(75, 0, 130));  // Indigo
        themeButton.setForeground(Color.WHITE);
        themeButton.setFocusPainted(false);
        
        JButton historyButton = new JButton("History");
        historyButton.setBackground(new Color(139, 69, 19));  // Saddle Brown
        historyButton.setForeground(Color.WHITE);
        historyButton.setFocusPainted(false);
        
        JButton refreshButton = new JButton("🔄 Refresh Rates");
        refreshButton.setBackground(new Color(32, 178, 170));  // Light Sea Green
        refreshButton.setForeground(Color.WHITE);
        refreshButton.setFocusPainted(false);

        // Set layout manager
        frame.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 10, 5, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Add title at the top
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 3;
        JLabel titleLabel = new JLabel("Currency Converter", SwingConstants.CENTER);
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        frame.add(titleLabel, gbc);

        // Add timestamp
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 3;
        frame.add(timestampLabel, gbc);

        // Add components to frame
        gbc.gridwidth = 1; // Reset gridwidth
        gbc.gridx = 0;
        gbc.gridy = 2;
        JLabel fromLabel = new JLabel("From:");
        fromLabel.setForeground(Color.WHITE);
        frame.add(fromLabel, gbc);

        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        frame.add(fromCurrency, gbc);

        gbc.gridwidth = 1;
        gbc.gridx = 0;
        gbc.gridy = 3;
        JLabel toLabel = new JLabel("To:");
        toLabel.setForeground(Color.WHITE);
        frame.add(toLabel, gbc);

        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        frame.add(toCurrency, gbc);

        gbc.gridwidth = 1;
        gbc.gridx = 0;
        gbc.gridy = 4;
        JLabel amountLabel = new JLabel("Amount:");
        amountLabel.setForeground(Color.WHITE);
        frame.add(amountLabel, gbc);

        gbc.gridx = 1;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        frame.add(amountField, gbc);

        gbc.gridwidth = 1;
        gbc.gridx = 0;
        gbc.gridy = 5;
        JLabel resultLabel = new JLabel("Result:");
        resultLabel.setForeground(Color.WHITE);
        frame.add(resultLabel, gbc);

        gbc.gridx = 1;
        gbc.gridy = 5;
        gbc.gridwidth = 2;
        frame.add(resultField, gbc);

        // Exchange rate display
        gbc.gridwidth = 3;
        gbc.gridx = 0;
        gbc.gridy = 6;
        frame.add(rateLabel, gbc);

        // Buttons row 1
        gbc.gridwidth = 1;
        gbc.gridx = 0;
        gbc.gridy = 7;
        frame.add(switchButton, gbc);

        gbc.gridx = 1;
        gbc.gridy = 7;
        frame.add(convertButton, gbc);

        gbc.gridx = 2;
        gbc.gridy = 7;
        frame.add(copyButton, gbc);

        // Buttons row 2
        gbc.gridx = 0;
        gbc.gridy = 8;
        frame.add(clearButton, gbc);

        gbc.gridx = 1;
        gbc.gridy = 8;
        frame.add(themeButton, gbc);

        gbc.gridx = 2;
        gbc.gridy = 8;
        frame.add(historyButton, gbc);

        // Buttons row 3
        gbc.gridx = 0;
        gbc.gridy = 9;
        gbc.gridwidth = 3;
        frame.add(refreshButton, gbc);

        // Create calculator number pad
        JPanel calculatorPanel = createCalculatorPanel(amountField);
        calculatorPanel.setBackground(new Color(30, 40, 60));
        
        // Add calculator panel to the layout
        gbc.gridx = 0;
        gbc.gridy = 10;
        gbc.gridwidth = 3;
        gbc.fill = GridBagConstraints.BOTH;
        frame.add(calculatorPanel, gbc);
        
        // Add favorites panel
        JPanel favsPanel = createFavoritesPanel(fromCurrency, toCurrency);
        gbc.gridx = 0;
        gbc.gridy = 11;
        gbc.gridwidth = 3;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        frame.add(favsPanel, gbc);
        
        // Add quick amounts panel
        JPanel quickAmountsPanel = createQuickAmountsPanel(amountField, resultField, fromCurrency, toCurrency);
        gbc.gridx = 0;
        gbc.gridy = 12;
        gbc.gridwidth = 3;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        frame.add(quickAmountsPanel, gbc);

        // Add event handlers
        
        // Update exchange rate display when currencies change
        ActionListener updateRateDisplay = e -> updateExchangeRateDisplay(fromCurrency, toCurrency, rateLabel);
        fromCurrency.addActionListener(updateRateDisplay);
        toCurrency.addActionListener(updateRateDisplay);
        
        // Initial rate display update
        updateExchangeRateDisplay(fromCurrency, toCurrency, rateLabel);
        
        // Enter key support for amount field
        amountField.addActionListener(e -> performConversion(amountField, resultField, fromCurrency, toCurrency));
        
        // Switch button
        switchButton.addActionListener(e -> {
            int fromIndex = fromCurrency.getSelectedIndex();
            int toIndex = toCurrency.getSelectedIndex();
            fromCurrency.setSelectedIndex(toIndex);
            toCurrency.setSelectedIndex(fromIndex);
        });

        // Convert button
        convertButton.addActionListener(e -> performConversion(amountField, resultField, fromCurrency, toCurrency));
        
        // Copy button
        copyButton.addActionListener(e -> {
            String result = resultField.getText();
            if (!result.isEmpty()) {
                StringSelection selection = new StringSelection(result);
                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                clipboard.setContents(selection, null);
                JOptionPane.showMessageDialog(frame, "Result copied to clipboard!", "Copied", JOptionPane.INFORMATION_MESSAGE);
            }
        });
        
        // Clear button
        clearButton.addActionListener(e -> {
            amountField.setText("");
            resultField.setText("");
        });
        
        // Theme toggle button
        themeButton.addActionListener(e -> toggleTheme(frame, themeButton, timestampLabel, rateLabel));
        
        // History button
        historyButton.addActionListener(e -> showEnhancedHistoryDialog(frame));
        
        // Refresh rates button
        refreshButton.addActionListener(e -> fetchExchangeRates(frame, fromCurrency, toCurrency, rateLabel));
        
        // Favorites combo box selection
        favoritesCombo.addActionListener(e -> {
            String selected = (String) favoritesCombo.getSelectedItem();
            if (selected != null && !selected.equals("Select Favorite...")) {
                String[] parts = selected.split(" → ");
                if (parts.length == 2) {
                    // Find and select the currencies
                    selectCurrencyByCode(fromCurrency, parts[0]);
                    selectCurrencyByCode(toCurrency, parts[1]);
                }
            }
        });

        // Initial load of exchange rates
        fetchExchangeRates(frame, fromCurrency, toCurrency, rateLabel);

        // Display the frame
        frame.setLocationRelativeTo(null); // Center the window
        frame.setVisible(true);
    }
    
    private static void updateExchangeRateDisplay(JComboBox<String> fromCurrency, JComboBox<String> toCurrency, JLabel rateLabel) {
        String fromCode = getCurrencyCode((String) fromCurrency.getSelectedItem());
        String toCode = getCurrencyCode((String) toCurrency.getSelectedItem());
        
        if (!fromCode.equals(toCode)) {
            double fromRate = exchangeRates.get(fromCode);
            double toRate = exchangeRates.get(toCode);
            double exchangeRate = toRate / fromRate;
            rateLabel.setText(String.format("Exchange Rate: 1 %s = %.4f %s", fromCode, exchangeRate, toCode));
        } else {
            rateLabel.setText("Exchange Rate: 1:1 (Same Currency)");
        }
    }
    
    private static String getCurrencyCode(String currencyString) {
        return currencyString.substring(0, 3);
    }
    
    private static void performConversion(JTextField amountField, JTextField resultField, JComboBox<String> fromCurrency, JComboBox<String> toCurrency) {
        try {
            double amount = Double.parseDouble(amountField.getText());
            if (amount < 0) {
                throw new NumberFormatException("Negative amount");
            }
            
            String fromCode = getCurrencyCode((String) fromCurrency.getSelectedItem());
            String toCode = getCurrencyCode((String) toCurrency.getSelectedItem());
            
            double result;
            if (fromCode.equals(toCode)) {
                result = amount;
            } else {
                double fromRate = exchangeRates.get(fromCode);
                double toRate = exchangeRates.get(toCode);
                result = amount * (toRate / fromRate);
            }
            
            resultField.setText(String.format("%.2f", result));
            
            // Add to history
            String historyEntry = String.format("%s: %.2f %s = %.2f %s", 
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")),
                amount, fromCode, result, toCode);
            conversionHistory.add(0, historyEntry); // Add to beginning
            
            // Keep only last 10 conversions
            if (conversionHistory.size() > 10) {
                conversionHistory.remove(conversionHistory.size() - 1);
            }
            
        } catch (NumberFormatException ex) {
            JFrame parentFrame = (JFrame) SwingUtilities.getWindowAncestor(amountField);
            JOptionPane.showMessageDialog(parentFrame, 
                "Please enter a valid positive number!", 
                "Invalid Input", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private static void toggleTheme(JFrame frame, JButton themeButton, JLabel timestampLabel, JLabel rateLabel) {
        isDarkMode = !isDarkMode;
        Color backgroundColor, textColor, lightTextColor;
        
        if (isDarkMode) {
            backgroundColor = new Color(30, 40, 60);
            textColor = Color.WHITE;
            lightTextColor = Color.LIGHT_GRAY;
            themeButton.setText("🌙 Dark");
        } else {
            backgroundColor = new Color(240, 248, 255);
            textColor = Color.BLACK;
            lightTextColor = Color.GRAY;
            themeButton.setText("☀ Light");
        }
        
        frame.getContentPane().setBackground(backgroundColor);
        
        // Update all labels
        Component[] components = frame.getContentPane().getComponents();
        for (Component comp : components) {
            if (comp instanceof JLabel) {
                if (comp == timestampLabel) {
                    comp.setForeground(lightTextColor);
                } else {
                    comp.setForeground(textColor);
                }
            }
        }
        
        frame.repaint();
    }
    
    private static void showEnhancedHistoryDialog(JFrame parent) {
        JDialog historyDialog = new JDialog(parent, "Enhanced Conversion History", true);
        historyDialog.setSize(700, 500);
        historyDialog.setLocationRelativeTo(parent);
        
        JTabbedPane tabbedPane = new JTabbedPane();
        
        // History Tab
        JPanel historyPanel = new JPanel(new BorderLayout());
        JTextArea historyArea = new JTextArea();
        historyArea.setEditable(false);
        historyArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        
        if (conversionHistory.isEmpty()) {
            historyArea.setText("No conversion history available.");
        } else {
            StringBuilder historyText = new StringBuilder("Recent Conversions:\n\n");
            for (int i = 0; i < Math.min(20, conversionHistory.size()); i++) {
                historyText.append((i + 1)).append(". ").append(conversionHistory.get(i)).append("\n");
            }
            historyArea.setText(historyText.toString());
        }
        
        JScrollPane scrollPane = new JScrollPane(historyArea);
        historyPanel.add(scrollPane, BorderLayout.CENTER);
        
        // Statistics Tab
        JPanel statsPanel = new JPanel(new BorderLayout());
        JTextArea statsArea = new JTextArea();
        statsArea.setEditable(false);
        statsArea.setFont(new Font("Arial", Font.PLAIN, 12));
        
        StringBuilder stats = new StringBuilder("Conversion Statistics:\n\n");
        stats.append("Total conversions: ").append(conversionHistory.size()).append("\n");
        
        // Count most used currencies
        Map<String, Integer> currencyCount = new HashMap<>();
        for (String entry : conversionHistory) {
            String[] parts = entry.split(" ");
            if (parts.length >= 5) {
                String fromCurrency = parts[2];
                String toCurrency = parts[5];
                currencyCount.put(fromCurrency, currencyCount.getOrDefault(fromCurrency, 0) + 1);
                currencyCount.put(toCurrency, currencyCount.getOrDefault(toCurrency, 0) + 1);
            }
        }
        
        stats.append("\nMost used currencies:\n");
        currencyCount.entrySet().stream()
            .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
            .limit(5)
            .forEach(entry -> stats.append("  ")
                .append(entry.getKey()).append(": ")
                .append(entry.getValue()).append(" times\n"));
        
        statsArea.setText(stats.toString());
        statsPanel.add(new JScrollPane(statsArea), BorderLayout.CENTER);
        
        tabbedPane.addTab("History", historyPanel);
        tabbedPane.addTab("Statistics", statsPanel);
        
        historyDialog.add(tabbedPane, BorderLayout.CENTER);
        
        JPanel buttonPanel = new JPanel();
        JButton closeButton = new JButton("Close");
        JButton exportButton = new JButton("Export to CSV");
        JButton clearHistoryButton = new JButton("Clear History");
        
        closeButton.addActionListener(e -> historyDialog.dispose());
        exportButton.addActionListener(e -> exportHistoryToCSV(parent));
        clearHistoryButton.addActionListener(e -> {
            int result = JOptionPane.showConfirmDialog(historyDialog, 
                "Are you sure you want to clear all history?", 
                "Confirm Clear", JOptionPane.YES_NO_OPTION);
            if (result == JOptionPane.YES_OPTION) {
                conversionHistory.clear();
                historyDialog.dispose();
                JOptionPane.showMessageDialog(parent, "History cleared successfully!");
            }
        });
        
        buttonPanel.add(exportButton);
        buttonPanel.add(clearHistoryButton);
        buttonPanel.add(closeButton);
        historyDialog.add(buttonPanel, BorderLayout.SOUTH);
        
        historyDialog.setVisible(true);
    }
    
    // Add CSV export functionality
    private static void exportHistoryToCSV(JFrame parent) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setSelectedFile(new java.io.File("currency_conversions.csv"));
        
        int result = fileChooser.showSaveDialog(parent);
        if (result == JFileChooser.APPROVE_OPTION) {
            try (java.io.PrintWriter writer = new java.io.PrintWriter(fileChooser.getSelectedFile())) {
                writer.println("Timestamp,Amount,From Currency,To Currency,Result,Exchange Rate");
                
                for (String entry : conversionHistory) {
                    // Parse the entry format: "HH:mm:ss: amount FROM = result TO"
                    String[] parts = entry.split(": ");
                    if (parts.length >= 2) {
                        String timestamp = parts[0];
                        String conversionPart = parts[1];
                        String[] conversionParts = conversionPart.split(" = ");
                        if (conversionParts.length == 2) {
                            String[] leftParts = conversionParts[0].split(" ");
                            String[] rightParts = conversionParts[1].split(" ");
                            
                            if (leftParts.length >= 2 && rightParts.length >= 2) {
                                String amount = leftParts[0];
                                String fromCurrency = leftParts[1];
                                String resultAmount = rightParts[0];
                                String toCurrency = rightParts[1];
                                
                                // Calculate exchange rate
                                try {
                                    double amountVal = Double.parseDouble(amount);
                                    double resultVal = Double.parseDouble(resultAmount);
                                    double rate = resultVal / amountVal;
                                    
                                    writer.printf("%s,%s,%s,%s,%s,%.6f%n", 
                                        timestamp, amount, fromCurrency, toCurrency, resultAmount, rate);
                                } catch (NumberFormatException e) {
                                    writer.printf("%s,%s,%s,%s,%s,%s%n", 
                                        timestamp, amount, fromCurrency, toCurrency, resultAmount, "N/A");
                                }
                            }
                        }
                    }
                }
                
                JOptionPane.showMessageDialog(parent, 
                    "History exported successfully to:\n" + fileChooser.getSelectedFile().getAbsolutePath(), 
                    "Export Successful", JOptionPane.INFORMATION_MESSAGE);
                    
            } catch (java.io.IOException e) {
                JOptionPane.showMessageDialog(parent, 
                    "Error exporting history:\n" + e.getMessage(), 
                    "Export Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private static void fetchExchangeRates(JFrame parent, JComboBox<String> fromCurrency, JComboBox<String> toCurrency, JLabel rateLabel) {
        if (isUpdatingRates) {
            JOptionPane.showMessageDialog(parent, "Already updating rates, please wait...", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        // Use SwingWorker to fetch rates in background
        SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                isUpdatingRates = true;
                
                // Update UI to show loading
                SwingUtilities.invokeLater(() -> {
                    timestampLabel.setText("Updating exchange rates...");
                });
                
                try {
                    HttpClient client = HttpClient.newHttpClient();
                    HttpRequest request = HttpRequest.newBuilder()
                            .uri(URI.create(API_URL))
                            .timeout(java.time.Duration.ofSeconds(10))
                            .build();
                    
                    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                    
                    if (response.statusCode() == 200) {
                        String jsonResponse = response.body();
                        parseAndUpdateRates(jsonResponse);
                        return true;
                    } else {
                        return false;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    return false;
                }
            }
            
            @Override
            protected void done() {
                isUpdatingRates = false;
                try {
                    boolean success = get();
                    if (success) {
                        lastUpdated = LocalDateTime.now();
                        timestampLabel.setText("Last Updated: " + lastUpdated.format(DateTimeFormatter.ofPattern("HH:mm:ss")));
                        updateExchangeRateDisplay(fromCurrency, toCurrency, rateLabel);
                        JOptionPane.showMessageDialog(parent, "Exchange rates updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        timestampLabel.setText("Failed to update rates - using cached data");
                        JOptionPane.showMessageDialog(parent, "Failed to fetch current exchange rates.\nUsing cached rates.", "Warning", JOptionPane.WARNING_MESSAGE);
                    }
                } catch (Exception e) {
                    timestampLabel.setText("Error updating rates - using cached data");
                    JOptionPane.showMessageDialog(parent, "Error occurred while fetching exchange rates:\n" + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        
        worker.execute();
    }
    
    private static void parseAndUpdateRates(String jsonResponse) {
        try {
            // Simple JSON parsing without external libraries
            // Looking for "rates":{"EUR":0.85,"GBP":0.73, etc.
            int ratesIndex = jsonResponse.indexOf("\"rates\":");
            if (ratesIndex == -1) return;
            
            int startBrace = jsonResponse.indexOf("{", ratesIndex);
            int endBrace = jsonResponse.indexOf("}", startBrace);
            if (startBrace == -1 || endBrace == -1) return;
            
            String ratesSection = jsonResponse.substring(startBrace + 1, endBrace);
            String[] ratePairs = ratesSection.split(",");
            
            // Update USD rate (base currency)
            exchangeRates.put("USD", 1.0);
            
            for (String pair : ratePairs) {
                String[] parts = pair.split(":");
                if (parts.length == 2) {
                    String currency = parts[0].trim().replace("\"", "");
                    try {
                        double rate = Double.parseDouble(parts[1].trim());
                        
                        // Only update currencies we support
                        if (exchangeRates.containsKey(currency)) {
                            exchangeRates.put(currency, rate);
                        }
                    } catch (NumberFormatException e) {
                        // Skip invalid rates
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            // If parsing fails, keep existing rates
        }
    }
    
    private static JPanel createCalculatorPanel(JTextField amountField) {
        JPanel panel = new JPanel(new GridLayout(4, 3, 2, 2));
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(Color.WHITE), 
            "Quick Input", 
            0, 0, new Font("Arial", Font.BOLD, 12), Color.WHITE));
        
        String[] buttons = {"7", "8", "9", "4", "5", "6", "1", "2", "3", ".", "0", "C"};
        
        for (String text : buttons) {
            JButton button = new JButton(text);
            button.setBackground(new Color(70, 80, 100));
            button.setForeground(Color.WHITE);
            button.setFocusPainted(false);
            button.setFont(new Font("Arial", Font.BOLD, 14));
            
            button.addActionListener(e -> {
                if (text.equals("C")) {
                    amountField.setText("");
                } else {
                    String current = amountField.getText();
                    if (text.equals(".") && current.contains(".")) {
                        return; // Don't add multiple decimal points
                    }
                    amountField.setText(current + text);
                }
            });
            
            panel.add(button);
        }
        
        return panel;
    }
    
    private static ActionListener createCalculatorButtonListener(String text, JTextField amountField) {
        return e -> {
            switch (text) {
                case "=":
                    // Evaluate expression
                    String expression = amountField.getText();
                    try {
                        double result = evaluateExpression(expression);
                        amountField.setText(String.valueOf(result));
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(amountField, "Invalid expression", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                    break;
                case "C":
                    // Clear
                    amountField.setText("");
                    break;
                default:
                    // Append button text to amount field
                    amountField.setText(amountField.getText() + text);
                    break;
            }
        };
    }
    
    private static double evaluateExpression(String expression) throws Exception {
        // Very simple expression evaluator (for demo purposes)
        // Supports +, -, *, / and parentheses
        // TODO: Replace with a proper math expression parser/evaluator
        String sanitized = expression.replaceAll("[^0-9+*/().-]", "");
        String[] tokens = sanitized.split("(?<=[-+*/()])|(?=[-+*/()])");
        
        // Convert to Reverse Polish Notation (RPN) using Shunting Yard algorithm
        ArrayList<String> rpn = new ArrayList<>();
        ArrayList<String> stack = new ArrayList<>();
        
        for (String token : tokens) {
            switch (token) {
                case "+":
                case "-":
                case "*":
                case "/":
                    while (!stack.isEmpty() && precedence(stack.get(stack.size() - 1)) >= precedence(token)) {
                        rpn.add(stack.remove(stack.size() - 1));
                    }
                    stack.add(token);
                    break;
                case "(":
                    stack.add(token);
                    break;
                case ")":
                    while (!stack.isEmpty() && !stack.get(stack.size() - 1).equals("(")) {
                        rpn.add(stack.remove(stack.size() - 1));
                    }
                    stack.remove(stack.size() - 1); // Remove "("
                    break;
                default:
                    // Number
                    rpn.add(token);
                    break;
            }
        }
        
        while (!stack.isEmpty()) {
            rpn.add(stack.remove(stack.size() - 1));
        }
        
        // Evaluate RPN
        stack.clear();
        for (String token : rpn) {
            switch (token) {
                case "+":
                case "-":
                case "*":
                case "/":
                    double b = Double.parseDouble(stack.remove(stack.size() - 1));
                    double a = Double.parseDouble(stack.remove(stack.size() - 1));
                    double result;
                    switch (token) {
                        case "+":
                            result = a + b;
                            break;
                        case "-":
                            result = a - b;
                            break;
                        case "*":
                            result = a * b;
                            break;
                        case "/":
                            result = a / b;
                            break;
                        default:
                            throw new Exception("Invalid operator");
                    }
                    stack.add(String.valueOf(result));
                    break;
                default:
                    // Number
                    stack.add(token);
                    break;
            }
        }
        
        return Double.parseDouble(stack.get(0));
    }
    
    private static int precedence(String operator) {
        switch (operator) {
            case "+":
            case "-":
                return 1;
            case "*":
            case "/":
                return 2;
            default:
                return 0;
        }
    }
    
    // Add favorites functionality
    private static void addToFavorites(String fromCurrency, String toCurrency) {
        String pair = fromCurrency + " → " + toCurrency;
        if (!favoritePairs.contains(pair)) {
            favoritePairs.add(pair);
            updateFavoritesCombo();
        }
    }
    
    private static void updateFavoritesCombo() {
        if (favoritesCombo != null) {
            favoritesCombo.removeAllItems();
            favoritesCombo.addItem("Select Favorite...");
            for (String pair : favoritePairs) {
                favoritesCombo.addItem(pair);
            }
        }
    }
    
    private static JPanel createFavoritesPanel(JComboBox<String> fromCurrency, JComboBox<String> toCurrency) {
        JPanel panel = new JPanel(new FlowLayout());
        panel.setBackground(new Color(30, 40, 60));
        
        JLabel favLabel = new JLabel("Favorites:");
        favLabel.setForeground(Color.WHITE);
        
        favoritesCombo = new JComboBox<>();
        favoritesCombo.addItem("Select Favorite...");
        
        JButton addFavButton = new JButton("⭐ Add");
        addFavButton.setBackground(new Color(255, 215, 0));
        addFavButton.setForeground(Color.BLACK);
        addFavButton.setFocusPainted(false);
        
        favoritesCombo.addActionListener(e -> {
            String selected = (String) favoritesCombo.getSelectedItem();
            if (selected != null && !selected.equals("Select Favorite...")) {
                String[] parts = selected.split(" → ");
                if (parts.length == 2) {
                    // Find and select the currencies
                    selectCurrencyByCode(fromCurrency, parts[0]);
                    selectCurrencyByCode(toCurrency, parts[1]);
                }
            }
        });
        
        addFavButton.addActionListener(e -> {
            String fromCode = getCurrencyCode((String) fromCurrency.getSelectedItem());
            String toCode = getCurrencyCode((String) toCurrency.getSelectedItem());
            addToFavorites(fromCode, toCode);
        });
        
        panel.add(favLabel);
        panel.add(favoritesCombo);
        panel.add(addFavButton);
        
        return panel;
    }
    
    private static void selectCurrencyByCode(JComboBox<String> combo, String code) {
        for (int i = 0; i < combo.getItemCount(); i++) {
            String item = combo.getItemAt(i);
            if (item.startsWith(code + " ")) {
                combo.setSelectedIndex(i);
                break;
            }
        }
    }
    
    // Add quick preset amounts
    private static JPanel createQuickAmountsPanel(JTextField amountField, JTextField resultField, JComboBox<String> fromCurrency, JComboBox<String> toCurrency) {
        JPanel panel = new JPanel(new FlowLayout());
        panel.setBackground(new Color(30, 40, 60));
        
        JLabel quickLabel = new JLabel("Quick:");
        quickLabel.setForeground(Color.WHITE);
        panel.add(quickLabel);
        
        String[] amounts = {"1", "10", "100", "1000"};
        for (String amount : amounts) {
            JButton button = new JButton(amount);
            button.setBackground(new Color(100, 120, 140));
            button.setForeground(Color.WHITE);
            button.setFocusPainted(false);
            button.setPreferredSize(new Dimension(50, 25));
            
            button.addActionListener(e -> {
                amountField.setText(amount);
                performConversion(amountField, resultField, fromCurrency, toCurrency);
            });
            
            panel.add(button);
        }
        
        return panel;
    }
}
