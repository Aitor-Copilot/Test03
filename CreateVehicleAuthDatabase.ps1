# ==============================================================================
# Vehicle Authorization Database Creator
# ==============================================================================
# This script creates a complete Microsoft Access database structure
# to store Vehicle Authorization data from JSON files.
# 
# Main table: Applications (with ApplicationID as primary key)
# 
# Usage: powershell -ExecutionPolicy Bypass -File "CreateVehicleAuthDatabase.ps1"
# ==============================================================================

param(
    [string]$DatabasePath = "Database.accdb",
    [switch]$Verify = $false
)

# ==============================================================================
# CONFIGURATION
# ==============================================================================

$ErrorActionPreference = "Continue"
$WarningPreference = "Continue"

# ==============================================================================
# FUNCTIONS
# ==============================================================================

function Write-Header {
    param([string]$Title)
    Write-Host ""
    Write-Host "=====================================================" -ForegroundColor Cyan
    Write-Host $Title -ForegroundColor Cyan
    Write-Host "=====================================================" -ForegroundColor Cyan
}

function Write-Success {
    param([string]$Message)
    Write-Host "+ $Message" -ForegroundColor Green
}

function Write-Warning {
    param([string]$Message)
    Write-Host "! $Message" -ForegroundColor Yellow
}

function Write-Error {
    param([string]$Message)
    Write-Host "- $Message" -ForegroundColor Red
}

function Write-Info {
    param([string]$Message)
    Write-Host "i $Message" -ForegroundColor Cyan
}

# ==============================================================================
# TABLE DEFINITIONS
# ==============================================================================

function Get-TableDefinitions {
    return @{
        # Base tables (no dependencies)
        "Addresses" = @{
            SQL = "CREATE TABLE Addresses (AddressID TEXT(100), Street TEXT(255), City TEXT(100), PostalCode TEXT(20), CountryCode TEXT(10), CreatedDate DATETIME, CONSTRAINT PK_Addresses PRIMARY KEY (AddressID))"
            Description = "Normalized address information"
        }
        
        "ContactDetails" = @{
            SQL = "CREATE TABLE ContactDetails (ContactDetailsID TEXT(100), Phone TEXT(50), Fax TEXT(50), Email TEXT(255), Website TEXT(255), CreatedDate DATETIME, CONSTRAINT PK_ContactDetails PRIMARY KEY (ContactDetailsID))"
            Description = "Contact information (phone, email, website)"
        }
        
        "Documents" = @{
            SQL = "CREATE TABLE Documents (DocumentID TEXT(100), FileTitle TEXT(255), FilePath TEXT(255), UploadDate DATETIME, CONSTRAINT PK_Documents PRIMARY KEY (DocumentID))"
            Description = "Document and file references"
        }
        
        # Tables with dependencies
        "ContactPersons" = @{
            SQL = "CREATE TABLE ContactPersons (ContactPersonID TEXT(100), FirstName TEXT(100), Surname TEXT(100), TitleOrFunction TEXT(100), AddressID TEXT(100), ContactDetailsID TEXT(100), LanguagesSpoken MEMO, PersonType TEXT(50), CONSTRAINT PK_ContactPersons PRIMARY KEY (ContactPersonID))"
            Description = "Individual contact person information"
        }
        
        "BillingInformation" = @{
            SQL = "CREATE TABLE BillingInformation (BillingID TEXT(100), LegalDenomination TEXT(255), Acronym TEXT(100), VATNumber TEXT(50), NationalRegNumber TEXT(50), AddressID TEXT(100), ContactDetailsID TEXT(100), SpecificBillingRequirements MEMO, CONSTRAINT PK_BillingInformation PRIMARY KEY (BillingID))"
            Description = "Billing and payment information"
        }
        
        "Bodies" = @{
            SQL = "CREATE TABLE Bodies (BodyID TEXT(100), BodyType TEXT(50), LegalDenomination TEXT(255), Acronym TEXT(100), VATNumber TEXT(50), NationalRegNumber TEXT(50), AddressID TEXT(100), ContactDetailsID TEXT(100), AdditionalInfo MEMO, BodyName TEXT(255), BodyIdNumber TEXT(100), EINNumber TEXT(50), CONSTRAINT PK_Bodies PRIMARY KEY (BodyID))"
            Description = "Organizations (Applicant, Notified, Designated, Assessment bodies)"
        }
        
        # Main table
        "Applications" = @{
            SQL = "CREATE TABLE Applications (ApplicationID TEXT(50), ID TEXT(100), CaseType TEXT(50), NationalRegNumber TEXT(50), ProjectName TEXT(255), ApplicationType TEXT(50), ApplicationTypeVariantVersion MEMO, IssuingAuthority TEXT(10), ApplicationStatus TEXT(50), Phase TEXT(50), DecisionDate DATETIME, Submission DATETIME, CompletenessAcknowledgement DATETIME, Modified DATETIME, CachedLastUpdate DATETIME, VehicleIdentifier MEMO, EIN TEXT(50), LegalDenomination TEXT(255), MemberStates MEMO, Subcategory MEMO, IsWholeEU YESNO, PreEngaged YESNO, PreEngagementID TEXT(50), IsPreEngagement YESNO, PreEngagementOtherInformation MEMO, DocLang TEXT(10), ApplicationVersion TEXT(20), ContactPersonID TEXT(100), FinancialContactPersonID TEXT(100), BillingInformationID TEXT(100), ApplicantBodyID TEXT(100), CreatedDate DATETIME, UpdatedDate DATETIME, CONSTRAINT PK_Applications PRIMARY KEY (ApplicationID))"
            Description = "Main table - Vehicle authorization applications"
        }
        
        # Tables dependent on Applications
        "Issues" = @{
            SQL = "CREATE TABLE Issues (IssueID TEXT(50), ID TEXT(100), ApplicationID TEXT(50), Title TEXT(255), IssueDescription MEMO, Owner TEXT(100), OwnerDisplayName TEXT(255), UserIsOwner YESNO, Assignees MEMO, AssigneesDisplayNames MEMO, DueBy DATETIME, CreationDate DATETIME, IssueType TEXT(50), IssueStatus TEXT(50), Resolution TEXT(255), AssessmentStage TEXT(50), ResolutionText MEMO, SSCClosedOut YESNO, ResolutionDescription MEMO, CONSTRAINT PK_Issues PRIMARY KEY (IssueID))"
            Description = "Issues and problems related to applications"
        }
        
        "ApplicationBodies" = @{
            SQL = "CREATE TABLE ApplicationBodies (ApplicationID TEXT(50), BodyID TEXT(100), BodyRole TEXT(50), CONSTRAINT PK_ApplicationBodies PRIMARY KEY (ApplicationID, BodyID, BodyRole))"
            Description = "Many-to-many relationship between Applications and Bodies"
        }
        
        "VehicleTypes" = @{
            SQL = "CREATE TABLE VehicleTypes (VehicleTypeID TEXT(100), ApplicationID TEXT(50), AuthorizationType TEXT(50), VehicleType TEXT(50), TypeID TEXT(50), TypeName TEXT(255), AltTypeName TEXT(255), CreationDate DATETIME, ReferenceToExistingStr MEMO, DescriptionNew MEMO, VehicleIdentifier TEXT(50), VehicleValue MEMO, AuthorizationHolderID TEXT(100), IsApplicantTypeHolder YESNO, ERATVDateOfRecord DATETIME, VehicleMainCategory TEXT(255), VehicleSubCategory TEXT(255), NonCodedRestrictions MEMO, CodedRestrictions MEMO, ChangeSummary MEMO, RegistrationEntityRecipients MEMO, CONSTRAINT PK_VehicleTypes PRIMARY KEY (VehicleTypeID))"
            Description = "Vehicle type, variant, and version information"
        }
        
        "ApplicationStaff" = @{
            SQL = "CREATE TABLE ApplicationStaff (ApplicationID TEXT(50), StaffType TEXT(50), StaffName TEXT(255), CONSTRAINT PK_ApplicationStaff PRIMARY KEY (ApplicationID, StaffType, StaffName))"
            Description = "Staff assignments (Assessors, Project Managers, etc.)"
        }
        
        # Tables dependent on VehicleTypes
        "VehiclesToAuthorise" = @{
            SQL = "CREATE TABLE VehiclesToAuthorise (VehicleToAuthoriseID TEXT(100), VehicleTypeID TEXT(100), VehicleValue TEXT(255), VehicleIdentifier TEXT(50), CONSTRAINT PK_VehiclesToAuthorise PRIMARY KEY (VehicleToAuthoriseID))"
            Description = "Specific vehicles within vehicle types"
        }
        
        "ApplicableRules" = @{
            SQL = "CREATE TABLE ApplicableRules (RuleID TEXT(100), VehicleTypeID TEXT(100), RuleType TEXT(50), MSCode TEXT(10), Comment MEMO, Directive TEXT(100), CONSTRAINT PK_ApplicableRules PRIMARY KEY (RuleID))"
            Description = "Regulatory rules and directives"
        }
        
        "MemberStateMappings" = @{
            SQL = "CREATE TABLE MemberStateMappings (MappingID TEXT(100), VehicleTypeID TEXT(100), CountryCode TEXT(10), Name TEXT(255), PassengerTransport YESNO, HighSpeed YESNO, FreightTransport YESNO, DangerousGoodsServices YESNO, ShuntingOnly YESNO, ShuntingOnlyTxt MEMO, Other YESNO, OtherDescription MEMO, IsBorderStation YESNO, AssigneeStr TEXT(255), CONSTRAINT PK_MemberStateMappings PRIMARY KEY (MappingID))"
            Description = "Country-specific operational data"
        }
        
        "AgencyMappings" = @{
            SQL = "CREATE TABLE AgencyMappings (AgencyMappingID TEXT(100), VehicleTypeID TEXT(100), Requirement TEXT(10), RequirementDescr MEMO, Visible YESNO, CONSTRAINT PK_AgencyMappings PRIMARY KEY (AgencyMappingID))"
            Description = "Agency requirements and mappings"
        }
        
        # Tables with further dependencies
        "Networks" = @{
            SQL = "CREATE TABLE Networks (NetworkID AUTOINCREMENT, MappingID TEXT(100), NetworkName TEXT(255), CONSTRAINT PK_Networks PRIMARY KEY (NetworkID))"
            Description = "Network information for member states"
        }
        
        "AgencyMappingValues" = @{
            SQL = "CREATE TABLE AgencyMappingValues (ValueID TEXT(100), AgencyMappingID TEXT(100), DocumentID TEXT(100), ValueDescription MEMO, ValueText MEMO, CONSTRAINT PK_AgencyMappingValues PRIMARY KEY (ValueID))"
            Description = "Values for agency mapping requirements"
        }
        
        "MSMappingRequirements" = @{
            SQL = "CREATE TABLE MSMappingRequirements (RequirementID TEXT(100), MappingID TEXT(100), Requirement TEXT(10), RequirementDescr MEMO, Visible YESNO, CONSTRAINT PK_MSMappingRequirements PRIMARY KEY (RequirementID))"
            Description = "Requirements for member state mappings"
        }
        
        "MSMappingRequirementValues" = @{
            SQL = "CREATE TABLE MSMappingRequirementValues (ValueID TEXT(100), RequirementID TEXT(100), DocumentID TEXT(100), ValueDescription MEMO, ValueText MEMO, CONSTRAINT PK_MSMappingRequirementValues PRIMARY KEY (ValueID))"
            Description = "Values for member state mapping requirements"
        }
    }
}

# ==============================================================================
# INDEX DEFINITIONS
# ==============================================================================

function Get-IndexDefinitions {
    return @(
        # Performance indexes for Applications table
        "CREATE INDEX IX_Applications_Status ON Applications(ApplicationStatus)",
        "CREATE INDEX IX_Applications_Type ON Applications(ApplicationType)",
        "CREATE INDEX IX_Applications_Submission ON Applications(Submission)",
        
        # Foreign key indexes for Issues
        "CREATE INDEX IX_Issues_ApplicationID ON Issues(ApplicationID)",
        "CREATE INDEX IX_Issues_Status ON Issues(IssueStatus)",
        "CREATE INDEX IX_Issues_CreationDate ON Issues(CreationDate)",
        
        # Foreign key indexes for VehicleTypes
        "CREATE INDEX IX_VehicleTypes_ApplicationID ON VehicleTypes(ApplicationID)",
        
        # Foreign key indexes for related tables
        "CREATE INDEX IX_VehiclesToAuthorise_VehicleTypeID ON VehiclesToAuthorise(VehicleTypeID)",
        "CREATE INDEX IX_ApplicableRules_VehicleTypeID ON ApplicableRules(VehicleTypeID)",
        "CREATE INDEX IX_MemberStateMappings_VehicleTypeID ON MemberStateMappings(VehicleTypeID)",
        "CREATE INDEX IX_AgencyMappings_VehicleTypeID ON AgencyMappings(VehicleTypeID)",
        "CREATE INDEX IX_Networks_MappingID ON Networks(MappingID)",
        "CREATE INDEX IX_ApplicationBodies_ApplicationID ON ApplicationBodies(ApplicationID)",
        "CREATE INDEX IX_ApplicationBodies_BodyID ON ApplicationBodies(BodyID)",
        "CREATE INDEX IX_ContactPersons_AddressID ON ContactPersons(AddressID)",
        "CREATE INDEX IX_Bodies_AddressID ON Bodies(AddressID)",
        "CREATE INDEX IX_MSMappingRequirements_MappingID ON MSMappingRequirements(MappingID)",
        "CREATE INDEX IX_MSMappingRequirementValues_RequirementID ON MSMappingRequirementValues(RequirementID)",
        "CREATE INDEX IX_AgencyMappingValues_AgencyMappingID ON AgencyMappingValues(AgencyMappingID)"
    )
}

# ==============================================================================
# DATABASE CREATION FUNCTIONS
# ==============================================================================

function Connect-ToAccess {
    param([string]$DatabasePath)
    
    try {
        Write-Info "Connecting to Microsoft Access..."
        
        $access = New-Object -ComObject Access.Application
        $access.Visible = $false
        
        $fullPath = (Resolve-Path $DatabasePath).Path
        Write-Info "Opening database: $fullPath"
        
        $access.OpenCurrentDatabase($fullPath)
        $db = $access.CurrentDb()
        
        return @{
            Access = $access
            Database = $db
            Success = $true
        }
    }
    catch {
        Write-Error "Failed to connect to Access: $($_.Exception.Message)"
        return @{
            Access = $null
            Database = $null
            Success = $false
            Error = $_.Exception.Message
        }
    }
}

function Disconnect-FromAccess {
    param($AccessConnection)
    
    try {
        if ($AccessConnection.Access) {
            $AccessConnection.Access.CloseCurrentDatabase()
            $AccessConnection.Access.Quit()
            [System.Runtime.Interopservices.Marshal]::ReleaseComObject($AccessConnection.Access) | Out-Null
        }
        [System.GC]::Collect()
        [System.GC]::WaitForPendingFinalizers()
    }
    catch {
        Write-Warning "Error during cleanup: $($_.Exception.Message)"
    }
}

function Remove-ExistingTable {
    param($Access, [string]$TableName)
    
    try {
        $Access.DoCmd.DeleteObject(0, $TableName)  # 0 = acTable
        return $true
    }
    catch {
        return $false  # Table doesn't exist, which is fine
    }
}

function Create-DatabaseTables {
    param($Database, $Access)
    
    $tables = Get-TableDefinitions
    $tableOrder = @(
        "Addresses", "ContactDetails", "Documents",
        "ContactPersons", "BillingInformation", "Bodies",
        "Applications", "Issues", "ApplicationBodies", "VehicleTypes",
        "VehiclesToAuthorise", "ApplicableRules", "MemberStateMappings",
        "Networks", "AgencyMappings", "AgencyMappingValues",
        "ApplicationStaff", "MSMappingRequirements", "MSMappingRequirementValues"
    )
    
    $successCount = 0
    $errorCount = 0
    $createdTables = @()
    
    Write-Info "Creating database tables..."
    
    foreach ($tableName in $tableOrder) {
        try {
            Write-Host "  Creating table: $tableName" -ForegroundColor Cyan
            
            # Remove existing table if it exists
            $removed = Remove-ExistingTable -Access $Access -TableName $tableName
            if ($removed) {
                Write-Host "    - Dropped existing table" -ForegroundColor Yellow
            }
            
            # Create new table
            $tableInfo = $tables[$tableName]
            $Database.Execute($tableInfo.SQL)
            
            Write-Success "    Successfully created: $tableName"
            Write-Host "    - Description: $($tableInfo.Description)" -ForegroundColor Gray
            
            $successCount++
            $createdTables += $tableName
            
        }
        catch {
            Write-Error "    Failed to create table $tableName`: $($_.Exception.Message)"
            $errorCount++
        }
    }
    
    return @{
        SuccessCount = $successCount
        ErrorCount = $errorCount
        CreatedTables = $createdTables
    }
}

function Create-DatabaseIndexes {
    param($Database)
    
    $indexes = Get-IndexDefinitions
    $successCount = 0
    $errorCount = 0
    
    Write-Info "Creating performance indexes..."
    
    foreach ($indexSQL in $indexes) {
        try {
            $Database.Execute($indexSQL)
            $successCount++
        }
        catch {
            Write-Warning "    Index creation warning: $($_.Exception.Message)"
            $errorCount++
        }
    }
    
    Write-Success "    Created $successCount indexes successfully"
    if ($errorCount -gt 0) {
        Write-Warning "    $errorCount indexes had warnings (may already exist)"
    }
    
    return @{
        SuccessCount = $successCount
        ErrorCount = $errorCount
    }
}

function Verify-DatabaseStructure {
    param($Database, $Access)
    
    Write-Header "DATABASE STRUCTURE VERIFICATION"
    
    try {
        # Count tables
        $tableCount = 0
        $tableList = @()
        foreach ($tableDef in $Database.TableDefs) {
            if (-not $tableDef.Name.StartsWith("MSys")) {  # Skip system tables
                $tableCount++
                $tableList += $tableDef.Name
            }
        }
        
        Write-Info "TABLES IN DATABASE:"
        $i = 1
        foreach ($table in ($tableList | Sort-Object)) {
            Write-Host "  $i. $table" -ForegroundColor White
            $i++
        }
        
        # Check main table
        Write-Info "MAIN TABLE VERIFICATION:"
        try {
            $applicationsTable = $Database.TableDefs("Applications")
            Write-Success "Applications Table Found"
            Write-Host "    - Primary Key: ApplicationID" -ForegroundColor Cyan
            Write-Host "    - Field Count: $($applicationsTable.Fields.Count)" -ForegroundColor White
            Write-Host "    - Description: Main table for vehicle authorization applications" -ForegroundColor Gray
        }
        catch {
            Write-Error "Applications table not found!"
        }
        
        # Count indexes
        $indexCount = 0
        foreach ($tableDef in $Database.TableDefs) {
            if (-not $tableDef.Name.StartsWith("MSys")) {
                foreach ($index in $tableDef.Indexes) {
                    $indexCount++
                }
            }
        }
        
        Write-Info "PERFORMANCE OPTIMIZATION:"
        Write-Host "    - Total Indexes: $indexCount" -ForegroundColor Cyan
        Write-Host "    - Primary Keys: Created on all tables" -ForegroundColor White
        Write-Host "    - Foreign Key Indexes: Created for relationships" -ForegroundColor White
        
        return @{
            TableCount = $tableCount
            IndexCount = $indexCount
            Success = $true
        }
    }
    catch {
        Write-Error "Verification failed: $($_.Exception.Message)"
        return @{
            TableCount = 0
            IndexCount = 0
            Success = $false
            Error = $_.Exception.Message
        }
    }
}

# ==============================================================================
# MAIN EXECUTION
# ==============================================================================

function Main {
    Write-Header "VEHICLE AUTHORIZATION DATABASE CREATOR"
    Write-Host "Database: $DatabasePath" -ForegroundColor Yellow
    Write-Host "Main Table: Applications (with ApplicationID as primary key)" -ForegroundColor Cyan
    Write-Host ""
    
    # Check if database file exists
    if (-not (Test-Path $DatabasePath)) {
        Write-Error "Database file not found: $DatabasePath"
        Write-Info "Please ensure the Access database file exists before running this script."
        exit 1
    }
    
    # Connect to Access
    $connection = Connect-ToAccess -DatabasePath $DatabasePath
    if (-not $connection.Success) {
        Write-Error "Failed to connect to Microsoft Access database."
        Write-Info "Please ensure Microsoft Access is installed and the database file is not open in another application."
        exit 1
    }
    
    try {
        # Create tables
        Write-Header "CREATING DATABASE STRUCTURE"
        $tableResult = Create-DatabaseTables -Database $connection.Database -Access $connection.Access
        
        # Create indexes
        $indexResult = Create-DatabaseIndexes -Database $connection.Database
        
        # Verify structure (if requested or if there were no errors)
        if ($Verify -or ($tableResult.ErrorCount -eq 0 -and $indexResult.ErrorCount -eq 0)) {
            $verifyResult = Verify-DatabaseStructure -Database $connection.Database -Access $connection.Access
        }
        
        # Display final summary
        Write-Header "CREATION SUMMARY"
        Write-Host "Tables created successfully: $($tableResult.SuccessCount)" -ForegroundColor Green
        Write-Host "Tables with errors: $($tableResult.ErrorCount)" -ForegroundColor $(if($tableResult.ErrorCount -gt 0){"Red"}else{"Green"})
        Write-Host "Indexes created: $($indexResult.SuccessCount)" -ForegroundColor Green
        Write-Host "Index warnings: $($indexResult.ErrorCount)" -ForegroundColor Yellow
        
        if ($verifyResult -and $verifyResult.Success) {
            Write-Host "Total tables verified: $($verifyResult.TableCount)" -ForegroundColor Cyan
            Write-Host "Total indexes verified: $($verifyResult.IndexCount)" -ForegroundColor Cyan
        }
        
        Write-Host ""
        if ($tableResult.ErrorCount -eq 0) {
            Write-Success "DATABASE STRUCTURE CREATED SUCCESSFULLY!"
            Write-Info "The database is ready to store Vehicle Authorization data from JSON files."
            Write-Info "Main table 'Applications' uses 'ApplicationID' as the primary key."
        }
        else {
            Write-Warning "Database created with some errors. Please review the output above."
        }
        
    }
    finally {
        # Always cleanup
        Disconnect-FromAccess -AccessConnection $connection
    }
}

# ==============================================================================
# SCRIPT EXECUTION
# ==============================================================================

try {
    Main
}
catch {
    Write-Error "Unexpected error: $($_.Exception.Message)"
    exit 1
}
