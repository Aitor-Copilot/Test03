package com.vehicleauth.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for BackupService
 */
class BackupServiceTest {
    
    private BackupService backupService;
    
    @TempDir
    Path tempDir;
    
    @BeforeEach
    void setUp() {
        backupService = new BackupService();
    }
    
    @Test
    @DisplayName("Should create BackupService instance")
    void shouldCreateBackupServiceInstance() {
        assertNotNull(backupService);
    }
    
    @Test
    @DisplayName("Should return backup info when backup directory exists")
    void shouldReturnBackupInfoWhenDirectoryExists() throws IOException {
        // Create a temporary backup directory
        Path backupDir = tempDir.resolve("Backup Database");
        Files.createDirectories(backupDir);
        
        // Create a test backup file
        Path testBackup = backupDir.resolve("test-backup.accdb");
        Files.write(testBackup, "test data".getBytes());
        
        // Note: This test would need to be adapted to work with the actual backup directory
        // For now, we're just testing the service instantiation and basic methods
        BackupService.BackupInfo info = backupService.getBackupInfo();
        assertNotNull(info);
        assertNotNull(info.getMessage());
    }
    
    @Test
    @DisplayName("Should handle backup result correctly")
    void shouldHandleBackupResultCorrectly() {
        BackupService.BackupResult result = new BackupService.BackupResult(
            true, 
            "Test backup successful", 
            "test-backup.accdb"
        );
        
        assertTrue(result.isSuccess());
        assertEquals("Test backup successful", result.getMessage());
        assertEquals("test-backup.accdb", result.getBackupFileName());
    }
    
    @Test
    @DisplayName("Should handle backup info correctly")
    void shouldHandleBackupInfoCorrectly() {
        BackupService.BackupInfo info = new BackupService.BackupInfo(
            true, 
            5, 
            1024000, 
            "5 backups found"
        );
        
        assertTrue(info.isAvailable());
        assertEquals(5, info.getBackupCount());
        assertEquals(1024000, info.getTotalSize());
        assertEquals("5 backups found", info.getMessage());
    }
    
    @Test
    @DisplayName("Should handle cleanup operation")
    void shouldHandleCleanupOperation() {
        // Test the cleanup method with keep count of 0 (keep all)
        int deletedCount = backupService.cleanupOldBackups(0);
        assertEquals(0, deletedCount); // Should not delete anything when keepCount is 0
    }
}
