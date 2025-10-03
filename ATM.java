import java.io.*;
import java.util.*;
import java.text.SimpleDateFormat;
import java.text.NumberFormat;
import java.util.Locale;

public class ATM {
    private static final String DATA_FILE = "atm_data.txt";
    private static final String TRANSACTION_FILE = "transactions.txt";
    private static Scanner scanner = new Scanner(System.in);
    private static double balance = 0;
    private static int pin = 1234; // Default PIN
    private static boolean isLoggedIn = false;
    private static final Locale indiaLocale = new Locale("en", "IN");
    private static final NumberFormat rupeeFormat = NumberFormat.getCurrencyInstance(indiaLocale);

    public static void main(String[] args) {
        System.out.println("============================================");
        System.out.println("      🏦 WELCOME TO JAVA BANK ATM         ");
        System.out.println("============================================");
        
        loadData(); // Load balance and PIN from file
        
        while (true) {
            if (!isLoggedIn) {
                showLoginMenu();
            } else {
                showMainMenu();
            }
        }
    }

    // Login Menu
    private static void showLoginMenu() {
        System.out.println("\n🔐 ATM LOGIN");
        System.out.println("1. Login with PIN");
        System.out.println("2. Change PIN");
        System.out.println("3. Exit");
        System.out.print("Choose an option: ");

        int choice = getIntInput();
        
        switch (choice) {
            case 1:
                login();
                break;
            case 2:
                changePin();
                break;
            case 3:
                saveData();
                System.out.println("\n✅ Thank you for using Java Bank ATM. Goodbye!");
                System.exit(0);
                break;
            default:
                System.out.println("❌ Invalid option! Please try again.");
        }
    }

    // Main Menu after login
    private static void showMainMenu() {
        System.out.println("\n🏦 MAIN MENU");
        System.out.println("1. Check Balance");
        System.out.println("2. Deposit Money");
        System.out.println("3. Withdraw Money");
        System.out.println("4. Transfer Money");
        System.out.println("5. Transaction History");
        System.out.println("6. Mini Statement");
        System.out.println("7. Logout");
        System.out.print("Choose an option: ");

        int choice = getIntInput();
        
        switch (choice) {
            case 1:
                checkBalance();
                break;
            case 2:
                depositMoney();
                break;
            case 3:
                withdrawMoney();
                break;
            case 4:
                transferMoney();
                break;
            case 5:
                showTransactionHistory();
                break;
            case 6:
                showMiniStatement();
                break;
            case 7:
                logout();
                break;
            default:
                System.out.println("❌ Invalid option! Please try again.");
        }
    }

    // Login method
    private static void login() {
        System.out.print("\nEnter your 4-digit PIN: ");
        int enteredPin = getIntInput();
        
        if (enteredPin == pin) {
            isLoggedIn = true;
            System.out.println("✅ Login successful! Welcome back.");
            logTransaction("LOGIN", 0, "User logged in");
        } else {
            System.out.println("❌ Invalid PIN! Please try again.");
        }
    }

    // Change PIN method
    private static void changePin() {
        System.out.print("\nEnter current PIN: ");
        int currentPin = getIntInput();
        
        if (currentPin != pin) {
            System.out.println("❌ Incorrect current PIN!");
            return;
        }
        
        System.out.print("Enter new 4-digit PIN: ");
        int newPin = getIntInput();
        
        if (String.valueOf(newPin).length() != 4) {
            System.out.println("❌ PIN must be 4 digits!");
            return;
        }
        
        System.out.print("Confirm new 4-digit PIN: ");
        int confirmPin = getIntInput();
        
        if (newPin != confirmPin) {
            System.out.println("❌ PINs don't match!");
            return;
        }
        
        pin = newPin;
        saveData();
        System.out.println("✅ PIN changed successfully!");
        logTransaction("PIN_CHANGE", 0, "PIN updated");
    }

    // Check Balance
    private static void checkBalance() {
        System.out.println("\n💰 ACCOUNT BALANCE");
        System.out.println("Current Balance: " + rupeeFormat.format(balance));
        logTransaction("BALANCE_CHECK", 0, "Balance inquiry");
    }

    // Deposit Money
    private static void depositMoney() {
        System.out.println("\n💵 DEPOSIT MONEY");
        System.out.print("Enter amount to deposit: ₹");
        double amount = getDoubleInput();
        
        if (amount <= 0) {
            System.out.println("❌ Amount must be greater than zero!");
            return;
        }
        
        // Indian ATM deposit limits
        if (amount > 50000) {
            System.out.println("❌ Deposit limit exceeded! Maximum deposit is ₹50,000 per transaction.");
            return;
        }
        
        // Check for valid Indian currency denominations
        if (!isValidDenomination(amount)) {
            System.out.println("❌ Invalid amount! Please enter amount in valid denominations (multiples of 100, 500, 2000).");
            return;
        }
        
        balance += amount;
        saveData();
        System.out.println("✅ " + rupeeFormat.format(amount) + " deposited successfully!");
        System.out.println("💰 New Balance: " + rupeeFormat.format(balance));
        logTransaction("DEPOSIT", amount, "Cash deposit");
    }

    // Withdraw Money
    private static void withdrawMoney() {
        System.out.println("\n💸 WITHDRAW MONEY");
        System.out.println("Available denominations: ₹100, ₹500, ₹2000");
        System.out.print("Enter amount to withdraw: ₹");
        double amount = getDoubleInput();
        
        if (amount <= 0) {
            System.out.println("❌ Amount must be greater than zero!");
            return;
        }
        
        if (amount > balance) {
            System.out.println("❌ Insufficient funds!");
            System.out.println("Available Balance: " + rupeeFormat.format(balance));
            return;
        }
        
        // Indian ATM withdrawal limits
        if (amount > 25000) {
            System.out.println("❌ Withdrawal limit exceeded! Maximum withdrawal is ₹25,000 per transaction.");
            return;
        }
        
        // Check for valid Indian currency denominations
        if (!isValidDenomination(amount)) {
            System.out.println("❌ Invalid amount! Please enter amount in valid denominations (multiples of 100, 500, 2000).");
            return;
        }
        
        // Check if ATM has sufficient cash (simulated)
        if (!isCashAvailable(amount)) {
            System.out.println("❌ ATM cash limit exceeded! Please try smaller amount.");
            return;
        }
        
        balance -= amount;
        saveData();
        System.out.println("✅ " + rupeeFormat.format(amount) + " withdrawn successfully!");
        dispenseNotes(amount); // Show note breakdown
        System.out.println("💰 New Balance: " + rupeeFormat.format(balance));
        logTransaction("WITHDRAWAL", amount, "Cash withdrawal");
    }

    // Transfer Money
    private static void transferMoney() {
        System.out.println("\n🔄 TRANSFER MONEY");
        System.out.print("Enter recipient account number: ");
        String accountNumber = scanner.nextLine();
        
        System.out.print("Enter recipient name: ");
        String recipientName = scanner.nextLine();
        
        System.out.print("Enter IFSC code: ");
        String ifscCode = scanner.nextLine();
        
        System.out.print("Enter amount to transfer: ₹");
        double amount = getDoubleInput();
        
        if (amount <= 0) {
            System.out.println("❌ Amount must be greater than zero!");
            return;
        }
        
        if (amount > balance) {
            System.out.println("❌ Insufficient funds!");
            System.out.println("Available Balance: " + rupeeFormat.format(balance));
            return;
        }
        
        // Indian transfer limits
        if (amount > 100000) {
            System.out.println("❌ Transfer limit exceeded! Maximum transfer is ₹1,00,000 per transaction.");
            return;
        }
        
        // Validate account number (basic validation for Indian banks)
        if (accountNumber.length() != 11 || !accountNumber.matches("\\d+")) {
            System.out.println("❌ Invalid account number! Must be 11 digits.");
            return;
        }
        
        // Validate IFSC code (basic format check)
        if (ifscCode.length() != 11 || !ifscCode.matches("[A-Z]{4}0\\d{6}")) {
            System.out.println("❌ Invalid IFSC code! Format: ABCD0123456");
            return;
        }
        
        // Confirm transfer
        System.out.println("\n📋 TRANSFER DETAILS:");
        System.out.println("Recipient: " + recipientName);
        System.out.println("Account: " + accountNumber);
        System.out.println("IFSC: " + ifscCode);
        System.out.println("Amount: " + rupeeFormat.format(amount));
        System.out.print("Confirm transfer? (yes/no): ");
        String confirmation = scanner.nextLine();
        
        if (!confirmation.equalsIgnoreCase("yes")) {
            System.out.println("❌ Transfer cancelled.");
            return;
        }
        
        balance -= amount;
        saveData();
        System.out.println("✅ " + rupeeFormat.format(amount) + " transferred to " + recipientName + " successfully!");
        System.out.println("💰 New Balance: " + rupeeFormat.format(balance));
        logTransaction("TRANSFER", amount, "Transfer to " + recipientName + " (" + accountNumber + ")");
    }

    // Show Transaction History
    private static void showTransactionHistory() {
        System.out.println("\n📊 TRANSACTION HISTORY");
        
        try {
            File file = new File(TRANSACTION_FILE);
            if (!file.exists()) {
                System.out.println("No transactions found.");
                return;
            }
            
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line;
            int count = 0;
            
            System.out.println("┌─────────────────────────────────────────────────────────────────────────────┐");
            System.out.println("│ Date & Time         │ Type          │ Amount        │ Description           │");
            System.out.println("├─────────────────────────────────────────────────────────────────────────────┤");
            
            List<String> transactions = new ArrayList<>();
            while ((line = reader.readLine()) != null) {
                transactions.add(line);
            }
            reader.close();
            
            // Show all transactions
            for (String transaction : transactions) {
                String[] parts = transaction.split("\\|");
                if (parts.length >= 4) {
                    double amount = Double.parseDouble(parts[2]);
                    String formattedAmount = amount == 0 ? "         - " : String.format("%-12s", rupeeFormat.format(amount));
                    System.out.printf("│ %-19s │ %-13s │ %-12s │ %-21s │%n", 
                            parts[0], parts[1], formattedAmount, parts[3]);
                }
            }
            
            System.out.println("└─────────────────────────────────────────────────────────────────────────────┘");
            
        } catch (IOException e) {
            System.out.println("❌ Error reading transaction history.");
        }
        
        logTransaction("HISTORY_VIEW", 0, "Viewed transaction history");
    }

    // Show Mini Statement (Last 5 transactions)
    private static void showMiniStatement() {
        System.out.println("\n📄 MINI STATEMENT");
        System.out.println("Current Balance: " + rupeeFormat.format(balance));
        
        try {
            File file = new File(TRANSACTION_FILE);
            if (!file.exists()) {
                System.out.println("No transactions found.");
                return;
            }
            
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line;
            
            System.out.println("┌─────────────────────────────────────────────────────────────────────────────┐");
            System.out.println("│ Date & Time         │ Type          │ Amount        │ Description           │");
            System.out.println("├─────────────────────────────────────────────────────────────────────────────┤");
            
            List<String> transactions = new ArrayList<>();
            while ((line = reader.readLine()) != null) {
                transactions.add(line);
            }
            reader.close();
            
            // Show last 5 transactions
            int start = Math.max(0, transactions.size() - 5);
            for (int i = start; i < transactions.size(); i++) {
                String[] parts = transactions.get(i).split("\\|");
                if (parts.length >= 4) {
                    double amount = Double.parseDouble(parts[2]);
                    String formattedAmount = amount == 0 ? "         - " : String.format("%-12s", rupeeFormat.format(amount));
                    System.out.printf("│ %-19s │ %-13s │ %-12s │ %-21s │%n", 
                            parts[0], parts[1], formattedAmount, parts[3]);
                }
            }
            
            System.out.println("└─────────────────────────────────────────────────────────────────────────────┘");
            
        } catch (IOException e) {
            System.out.println("❌ Error reading mini statement.");
        }
        
        logTransaction("MINI_STATEMENT", 0, "Viewed mini statement");
    }

    // Logout
    private static void logout() {
        isLoggedIn = false;
        System.out.println("✅ Logged out successfully!");
        logTransaction("LOGOUT", 0, "User logged out");
    }

    // Utility method to validate Indian currency denominations
    private static boolean isValidDenomination(double amount) {
        // Check if amount is multiple of 100 (for ₹100, ₹500, ₹2000 notes)
        return amount % 100 == 0;
    }

    // Utility method to simulate cash availability
    private static boolean isCashAvailable(double amount) {
        // Simulate ATM cash limit of ₹1,00,000
        final double ATM_CASH_LIMIT = 100000;
        Random random = new Random();
        double availableCash = random.nextDouble() * ATM_CASH_LIMIT;
        
        return amount <= availableCash;
    }

    // Method to dispense notes breakdown
    private static void dispenseNotes(double amount) {
        System.out.println("💵 Notes dispensed:");
        
        int twoThousandNotes = (int) (amount / 2000);
        amount %= 2000;
        
        int fiveHundredNotes = (int) (amount / 500);
        amount %= 500;
        
        int hundredNotes = (int) (amount / 100);
        
        if (twoThousandNotes > 0) {
            System.out.println("   ₹2000 x " + twoThousandNotes + " = " + rupeeFormat.format(twoThousandNotes * 2000));
        }
        if (fiveHundredNotes > 0) {
            System.out.println("   ₹500  x " + fiveHundredNotes + " = " + rupeeFormat.format(fiveHundredNotes * 500));
        }
        if (hundredNotes > 0) {
            System.out.println("   ₹100  x " + hundredNotes + " = " + rupeeFormat.format(hundredNotes * 100));
        }
    }

    // Log transaction
    private static void logTransaction(String type, double amount, String description) {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String timestamp = dateFormat.format(new Date());
            
            FileWriter writer = new FileWriter(TRANSACTION_FILE, true);
            writer.write(String.format("%s|%s|%.2f|%s%n", timestamp, type, amount, description));
            writer.close();
        } catch (IOException e) {
            System.out.println("❌ Error logging transaction.");
        }
    }

    // Load data from file
    private static void loadData() {
        try {
            File file = new File(DATA_FILE);
            if (!file.exists()) {
                // Initialize with default values
                balance = 10000.00; // Default starting balance in ₹
                saveData();
                return;
            }
            
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("BALANCE:")) {
                    balance = Double.parseDouble(line.substring(8));
                } else if (line.startsWith("PIN:")) {
                    pin = Integer.parseInt(line.substring(4));
                }
            }
            reader.close();
        } catch (IOException e) {
            System.out.println("❌ Error loading data. Using default values.");
            balance = 10000.00;
        }
    }

    // Save data to file
    private static void saveData() {
        try {
            FileWriter writer = new FileWriter(DATA_FILE);
            writer.write("BALANCE:" + balance + "\n");
            writer.write("PIN:" + pin + "\n");
            writer.close();
        } catch (IOException e) {
            System.out.println("❌ Error saving data.");
        }
    }

    // Utility method to get integer input
    private static int getIntInput() {
        while (true) {
            try {
                return Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.print("❌ Invalid input! Please enter a number: ");
            }
        }
    }

    // Utility method to get double input
    private static double getDoubleInput() {
        while (true) {
            try {
                return Double.parseDouble(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.print("❌ Invalid input! Please enter a valid amount: ");
            }
        }
    }
}