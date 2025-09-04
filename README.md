# Vehicle Authorization Database

This project contains a complete Microsoft Access database structure designed to store Vehicle Authorization data from JSON files.

## Quick Start

To create the database structure, run the single PowerShell script:

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

- Microsoft Access installed
- PowerShell execution policy allowing script execution
- Access database file (Database.accdb) in the same folder

## Files

- `CreateVehicleAuthDatabase.ps1` - **Main script** (only script needed)
- `Database.accdb` - Target Microsoft Access database
- `DATABASE_STRUCTURE_SUMMARY.md` - Complete documentation
- `Json Files/` - Source JSON files used for analysis

## Features

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
