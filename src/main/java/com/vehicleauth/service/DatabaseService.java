package com.vehicleauth.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
 * Service class for database operations
 * Handles PowerShell script execution and database management
 */
public class DatabaseService {
    
    private static final Logger logger = LoggerFactory.getLogger(DatabaseService.class);
    
    // Configuration constants
    private static final String POWERSHELL_SCRIPT = "CreateVehicleAuthDatabase.ps1";
    private static final String DATABASE_FILE = "Database.accdb";
    private static final int SCRIPT_TIMEOUT_MINUTES = 10;
    
    /**
     * Create the database by executing the PowerShell script
     * @return true if database creation was successful, false otherwise
     */
    public boolean createDatabase() {
        logger.info("Starting database creation process");
        
        // Verify prerequisites
        if (!verifyPrerequisites()) {
            return false;
        }
        
        try {
            // Execute PowerShell script
            return executeScript(false);
            
        } catch (Exception e) {
            logger.error("Database creation failed with exception", e);
            System.err.println("Database creation failed: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Verify the database structure by executing the PowerShell script with -Verify flag
     * @return true if database verification was successful, false otherwise
     */
    public boolean verifyDatabase() {
        logger.info("Starting database verification process");
        
        // Check if database file exists
        if (!databaseExists()) {
            System.out.println("❌ Database file not found: " + DATABASE_FILE);
            System.out.println("💡 Please create the database first using option 1.");
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
            logger.error("Database verification failed with exception", e);
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
            System.err.println("❌ PowerShell script not found: " + POWERSHELL_SCRIPT);
            System.err.println("💡 Please ensure the script is in the same directory as this application.");
            return false;
        }
        
        // Check if database file exists
        if (!databaseExists()) {
            System.err.println("❌ Database file not found: " + DATABASE_FILE);
            System.err.println("💡 Please ensure the Access database file exists.");
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
        boolean exists = Files.exists(scriptPath);
        logger.debug("Script existence check: {} - {}", POWERSHELL_SCRIPT, exists);
        return exists;
    }
    
    /**
     * Check if the database file exists
     * @return true if database exists, false otherwise
     */
    private boolean databaseExists() {
        Path dbPath = Paths.get(DATABASE_FILE);
        boolean exists = Files.exists(dbPath);
        logger.debug("Database existence check: {} - {}", DATABASE_FILE, exists);
        return exists;
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
        
        logger.info("Executing command: {}", String.join(" ", command));
        System.out.println("🔧 Executing: " + String.join(" ", command));
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
            logger.warn("Script execution timed out after {} minutes", SCRIPT_TIMEOUT_MINUTES);
            System.err.println("❌ Script execution timed out after " + SCRIPT_TIMEOUT_MINUTES + " minutes");
            process.destroyForcibly();
            return false;
        }
        
        int exitCode = process.exitValue();
        logger.info("Script execution completed with exit code: {}", exitCode);
        
        if (exitCode == 0 && success) {
            System.out.println();
            System.out.println("✅ Script execution completed successfully!");
            return true;
        } else {
            System.out.println();
            System.err.println("❌ Script execution failed with exit code: " + exitCode);
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
                
                // Log important lines
                if (line.contains("ERROR") || line.contains("FAILED") || line.contains("❌")) {
                    logger.warn("Script output warning/error: {}", line);
                } else if (line.contains("SUCCESS") || line.contains("✅") || line.contains("DATABASE STRUCTURE CREATED SUCCESSFULLY")) {
                    logger.info("Script output success: {}", line);
                    successIndicator = true;
                }
            }
        } catch (IOException e) {
            logger.error("Error reading process output", e);
            System.err.println("❌ Error reading script output: " + e.getMessage());
            return false;
        }
        
        return successIndicator;
    }
    
    /**
     * Get the current working directory
     * @return Current working directory path
     */
    public String getCurrentDirectory() {
        return System.getProperty("user.dir");
    }
    
    /**
     * Get information about the database service configuration
     * @return Configuration information string
     */
    public String getServiceInfo() {
        StringBuilder info = new StringBuilder();
        info.append("Database Service Configuration:\n");
        info.append("- PowerShell Script: ").append(POWERSHELL_SCRIPT).append("\n");
        info.append("- Database File: ").append(DATABASE_FILE).append("\n");
        info.append("- Script Timeout: ").append(SCRIPT_TIMEOUT_MINUTES).append(" minutes\n");
        info.append("- Current Directory: ").append(getCurrentDirectory()).append("\n");
        info.append("- Script Exists: ").append(scriptExists()).append("\n");
        info.append("- Database Exists: ").append(databaseExists()).append("\n");
        
        return info.toString();
    }
}
