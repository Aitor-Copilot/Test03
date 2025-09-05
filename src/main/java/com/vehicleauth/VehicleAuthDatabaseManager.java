package com.vehicleauth;

import com.vehicleauth.service.BackupService;
import com.vehicleauth.ui.MenuInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main class for Vehicle Authorization Database Manager
 * Provides a menu-driven interface for database operations
 */
public class VehicleAuthDatabaseManager {
    
    private static final Logger logger = LoggerFactory.getLogger(VehicleAuthDatabaseManager.class);
    
    public static void main(String[] args) {
        logger.info("Starting Vehicle Authorization Database Manager");
        
        // Automatic database backup at startup
        logger.info("Performing automatic database backup");
        BackupService.createStartupBackup();
        
        try {
            MenuInterface menuInterface = new MenuInterface();
            menuInterface.showMainMenu();
        } catch (Exception e) {
            logger.error("Fatal error occurred", e);
            System.err.println("Fatal error: " + e.getMessage());
            System.exit(1);
        }
        
        logger.info("Application shutdown complete");
    }
}
