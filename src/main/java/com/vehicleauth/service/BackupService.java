package com.vehicleauth.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Service class for automatic database backup operations
 * Creates timestamped backups of the database file
 */
public class BackupService {
    
    // Configuration constants
    private static final String DATABASE_FILE = "Database.accdb";
    private static final String BACKUP_FOLDER = "Backup Database";
    private static final String BACKUP_PREFIX = "Database-Backup-";
    private static final String BACKUP_EXTENSION = ".accdb";
    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");
    
    /**
     * Automatically create a backup of the database at startup
     * This method runs silently and handles all errors gracefully
     */
    public static void createStartupBackup() {
        try {
            BackupService backupService = new BackupService();
            BackupResult result = backupService.createBackup();
            
            if (result.isSuccess()) {
                System.out.println("✓ Database backup created: " + result.getBackupFileName());
            } else {
                System.out.println("⚠ Backup skipped: " + result.getMessage());
            }
        } catch (Exception e) {
            // Silent failure - don't interrupt application startup
            System.out.println("⚠ Backup creation failed silently: " + e.getMessage());
        }
    }
    
    /**
     * Create a timestamped backup of the database
     * @return BackupResult containing success status and details
     */
    public BackupResult createBackup() {
        try {
            // Check if source database exists
            Path sourcePath = Paths.get(DATABASE_FILE);
            if (!Files.exists(sourcePath)) {
                return new BackupResult(false, "Source database file not found: " + DATABASE_FILE, null);
            }
            
            // Ensure backup directory exists
            Path backupDir = Paths.get(BACKUP_FOLDER);
            if (!Files.exists(backupDir)) {
                try {
                    Files.createDirectories(backupDir);
                } catch (IOException e) {
                    return new BackupResult(false, "Failed to create backup directory: " + e.getMessage(), null);
                }
            }
            
            // Generate timestamped backup filename
            String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMAT);
            String backupFileName = BACKUP_PREFIX + timestamp + BACKUP_EXTENSION;
            Path backupPath = backupDir.resolve(backupFileName);
            
            // Create the backup copy
            Files.copy(sourcePath, backupPath, StandardCopyOption.REPLACE_EXISTING);
            
            // Get file size for reporting
            long fileSize = Files.size(backupPath);
            String fileSizeStr = formatFileSize(fileSize);
            
            return new BackupResult(true, 
                "Backup created successfully (" + fileSizeStr + ")", 
                backupFileName);
                
        } catch (IOException e) {
            return new BackupResult(false, "Backup failed: " + e.getMessage(), null);
        } catch (Exception e) {
            return new BackupResult(false, "Unexpected error during backup: " + e.getMessage(), null);
        }
    }
    
    /**
     * Get information about the backup directory
     * @return BackupInfo containing directory details
     */
    public BackupInfo getBackupInfo() {
        try {
            Path backupDir = Paths.get(BACKUP_FOLDER);
            
            if (!Files.exists(backupDir)) {
                return new BackupInfo(false, 0, 0, "Backup directory does not exist");
            }
            
            long backupCount = Files.list(backupDir)
                .filter(path -> path.toString().toLowerCase().endsWith(".accdb"))
                .count();
            
            long totalSize = Files.list(backupDir)
                .filter(path -> path.toString().toLowerCase().endsWith(".accdb"))
                .mapToLong(path -> {
                    try {
                        return Files.size(path);
                    } catch (IOException e) {
                        return 0;
                    }
                })
                .sum();
            
            return new BackupInfo(true, backupCount, totalSize, "Backup directory is available");
            
        } catch (IOException e) {
            return new BackupInfo(false, 0, 0, "Error reading backup directory: " + e.getMessage());
        }
    }
    
    /**
     * Clean up old backups, keeping only the specified number of most recent backups
     * @param keepCount Number of backups to keep (0 = keep all)
     * @return Number of backups deleted
     */
    public int cleanupOldBackups(int keepCount) {
        if (keepCount <= 0) {
            return 0; // Keep all backups
        }
        
        try {
            Path backupDir = Paths.get(BACKUP_FOLDER);
            if (!Files.exists(backupDir)) {
                return 0;
            }
            
            // Get all backup files sorted by last modified time (newest first)
            var backupFiles = Files.list(backupDir)
                .filter(path -> path.toString().toLowerCase().endsWith(".accdb"))
                .filter(path -> path.getFileName().toString().startsWith(BACKUP_PREFIX))
                .sorted((p1, p2) -> {
                    try {
                        return Files.getLastModifiedTime(p2).compareTo(Files.getLastModifiedTime(p1));
                    } catch (IOException e) {
                        return 0;
                    }
                })
                .toList();
            
            int deletedCount = 0;
            
            // Delete files beyond the keep count
            for (int i = keepCount; i < backupFiles.size(); i++) {
                try {
                    Files.delete(backupFiles.get(i));
                    deletedCount++;
                } catch (IOException e) {
                    // Continue with other files
                }
            }
            
            return deletedCount;
            
        } catch (IOException e) {
            return 0;
        }
    }
    
    /**
     * Format file size in human-readable format
     * @param bytes File size in bytes
     * @return Formatted file size string
     */
    private String formatFileSize(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        } else if (bytes < 1024 * 1024) {
            return String.format("%.1f KB", bytes / 1024.0);
        } else if (bytes < 1024 * 1024 * 1024) {
            return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
        } else {
            return String.format("%.1f GB", bytes / (1024.0 * 1024.0 * 1024.0));
        }
    }
    
    /**
     * Result class for backup operations
     */
    public static class BackupResult {
        private final boolean success;
        private final String message;
        private final String backupFileName;
        
        public BackupResult(boolean success, String message, String backupFileName) {
            this.success = success;
            this.message = message;
            this.backupFileName = backupFileName;
        }
        
        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public String getBackupFileName() { return backupFileName; }
    }
    
    /**
     * Information class for backup directory status
     */
    public static class BackupInfo {
        private final boolean available;
        private final long backupCount;
        private final long totalSize;
        private final String message;
        
        public BackupInfo(boolean available, long backupCount, long totalSize, String message) {
            this.available = available;
            this.backupCount = backupCount;
            this.totalSize = totalSize;
            this.message = message;
        }
        
        public boolean isAvailable() { return available; }
        public long getBackupCount() { return backupCount; }
        public long getTotalSize() { return totalSize; }
        public String getMessage() { return message; }
    }
}
