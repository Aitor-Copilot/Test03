# Vehicle Authorization Database

This project contains a complete Microsoft Access database structure designed to store Vehicle Authorization data from JSON files, along with a Java application for easy database management.

## Quick Start

### Option 1: Java Application (Recommended)
Run the user-friendly Java application:
```batch
run-vehicle-auth-manager.bat
```

### Option 2: Direct PowerShell Script
To create the database structure directly, run the PowerShell script:
```powershell
powershell -ExecutionPolicy Bypass -File "CreateVehicleAuthDatabase.ps1"
```

### Options

- **Basic Creation**: `CreateVehicleAuthDatabase.ps1`
- **With Verification**: `CreateVehicleAuthDatabase.ps1 -Verify`
- **Custom Database**: `CreateVehicleAuthDatabase.ps1 -DatabasePath "MyDatabase.accdb"`

## What It Creates

- **19 Tables** with proper relationships
- **38 Indexes** for optimal performance
- **Main Table**: `Applications` with `ApplicationID` as primary key
- Complete structure to store all JSON data elements

## Requirements

- **Java 11 or later** (for the Java application)
- **Microsoft Access installed**
- **PowerShell execution policy** allowing script execution
- **Access database file** (Database.accdb) in the same folder

## Files

### Core Files
- `run-vehicle-auth-manager.bat` - **Java application launcher** (easiest way to use)
- `CreateVehicleAuthDatabase.ps1` - **Main PowerShell script** (database creation)
- `Database.accdb` - Target Microsoft Access database

### Java Application
- `src/main/java/` - Java source code (Maven project)
- `pom.xml` - Maven configuration with dependencies
- `target/classes/` - Compiled Java classes

### Documentation
- `README.md` - This file (usage instructions)
- `DATABASE_STRUCTURE_SUMMARY.md` - Complete database documentation
- `Json Files/` - Source JSON files used for analysis

## Features

### Java Application Features
✅ **Automatic Backup** - Creates timestamped database backup at startup  
✅ **User-Friendly Menu** - Interactive menu-driven interface  
✅ **Database Creation** - Create complete database structure  
✅ **Database Verification** - Verify existing database structure  
✅ **Future-Ready** - Extensible for JSON import and reporting features  
✅ **Cross-Platform** - Runs on any system with Java 11+  

### PowerShell Script Features
✅ **Single Script Solution** - One comprehensive script does everything  
✅ **Error Handling** - Robust error handling and reporting  
✅ **Progress Tracking** - Clear progress indicators during creation  
✅ **Verification** - Built-in structure verification  
✅ **Cleanup** - Automatic cleanup of existing tables  
✅ **Performance Optimized** - Indexes on all key relationships  

## Database Structure

### Main Tables
- **Applications** (Main table) - Vehicle authorization applications
- **Issues** - Problems and issues related to applications
- **VehicleTypes** - Vehicle types, variants, and versions
- **Bodies** - Organizations (Applicant, Notified, Designated, Assessment)
- **ContactPersons** - Individual contacts
- **Addresses** & **ContactDetails** - Normalized contact information

### Supporting Tables
- **Documents** - File references
- **VehiclesToAuthorise** - Specific vehicle details
- **ApplicableRules** - Regulatory compliance
- **MemberStateMappings** - Country-specific data
- **Networks** - Network information
- **AgencyMappings** - Agency requirements
- And more...

## Success

The script creates a fully normalized relational database structure that captures all the complex nested data from your JSON files while maintaining proper database design principles.

**Ready for production use!** 🎉
