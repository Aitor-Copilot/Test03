package com.vehicleauth.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for DatabaseService
 */
class DatabaseServiceTest {
    
    private DatabaseService databaseService;
    
    @BeforeEach
    void setUp() {
        databaseService = new DatabaseService();
    }
    
    @Test
    @DisplayName("Should create DatabaseService instance")
    void shouldCreateDatabaseServiceInstance() {
        assertNotNull(databaseService);
    }
    
    @Test
    @DisplayName("Should return current directory")
    void shouldReturnCurrentDirectory() {
        String currentDir = databaseService.getCurrentDirectory();
        assertNotNull(currentDir);
        assertFalse(currentDir.isEmpty());
    }
    
    @Test
    @DisplayName("Should return service info")
    void shouldReturnServiceInfo() {
        String serviceInfo = databaseService.getServiceInfo();
        assertNotNull(serviceInfo);
        assertFalse(serviceInfo.isEmpty());
        assertTrue(serviceInfo.contains("Database Service Configuration"));
        assertTrue(serviceInfo.contains("PowerShell Script"));
        assertTrue(serviceInfo.contains("Database File"));
    }
}
