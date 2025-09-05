package com.vehicleauth.ui;

import com.vehicleauth.service.ConfigurationService;
import com.vehicleauth.service.DatabaseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Scanner;

/**
 * Menu interface for Vehicle Authorization Database Manager
 * Provides user interaction and menu navigation
 */
public class MenuInterface {
    
    private static final Logger logger = LoggerFactory.getLogger(MenuInterface.class);
    private final Scanner scanner;
    private final DatabaseService databaseService;
    private final ConfigurationService configurationService;
    private boolean running;
    
    public MenuInterface() {
        this.scanner = new Scanner(System.in);
        this.databaseService = new DatabaseService();
        this.configurationService = new ConfigurationService();
        this.running = true;
    }
    
    /**
     * Display and handle the main menu
     */
    public void showMainMenu() {
        printWelcome();
        
        while (running) {
            printMainMenu();
            int choice = getUserChoice();
            handleMenuChoice(choice);
        }
        
        scanner.close();
        System.out.println("\nThank you for using Vehicle Authorization Database Manager!");
    }
    
    /**
     * Print welcome message and application info
     */
    private void printWelcome() {
        System.out.println("===============================================================");
        System.out.println("    VEHICLE AUTHORIZATION DATABASE MANAGER");
        System.out.println("===============================================================");
        System.out.println("This application manages Vehicle Authorization database operations");
        System.out.println("based on JSON data structures for vehicle authorization processes.");
        System.out.println();
    }
    
    /**
     * Print the main menu options
     */
    private void printMainMenu() {
        System.out.println("===============================================================");
        System.out.println("                        MAIN MENU");
        System.out.println("===============================================================");
        System.out.println("1. Create New Database");
        System.out.println("2. Verify Database Structure");
        System.out.println("3. Generate Field Length Configuration");
        System.out.println("4. Import JSON Data (Coming Soon)");
        System.out.println("5. Export Database Report (Coming Soon)");
        System.out.println("6. Database Statistics (Coming Soon)");
        System.out.println("0. Exit");
        System.out.println("===============================================================");
        System.out.print("Please select an option (0-6): ");
    }
    
    /**
     * Get user input for menu choice
     * @return Selected menu option
     */
    private int getUserChoice() {
        try {
            String input = scanner.nextLine().trim();
            return Integer.parseInt(input);
        } catch (NumberFormatException e) {
            logger.debug("Invalid input received: non-numeric");
            return -1;
        }
    }
    
    /**
     * Handle the selected menu choice
     * @param choice Selected menu option
     */
    private void handleMenuChoice(int choice) {
        System.out.println();
        
        switch (choice) {
            case 1:
                handleCreateDatabase();
                break;
            case 2:
                handleVerifyDatabase();
                break;
            case 3:
                handleGenerateFieldLengthConfig();
                break;
            case 4:
                handleImportJsonData();
                break;
            case 5:
                handleExportReport();
                break;
            case 6:
                handleDatabaseStatistics();
                break;
            case 0:
                handleExit();
                break;
            default:
                System.out.println("❌ Invalid option. Please select a number between 0-6.");
                break;
        }
        
        if (running && choice != 0) {
            waitForUserInput();
        }
    }
    
    /**
     * Handle database creation option
     */
    private void handleCreateDatabase() {
        System.out.println("🔨 CREATE NEW DATABASE");
        System.out.println("---------------------------------------------------------------");
        System.out.println("This will create a complete Vehicle Authorization database structure");
        System.out.println("with all necessary tables, indexes, and relationships.");
        System.out.println();
        
        System.out.print("Do you want to proceed? (y/N): ");
        String confirmation = scanner.nextLine().trim().toLowerCase();
        
        if ("y".equals(confirmation) || "yes".equals(confirmation)) {
            System.out.println("\n🚀 Starting database creation...");
            
            try {
                boolean success = databaseService.createDatabase();
                if (success) {
                    System.out.println("✅ Database created successfully!");
                    System.out.println("📊 Main table: Applications (with ApplicationID as primary key)");
                    System.out.println("🗃️  Complete structure ready for Vehicle Authorization data");
                } else {
                    System.out.println("❌ Database creation failed. Please check the logs for details.");
                }
            } catch (Exception e) {
                logger.error("Error during database creation", e);
                System.out.println("❌ Error occurred during database creation: " + e.getMessage());
            }
        } else {
            System.out.println("❌ Database creation cancelled.");
        }
    }
    
    /**
     * Handle database verification option
     */
    private void handleVerifyDatabase() {
        System.out.println("🔍 VERIFY DATABASE STRUCTURE");
        System.out.println("---------------------------------------------------------------");
        System.out.println("This will verify the existing database structure and report");
        System.out.println("on tables, indexes, and relationships.");
        System.out.println();
        
        try {
            boolean success = databaseService.verifyDatabase();
            if (success) {
                System.out.println("✅ Database structure verification completed successfully!");
            } else {
                System.out.println("❌ Database verification failed or database not found.");
            }
        } catch (Exception e) {
            logger.error("Error during database verification", e);
            System.out.println("❌ Error occurred during database verification: " + e.getMessage());
        }
    }
    
    /**
     * Handle field length configuration generation
     */
    private void handleGenerateFieldLengthConfig() {
        System.out.println("📊 GENERATE FIELD LENGTH CONFIGURATION");
        System.out.println("---------------------------------------------------------------");
        System.out.println("This will analyze all JSON files in the 'Json Files' directory");
        System.out.println("and generate a configuration file with maximum text field lengths");
        System.out.println("for the database schema.");
        System.out.println();
        System.out.println("The configuration file will be saved as:");
        System.out.println("  📁 Configuration/Fields_length.txt");
        System.out.println();
        
        System.out.print("Do you want to proceed? (y/N): ");
        String confirmation = scanner.nextLine().trim().toLowerCase();
        
        if ("y".equals(confirmation) || "yes".equals(confirmation)) {
            System.out.println("\n🔍 Analyzing JSON files...");
            boolean success = configurationService.generateFieldLengthConfiguration();
            
            if (success) {
                System.out.println("\n✅ Field length configuration generated successfully!");
                System.out.println("📄 Configuration file: Configuration/Fields_length.txt");
                System.out.println("🔧 This file can be used to optimize database field sizes.");
            } else {
                System.out.println("\n❌ Failed to generate field length configuration.");
                System.out.println("Please check the logs for more details.");
            }
        } else {
            System.out.println("❌ Operation cancelled.");
        }
    }
    
    /**
     * Handle JSON data import option (placeholder for future implementation)
     */
    private void handleImportJsonData() {
        System.out.println("📥 IMPORT JSON DATA");
        System.out.println("---------------------------------------------------------------");
        System.out.println("This feature will import Vehicle Authorization data from JSON files");
        System.out.println("into the database tables.");
        System.out.println();
        System.out.println("🚧 This feature is coming soon!");
        System.out.println("💡 The database structure is ready to receive JSON data.");
    }
    
    /**
     * Handle export report option (placeholder for future implementation)
     */
    private void handleExportReport() {
        System.out.println("📤 EXPORT DATABASE REPORT");
        System.out.println("---------------------------------------------------------------");
        System.out.println("This feature will generate comprehensive reports about");
        System.out.println("the database content and structure.");
        System.out.println();
        System.out.println("🚧 This feature is coming soon!");
    }
    
    /**
     * Handle database statistics option (placeholder for future implementation)
     */
    private void handleDatabaseStatistics() {
        System.out.println("📊 DATABASE STATISTICS");
        System.out.println("---------------------------------------------------------------");
        System.out.println("This feature will show statistics about database content,");
        System.out.println("including record counts, data distribution, and more.");
        System.out.println();
        System.out.println("🚧 This feature is coming soon!");
    }
    
    /**
     * Handle application exit
     */
    private void handleExit() {
        System.out.println("👋 GOODBYE");
        System.out.println("---------------------------------------------------------------");
        System.out.println("Thank you for using Vehicle Authorization Database Manager!");
        running = false;
    }
    
    /**
     * Wait for user to press Enter before continuing
     */
    private void waitForUserInput() {
        System.out.println("\nPress Enter to continue...");
        scanner.nextLine();
        System.out.println();
    }
}
