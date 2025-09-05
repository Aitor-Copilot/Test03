# Java Application Guide

## Vehicle Authorization Database Manager

This Java application provides a user-friendly menu interface for managing the Vehicle Authorization database operations.

## Features

### ✅ **Current Features**
1. **Create New Database** - Executes the PowerShell script to create the complete database structure
2. **Verify Database Structure** - Verifies existing database with detailed reporting
3. **Interactive Menu** - User-friendly command-line interface
4. **Error Handling** - Comprehensive error handling and user feedback
5. **Real-time Output** - Shows PowerShell script output in real-time

### 🚧 **Future Features** (Framework Ready)
- **Import JSON Data** - Import vehicle authorization data from JSON files
- **Export Database Report** - Generate comprehensive database reports
- **Database Statistics** - Show database content statistics and analytics

## Quick Start

### Method 1: Batch File (Easiest)
```batch
run-vehicle-auth-manager.bat
```

### Method 2: Direct Java Execution
```bash
java -cp target/classes com.vehicleauth.VehicleAuthDatabaseManagerSimple
```

### Method 3: Maven Execution
```bash
mvn compile exec:java -Dexec.mainClass=com.vehicleauth.VehicleAuthDatabaseManagerSimple
```

## Application Structure

### Main Classes

#### 1. `VehicleAuthDatabaseManagerSimple.java`
- **Purpose**: Main application class (no external dependencies)
- **Features**: Complete menu system, PowerShell script execution
- **Dependencies**: Only standard Java libraries
- **Recommended**: Use this for production

#### 2. `VehicleAuthDatabaseManager.java` 
- **Purpose**: Main application with logging framework
- **Features**: Advanced logging, dependency injection ready
- **Dependencies**: SLF4J, Logback, Apache Commons
- **Use Case**: For advanced features and enterprise deployment

#### 3. `DatabaseService.java`
- **Purpose**: Service layer for database operations
- **Features**: Script execution, validation, error handling
- **Dependencies**: SLF4J, Apache Commons

#### 4. `MenuInterface.java`
- **Purpose**: User interface and menu handling
- **Features**: Interactive menus, user input validation
- **Dependencies**: SLF4J

## Menu Options

```
===============================================================
                        MAIN MENU
===============================================================
1. Create New Database
2. Verify Database Structure  
3. Import JSON Data (Coming Soon)
4. Export Database Report (Coming Soon)
5. Database Statistics (Coming Soon)
0. Exit
===============================================================
```

### Option 1: Create New Database
- Executes `CreateVehicleAuthDatabase.ps1`
- Creates 19 tables with 38 indexes
- Sets up complete relational structure
- Main table: Applications (ApplicationID as primary key)

### Option 2: Verify Database Structure
- Executes `CreateVehicleAuthDatabase.ps1 -Verify`
- Reports on existing database structure
- Shows table count, index count, relationships
- Validates database integrity

## Prerequisites

### Required
- **Java 11 or later**
- **Microsoft Access** installed
- **PowerShell** with execution policy allowing scripts
- **Database.accdb** file in the same directory
- **CreateVehicleAuthDatabase.ps1** script in the same directory

### Optional (for Maven build)
- **Maven 3.6+**
- **Internet connection** (for dependency download)

## Building from Source

### Compile Simple Version (No Dependencies)
```bash
javac -d target/classes src/main/java/com/vehicleauth/VehicleAuthDatabaseManagerSimple.java
```

### Compile with Maven
```bash
mvn clean compile
```

### Run Tests
```bash
mvn test
```

### Package JAR
```bash
mvn package
```

## Configuration

### Script Configuration
The application looks for these files in the current directory:
- `CreateVehicleAuthDatabase.ps1` - PowerShell script
- `Database.accdb` - Microsoft Access database

### Logging Configuration
- Log files created in `logs/` directory
- Configuration in `src/main/resources/logback.xml`
- Console output for warnings and errors only

## Error Handling

### Common Issues

#### 1. "PowerShell script not found"
- **Solution**: Ensure `CreateVehicleAuthDatabase.ps1` is in the same directory
- **Check**: File exists and is not corrupted

#### 2. "Database file not found"
- **Solution**: Ensure `Database.accdb` is in the same directory
- **Check**: File exists and is not locked by another application

#### 3. "Script execution failed"
- **Solution**: Check PowerShell execution policy
- **Command**: `Set-ExecutionPolicy -ExecutionPolicy RemoteSigned -Scope CurrentUser`

#### 4. "Java not found"
- **Solution**: Install Java 11 or later
- **Check**: `java -version` command works

## Maven Dependencies

```xml
<!-- Core dependencies for full version -->
<dependency>
    <groupId>org.slf4j</groupId>
    <artifactId>slf4j-api</artifactId>
    <version>2.0.7</version>
</dependency>

<dependency>
    <groupId>ch.qos.logback</groupId>
    <artifactId>logback-classic</artifactId>
    <version>1.4.8</version>
</dependency>

<!-- Future features -->
<dependency>
    <groupId>com.fasterxml.jackson.core</groupId>
    <artifactId>jackson-databind</artifactId>
    <version>2.15.2</version>
</dependency>

<dependency>
    <groupId>commons-io</groupId>
    <artifactId>commons-io</artifactId>
    <version>2.11.0</version>
</dependency>
```

## Extension Points

The application is designed for easy extension:

### Adding New Menu Options
1. Add menu option in `printMainMenu()`
2. Add case in `handleMenuChoice()`
3. Implement handler method

### Adding New Database Operations
1. Add method to `DatabaseService`
2. Create corresponding UI handler
3. Add to menu system

### Adding JSON Import
1. Use Jackson dependency (already included)
2. Create JSON parsing service
3. Map JSON to database operations

## Success Indicators

When running the application successfully:
- ✅ Menu appears correctly
- ✅ Database creation completes without errors
- ✅ PowerShell script output shows "SUCCESS"
- ✅ Database verification shows correct table/index counts
- ✅ All prerequisites are met

## Support

For issues:
1. Check prerequisites are met
2. Verify file locations
3. Check PowerShell execution policy
4. Review error messages in console output
5. Check log files in `logs/` directory

---
**Application Status**: ✅ Ready for production use  
**Main Features**: ✅ Database creation and verification  
**Future Features**: 🚧 JSON import, reporting, statistics  
