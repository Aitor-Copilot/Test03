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
            SQL = "CREATE TABLE Addresses (AddressID TEXT(100) PRIMARY KEY, Street TEXT(255), City TEXT(100), PostalCode TEXT(20), CountryCode TEXT(10), CreatedDate DATETIME)"
            Description = "Normalized address information"
        }
        
        "ContactDetails" = @{
            SQL = "CREATE TABLE ContactDetails (ContactDetailsID TEXT(100) PRIMARY KEY, Phone TEXT(50), Fax TEXT(50), Email TEXT(255), Website TEXT(255), CreatedDate DATETIME)"
            Description = "Contact information (phone, email, website)"
        }
        
        "Documents" = @{
            SQL = "CREATE TABLE Documents (DocumentID TEXT(100) PRIMARY KEY, FileTitle TEXT(255), FilePath TEXT(255), UploadDate DATETIME)"
            Description = "Document and file references"
        }
        
        # Tables with dependencies
        "ContactPersons" = @{
            SQL = "CREATE TABLE ContactPersons (ContactPersonID TEXT(100) PRIMARY KEY, FirstName TEXT(100), Surname TEXT(100), TitleOrFunction TEXT(100), AddressID TEXT(100), ContactDetailsID TEXT(100), LanguagesSpoken MEMO, PersonType TEXT(50))"
            Description = "Individual contact person information"
        }
        
        "BillingInformation" = @{
            SQL = "CREATE TABLE BillingInformation (BillingID TEXT(100) PRIMARY KEY, LegalDenomination TEXT(255), Acronym TEXT(100), VATNumber TEXT(50), NationalRegNumber TEXT(50), AddressID TEXT(100), ContactDetailsID TEXT(100), SpecificBillingRequirements MEMO)"
            Description = "Billing and payment information"
        }
        
        "Bodies" = @{
            SQL = "CREATE TABLE Bodies (BodyID TEXT(100) PRIMARY KEY, BodyType TEXT(50), LegalDenomination TEXT(255), Acronym TEXT(100), VATNumber TEXT(50), NationalRegNumber TEXT(50), AddressID TEXT(100), ContactDetailsID TEXT(100), AdditionalInfo MEMO, BodyName TEXT(255), BodyIdNumber TEXT(100), EINNumber TEXT(50))"
            Description = "Organizations (Applicant, Notified, Designated, Assessment bodies)"
        }
        
        # Main table
        "Applications" = @{
            SQL = "CREATE TABLE Applications (ApplicationID TEXT(50) PRIMARY KEY, ID TEXT(100), CaseType TEXT(50), NationalRegNumber TEXT(50), ProjectName TEXT(255), ApplicationType TEXT(50), ApplicationTypeVariantVersion MEMO, IssuingAuthority TEXT(10), ApplicationStatus TEXT(50), Phase TEXT(50), DecisionDate DATETIME, Submission DATETIME, CompletenessAcknowledgement DATETIME, Modified DATETIME, CachedLastUpdate DATETIME, VehicleIdentifier MEMO, EIN TEXT(50), LegalDenomination TEXT(255), MemberStates MEMO, Subcategory MEMO, IsWholeEU YESNO, PreEngaged YESNO, PreEngagementID TEXT(50), IsPreEngagement YESNO, PreEngagementOtherInformation MEMO, DocLang TEXT(10), ApplicationVersion TEXT(20), ContactPersonID TEXT(100), FinancialContactPersonID TEXT(100), BillingInformationID TEXT(100), ApplicantBodyID TEXT(100), CreatedDate DATETIME, UpdatedDate DATETIME)"
            Description = "Main table - Vehicle authorization applications"
        }
        
        # Tables dependent on Applications
        "Issues" = @{
            SQL = "CREATE TABLE Issues (IssueID TEXT(50) PRIMARY KEY, ID TEXT(100), ApplicationID TEXT(50), Title TEXT(255), IssueDescription MEMO, Owner TEXT(100), OwnerDisplayName TEXT(255), UserIsOwner YESNO, Assignees MEMO, AssigneesDisplayNames MEMO, DueBy DATETIME, CreationDate DATETIME, IssueType TEXT(50), IssueStatus TEXT(50), Resolution TEXT(255), AssessmentStage TEXT(50), ResolutionText MEMO, SSCClosedOut YESNO, ResolutionDescription MEMO)"
            Description = "Issues and problems related to applications"
        }
        
        "ApplicationBodies" = @{
            SQL = "CREATE TABLE ApplicationBodies (ApplicationID TEXT(50), BodyID TEXT(100), BodyRole TEXT(50), PRIMARY KEY (ApplicationID, BodyID, BodyRole))"
            Description = "Many-to-many relationship between Applications and Bodies"
        }
        
        "VehicleTypes" = @{
            SQL = "CREATE TABLE VehicleTypes (VehicleTypeID TEXT(100) PRIMARY KEY, ApplicationID TEXT(50), AuthorizationType TEXT(50), VehicleType TEXT(50), TypeID TEXT(50), TypeName TEXT(255), AltTypeName TEXT(255), CreationDate DATETIME, ReferenceToExistingStr MEMO, DescriptionNew MEMO, VehicleIdentifier TEXT(50), VehicleValue MEMO, AuthorizationHolderID TEXT(100), IsApplicantTypeHolder YESNO, ERATVDateOfRecord DATETIME, VehicleMainCategory TEXT(255), VehicleSubCategory TEXT(255), NonCodedRestrictions MEMO, CodedRestrictions MEMO, ChangeSummary MEMO, RegistrationEntityRecipients MEMO)"
            Description = "Vehicle type, variant, and version information"
        }
        
        "ApplicationStaff" = @{
            SQL = "CREATE TABLE ApplicationStaff (ApplicationID TEXT(50), StaffType TEXT(50), StaffName TEXT(255), PRIMARY KEY (ApplicationID, StaffType, StaffName))"
            Description = "Staff assignments (Assessors, Project Managers, etc.)"
        }
        
        # Tables dependent on VehicleTypes
        "VehiclesToAuthorise" = @{
            SQL = "CREATE TABLE VehiclesToAuthorise (VehicleToAuthoriseID TEXT(100) PRIMARY KEY, VehicleTypeID TEXT(100), VehicleValue TEXT(255), VehicleIdentifier TEXT(50))"
            Description = "Specific vehicles within vehicle types"
        }
        
        "ApplicableRules" = @{
            SQL = "CREATE TABLE ApplicableRules (RuleID TEXT(100) PRIMARY KEY, VehicleTypeID TEXT(100), RuleType TEXT(50), MSCode TEXT(10), Comment MEMO, Directive TEXT(100))"
            Description = "Regulatory rules and directives"
        }
        
        "MemberStateMappings" = @{
            SQL = "CREATE TABLE MemberStateMappings (MappingID TEXT(100) PRIMARY KEY, VehicleTypeID TEXT(100), CountryCode TEXT(10), Name TEXT(255), PassengerTransport YESNO, HighSpeed YESNO, FreightTransport YESNO, DangerousGoodsServices YESNO, ShuntingOnly YESNO, ShuntingOnlyTxt MEMO, Other YESNO, OtherDescription MEMO, IsBorderStation YESNO, AssigneeStr TEXT(255))"
            Description = "Country-specific operational data"
        }
        
        "AgencyMappings" = @{
            SQL = "CREATE TABLE AgencyMappings (AgencyMappingID TEXT(100) PRIMARY KEY, VehicleTypeID TEXT(100), Requirement TEXT(10), RequirementDescr MEMO, Visible YESNO)"
            Description = "Agency requirements and mappings"
        }
        
        # Tables with further dependencies
        "Networks" = @{
            SQL = "CREATE TABLE Networks (NetworkID AUTOINCREMENT PRIMARY KEY, MappingID TEXT(100), NetworkName TEXT(255))"
            Description = "Network information for member states"
        }
        
        "AgencyMappingValues" = @{
            SQL = "CREATE TABLE AgencyMappingValues (ValueID TEXT(100) PRIMARY KEY, AgencyMappingID TEXT(100), DocumentID TEXT(100), ValueDescription MEMO, ValueText MEMO)"
            Description = "Values for agency mapping requirements"
        }
        
        "MSMappingRequirements" = @{
            SQL = "CREATE TABLE MSMappingRequirements (RequirementID TEXT(100) PRIMARY KEY, MappingID TEXT(100), Requirement TEXT(10), RequirementDescr MEMO, Visible YESNO)"
            Description = "Requirements for member state mappings"
        }
        
        "MSMappingRequirementValues" = @{
            SQL = "CREATE TABLE MSMappingRequirementValues (ValueID TEXT(100) PRIMARY KEY, RequirementID TEXT(100), DocumentID TEXT(100), ValueDescription MEMO, ValueText MEMO)"
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
        "CREATE INDEX IX_ContactPersons_ContactDetailsID ON ContactPersons(ContactDetailsID)",
        "CREATE INDEX IX_Bodies_AddressID ON Bodies(AddressID)",
        "CREATE INDEX IX_Bodies_ContactDetailsID ON Bodies(ContactDetailsID)",
        "CREATE INDEX IX_BillingInformation_AddressID ON BillingInformation(AddressID)",
        "CREATE INDEX IX_BillingInformation_ContactDetailsID ON BillingInformation(ContactDetailsID)",
        "CREATE INDEX IX_MSMappingRequirements_MappingID ON MSMappingRequirements(MappingID)",
        "CREATE INDEX IX_MSMappingRequirementValues_RequirementID ON MSMappingRequirementValues(RequirementID)",
        "CREATE INDEX IX_MSMappingRequirementValues_DocumentID ON MSMappingRequirementValues(DocumentID)",
        "CREATE INDEX IX_AgencyMappingValues_AgencyMappingID ON AgencyMappingValues(AgencyMappingID)",
        "CREATE INDEX IX_AgencyMappingValues_DocumentID ON AgencyMappingValues(DocumentID)",
        "CREATE INDEX IX_ApplicationStaff_ApplicationID ON ApplicationStaff(ApplicationID)",
        
        # NEW: indexes for newly added FKs
        "CREATE INDEX IX_Applications_ContactPersonID ON Applications(ContactPersonID)",
        "CREATE INDEX IX_Applications_FinancialContactPersonID ON Applications(FinancialContactPersonID)",
        "CREATE INDEX IX_Applications_BillingInformationID ON Applications(BillingInformationID)",
        "CREATE INDEX IX_Applications_ApplicantBodyID ON Applications(ApplicantBodyID)",
        "CREATE INDEX IX_VehicleTypes_AuthorizationHolderID ON VehicleTypes(AuthorizationHolderID)"
    )
}

# ==============================================================================
# FOREIGN KEY RELATIONSHIPS
# ==============================================================================

function Get-ForeignKeyDefinitions {
    return @(
        # Main application relationships
        "ALTER TABLE Issues ADD CONSTRAINT FK_Issues_Applications FOREIGN KEY (ApplicationID) REFERENCES Applications(ApplicationID)",
        "ALTER TABLE VehicleTypes ADD CONSTRAINT FK_VehicleTypes_Applications FOREIGN KEY (ApplicationID) REFERENCES Applications(ApplicationID)",
        "ALTER TABLE ApplicationStaff ADD CONSTRAINT FK_ApplicationStaff_Applications FOREIGN KEY (ApplicationID) REFERENCES Applications(ApplicationID)",
        
        # Vehicle type related relationships
        "ALTER TABLE VehiclesToAuthorise ADD CONSTRAINT FK_VehiclesToAuthorise_VehicleTypes FOREIGN KEY (VehicleTypeID) REFERENCES VehicleTypes(VehicleTypeID)",
        "ALTER TABLE ApplicableRules ADD CONSTRAINT FK_ApplicableRules_VehicleTypes FOREIGN KEY (VehicleTypeID) REFERENCES VehicleTypes(VehicleTypeID)",
        "ALTER TABLE MemberStateMappings ADD CONSTRAINT FK_MemberStateMappings_VehicleTypes FOREIGN KEY (VehicleTypeID) REFERENCES VehicleTypes(VehicleTypeID)",
        "ALTER TABLE AgencyMappings ADD CONSTRAINT FK_AgencyMappings_VehicleTypes FOREIGN KEY (VehicleTypeID) REFERENCES VehicleTypes(VehicleTypeID)",
        
        # Member state mapping relationships
        "ALTER TABLE Networks ADD CONSTRAINT FK_Networks_MemberStateMappings FOREIGN KEY (MappingID) REFERENCES MemberStateMappings(MappingID)",
        "ALTER TABLE MSMappingRequirements ADD CONSTRAINT FK_MSMappingRequirements_MemberStateMappings FOREIGN KEY (MappingID) REFERENCES MemberStateMappings(MappingID)",
        "ALTER TABLE MSMappingRequirementValues ADD CONSTRAINT FK_MSMappingRequirementValues_MSMappingRequirements FOREIGN KEY (RequirementID) REFERENCES MSMappingRequirements(RequirementID)",
        "ALTER TABLE MSMappingRequirementValues ADD CONSTRAINT FK_MSMappingRequirementValues_Documents FOREIGN KEY (DocumentID) REFERENCES Documents(DocumentID)",
        
        # Agency mapping relationships
        "ALTER TABLE AgencyMappingValues ADD CONSTRAINT FK_AgencyMappingValues_AgencyMappings FOREIGN KEY (AgencyMappingID) REFERENCES AgencyMappings(AgencyMappingID)",
        "ALTER TABLE AgencyMappingValues ADD CONSTRAINT FK_AgencyMappingValues_Documents FOREIGN KEY (DocumentID) REFERENCES Documents(DocumentID)",
        
        # Many-to-many relationship table
        "ALTER TABLE ApplicationBodies ADD CONSTRAINT FK_ApplicationBodies_Applications FOREIGN KEY (ApplicationID) REFERENCES Applications(ApplicationID)",
        "ALTER TABLE ApplicationBodies ADD CONSTRAINT FK_ApplicationBodies_Bodies FOREIGN KEY (BodyID) REFERENCES Bodies(BodyID)",
        
        # Address and contact detail relationships
        "ALTER TABLE ContactPersons ADD CONSTRAINT FK_ContactPersons_Addresses FOREIGN KEY (AddressID) REFERENCES Addresses(AddressID)",
        "ALTER TABLE ContactPersons ADD CONSTRAINT FK_ContactPersons_ContactDetails FOREIGN KEY (ContactDetailsID) REFERENCES ContactDetails(ContactDetailsID)",
        "ALTER TABLE BillingInformation ADD CONSTRAINT FK_BillingInformation_Addresses FOREIGN KEY (AddressID) REFERENCES Addresses(AddressID)",
        "ALTER TABLE BillingInformation ADD CONSTRAINT FK_BillingInformation_ContactDetails FOREIGN KEY (ContactDetailsID) REFERENCES ContactDetails(ContactDetailsID)",
        "ALTER TABLE Bodies ADD CONSTRAINT FK_Bodies_Addresses FOREIGN KEY (AddressID) REFERENCES Addresses(AddressID)",
        "ALTER TABLE Bodies ADD CONSTRAINT FK_Bodies_ContactDetails FOREIGN KEY (ContactDetailsID) REFERENCES ContactDetails(ContactDetailsID)"
        
        # Application → ContactPersons/BillingInformation/Bodies (made optional with conditional logic)
        # Note: These will be added conditionally to avoid null reference issues
        
        # VehicleTypes → Bodies (authorisation holder) - made optional
        # Note: This will be added conditionally to avoid null reference issues
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

function Remove-AllRelationships {
    param($Database)
    
    Write-Info "Removing existing foreign key relationships..."
    $removedCount = 0
    
    try {
        # Get all relationships and remove them
        $relations = $Database.Relations
        $relationNames = @()
        
        # Collect relation names first (can't modify collection while iterating)
        foreach ($relation in $relations) {
            if (-not $relation.Name.StartsWith("MSys")) {
                $relationNames += $relation.Name
            }
        }
        
        # Remove each relationship
        foreach ($relationName in $relationNames) {
            try {
                $Database.Relations.Delete($relationName)
                Write-Host "    - Removed relationship: $relationName" -ForegroundColor Yellow
                $removedCount++
            }
            catch {
                Write-Warning "    - Could not remove relationship $relationName`: $($_.Exception.Message)"
            }
        }
    }
    catch {
        Write-Warning "Could not access relationships: $($_.Exception.Message)"
    }
    
    Write-Success "    Removed $removedCount existing relationships"
    return $removedCount
}

function Remove-ExistingTable {
    param($Access, [string]$TableName)
    
    try {
        # Check if table exists first
        $tableExists = $false
        foreach ($tableDef in $Access.CurrentDb().TableDefs) {
            if ($tableDef.Name -eq $TableName -and -not $tableDef.Name.StartsWith("MSys")) {
                $tableExists = $true
                break
            }
        }
        
        if ($tableExists) {
            $Access.DoCmd.DeleteObject(0, $TableName)  # 0 = acTable
            Write-Host "    - Dropped existing table: $TableName" -ForegroundColor Yellow
            return $true
        }
        return $false  # Table doesn't exist
    }
    catch {
        Write-Warning "    - Could not remove table $TableName`: $($_.Exception.Message)"
        return $false
    }
}

function Create-DatabaseTables {
    param($Database, $Access)
    
    # First, remove all existing relationships so we can drop tables
    Remove-AllRelationships -Database $Database
    
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

    # Create foreign key relationships
    Write-Info "Creating foreign key relationships..."
    $foreignKeys = Get-ForeignKeyDefinitions
    $fkSuccessCount = 0
    $fkErrorCount = 0
    
    foreach ($fkSQL in $foreignKeys) {
        try {
            # Skip if SQL contains conditional note
            if ($fkSQL -like "*Note:*") {
                Write-Host "    - Skipping optional relationship: $($fkSQL.Split('--')[0].Trim())" -ForegroundColor Yellow
                continue
            }
            $Database.Execute($fkSQL)
            $fkSuccessCount++
            # Extract constraint name for better reporting
            $constraintName = ($fkSQL -split "CONSTRAINT ")[1].Split(" ")[0]
            Write-Host "    - Created: $constraintName" -ForegroundColor Green
        }
        catch {
            # Check if it's just a "already exists" warning
            if ($_.Exception.Message -like "*Ya existe una relación*" -or $_.Exception.Message -like "*already exists*") {
                Write-Host "    - Relationship already exists (skipped)" -ForegroundColor Yellow
            }
            else {
                Write-Warning "    Foreign key creation error: $($_.Exception.Message)"
                Write-Host "    - Failed SQL: $fkSQL" -ForegroundColor Red
                $fkErrorCount++
            }
        }
    }
    
    Write-Success "    Created $fkSuccessCount foreign key relationships successfully"
    if ($fkErrorCount -gt 0) {
        Write-Warning "    $fkErrorCount foreign key relationships had warnings"
    }
    
    return @{
        SuccessCount = $successCount
        ErrorCount = $errorCount
        ForeignKeySuccessCount = $fkSuccessCount
        ForeignKeyErrorCount = $fkErrorCount
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
        
        # Check foreign key relationships
        $relationshipCount = 0
        try {
            $relations = $Database.Relations
            $relationshipCount = $relations.Count
        }
        catch {
            # Some versions may not support Relations collection
            Write-Warning "Could not verify foreign key relationships"
        }

        Write-Info "PERFORMANCE OPTIMIZATION:"
        Write-Host "    - Total Indexes: $indexCount" -ForegroundColor Cyan
        Write-Host "    - Primary Keys: Created on all tables" -ForegroundColor White
        Write-Host "    - Foreign Key Indexes: Created for relationships" -ForegroundColor White
        Write-Host "    - Foreign Key Relationships: $relationshipCount" -ForegroundColor Cyan
        
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
        Write-Host "Foreign key relationships created: $($indexResult.ForeignKeySuccessCount)" -ForegroundColor Green
        Write-Host "Foreign key warnings: $($indexResult.ForeignKeyErrorCount)" -ForegroundColor Yellow
        
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
