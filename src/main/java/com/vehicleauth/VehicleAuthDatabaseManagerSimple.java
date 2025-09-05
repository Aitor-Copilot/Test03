package com.vehicleauth;

import java.util.Scanner;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Simple Vehicle Authorization Database Manager (without external dependencies)
 * Provides a menu-driven interface for database operations
 */
public class VehicleAuthDatabaseManagerSimple {
    
    // Configuration constants
    private static final String POWERSHELL_SCRIPT = "CreateVehicleAuthDatabase.ps1";
    private static final String DATABASE_FILE = "Database.accdb";
    private static final int SCRIPT_TIMEOUT_MINUTES = 10;
    
    private final Scanner scanner;
    private boolean running;
    
    public VehicleAuthDatabaseManagerSimple() {
        this.scanner = new Scanner(System.in);
        this.running = true;
    }
    
    public static void main(String[] args) {
        System.out.println("Starting Vehicle Authorization Database Manager");
        
        try {
            VehicleAuthDatabaseManagerSimple app = new VehicleAuthDatabaseManagerSimple();
            app.showMainMenu();
        } catch (Exception e) {
            System.err.println("Fatal error: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
        
        System.out.println("Application shutdown complete");
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
        System.out.println("3. Import JSON Data (Coming Soon)");
        System.out.println("4. Export Database Report (Coming Soon)");
        System.out.println("5. Database Statistics (Coming Soon)");
        System.out.println("0. Exit");
        System.out.println("===============================================================");
        System.out.print("Please select an option (0-5): ");
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
                handleImportJsonData();
                break;
            case 4:
                handleExportReport();
                break;
            case 5:
                handleDatabaseStatistics();
                break;
            case 0:
                handleExit();
                break;
            default:
                System.out.println("Invalid option. Please select a number between 0-5.");
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
        System.out.println("CREATE NEW DATABASE");
        System.out.println("---------------------------------------------------------------");
        System.out.println("This will create a complete Vehicle Authorization database structure");
        System.out.println("with all necessary tables, indexes, and relationships.");
        System.out.println();
        
        System.out.print("Do you want to proceed? (y/N): ");
        String confirmation = scanner.nextLine().trim().toLowerCase();
        
        if ("y".equals(confirmation) || "yes".equals(confirmation)) {
            System.out.println("\nStarting database creation...");
            
            try {
                boolean success = createDatabase();
                if (success) {
                    System.out.println("Database created successfully!");
                    System.out.println("Main table: Applications (with ApplicationID as primary key)");
                    System.out.println("Complete structure ready for Vehicle Authorization data");
                } else {
                    System.out.println("Database creation failed. Please check the output above for details.");
                }
            } catch (Exception e) {
                System.err.println("Error occurred during database creation: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.out.println("Database creation cancelled.");
        }
    }
    
    /**
     * Handle database verification option
     */
    private void handleVerifyDatabase() {
        System.out.println("VERIFY DATABASE STRUCTURE");
        System.out.println("---------------------------------------------------------------");
        System.out.println("This will verify the existing database structure and report");
        System.out.println("on tables, indexes, and relationships.");
        System.out.println();
        
        try {
            boolean success = verifyDatabase();
            if (success) {
                System.out.println("Database structure verification completed successfully!");
            } else {
                System.out.println("Database verification failed or database not found.");
            }
        } catch (Exception e) {
            System.err.println("Error occurred during database verification: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Handle JSON data import option (placeholder for future implementation)
     */
    private void handleImportJsonData() {
        System.out.println("IMPORT JSON DATA");
        System.out.println("---------------------------------------------------------------");
        System.out.println("This feature will import Vehicle Authorization data from JSON files");
        System.out.println("into the database tables.");
        System.out.println();
        System.out.println("This feature is coming soon!");
        System.out.println("The database structure is ready to receive JSON data.");
    }
    
    /**
     * Handle export report option (placeholder for future implementation)
     */
    private void handleExportReport() {
        System.out.println("EXPORT DATABASE REPORT");
        System.out.println("---------------------------------------------------------------");
        System.out.println("This feature will generate comprehensive reports about");
        System.out.println("the database content and structure.");
        System.out.println();
        System.out.println("This feature is coming soon!");
    }
    
    /**
     * Handle database statistics option (placeholder for future implementation)
     */
    private void handleDatabaseStatistics() {
        System.out.println("DATABASE STATISTICS");
        System.out.println("---------------------------------------------------------------");
        System.out.println("This feature will show statistics about database content,");
        System.out.println("including record counts, data distribution, and more.");
        System.out.println();
        System.out.println("This feature is coming soon!");
    }
    
    /**
     * Handle application exit
     */
    private void handleExit() {
        System.out.println("GOODBYE");
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
    
    /**
     * Create the database by executing the PowerShell script
     * @return true if database creation was successful, false otherwise
     */
    private boolean createDatabase() {
        System.out.println("Starting database creation process");
        
        // Verify prerequisites
        if (!verifyPrerequisites()) {
            return false;
        }
        
        try {
            // Execute PowerShell script
            return executeScript(false);
            
        } catch (Exception e) {
            System.err.println("Database creation failed: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Verify the database structure by executing the PowerShell script with -Verify flag
     * @return true if database verification was successful, false otherwise
     */
    private boolean verifyDatabase() {
        System.out.println("Starting database verification process");
        
        // Check if database file exists
        if (!databaseExists()) {
            System.out.println("Database file not found: " + DATABASE_FILE);
            System.out.println("Please create the database first using option 1.");
            return false;
        }
        
        // Verify prerequisites
        if (!verifyPrerequisites()) {
            return false;
        }
        
        try {
            // Execute PowerShell script with verification
            return executeScript(true);
            
        } catch (Exception e) {
            System.err.println("Database verification failed: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Verify that all prerequisites are met for script execution
     * @return true if prerequisites are met, false otherwise
     */
    private boolean verifyPrerequisites() {
        // Check if PowerShell script exists
        if (!scriptExists()) {
            System.err.println("PowerShell script not found: " + POWERSHELL_SCRIPT);
            System.err.println("Please ensure the script is in the same directory as this application.");
            return false;
        }
        
        // Check if database file exists
        if (!databaseExists()) {
            System.err.println("Database file not found: " + DATABASE_FILE);
            System.err.println("Please ensure the Access database file exists.");
            return false;
        }
        
        return true;
    }
    
    /**
     * Check if the PowerShell script exists
     * @return true if script exists, false otherwise
     */
    private boolean scriptExists() {
        Path scriptPath = Paths.get(POWERSHELL_SCRIPT);
        return Files.exists(scriptPath);
    }
    
    /**
     * Check if the database file exists
     * @return true if database exists, false otherwise
     */
    private boolean databaseExists() {
        Path dbPath = Paths.get(DATABASE_FILE);
        return Files.exists(dbPath);
    }
    
    /**
     * Execute the PowerShell script
     * @param withVerification true to run with -Verify flag, false for basic creation
     * @return true if execution was successful, false otherwise
     */
    private boolean executeScript(boolean withVerification) throws IOException, InterruptedException {
        // Build PowerShell command
        List<String> command = new ArrayList<>();
        command.add("powershell");
        command.add("-ExecutionPolicy");
        command.add("Bypass");
        command.add("-File");
        command.add(POWERSHELL_SCRIPT);
        
        if (withVerification) {
            command.add("-Verify");
        }
        
        System.out.println("Executing: " + String.join(" ", command));
        System.out.println();
        
        // Create process builder
        ProcessBuilder processBuilder = new ProcessBuilder(command);
        processBuilder.directory(new File("."));
        processBuilder.redirectErrorStream(true);
        
        // Start process
        Process process = processBuilder.start();
        
        // Read output in real-time
        boolean success = readProcessOutput(process);
        
        // Wait for process to complete
        boolean finished = process.waitFor(SCRIPT_TIMEOUT_MINUTES, TimeUnit.MINUTES);
        
        if (!finished) {
            System.err.println("Script execution timed out after " + SCRIPT_TIMEOUT_MINUTES + " minutes");
            process.destroyForcibly();
            return false;
        }
        
        int exitCode = process.exitValue();
        System.out.println("Script execution completed with exit code: " + exitCode);
        
        if (exitCode == 0 && success) {
            System.out.println();
            System.out.println("Script execution completed successfully!");
            return true;
        } else {
            System.out.println();
            System.err.println("Script execution failed with exit code: " + exitCode);
            return false;
        }
    }
    
    /**
     * Read and display process output in real-time
     * @param process The process to read output from
     * @return true if output suggests success, false otherwise
     */
    private boolean readProcessOutput(Process process) {
        boolean successIndicator = false;
        
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
                
                // Check for success indicators
                if (line.contains("SUCCESS") || line.contains("DATABASE STRUCTURE CREATED SUCCESSFULLY")) {
                    successIndicator = true;
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading script output: " + e.getMessage());
            return false;
        }
        
        return successIndicator;
    }
}
