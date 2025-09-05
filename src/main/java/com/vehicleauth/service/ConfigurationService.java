package com.vehicleauth.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * Service class for analyzing JSON files and generating field length configuration
 * Creates Fields_length.txt in Configuration folder with maximum text field lengths
 */
public class ConfigurationService {
    
    private static final Logger logger = LoggerFactory.getLogger(ConfigurationService.class);
    
    // Configuration constants
    private static final String JSON_FILES_DIR = "Json Files";
    private static final String CONFIG_DIR = "Configuration";
    private static final String CONFIG_FILE = "Fields_length.txt";
    
    // Database table to JSON field mappings based on the database schema
    private final Map<String, Map<String, String>> tableFieldMappings;
    
    public ConfigurationService() {
        this.tableFieldMappings = initializeTableFieldMappings();
    }
    
    /**
     * Analyze all JSON files and generate field length configuration
     * @return true if configuration file was generated successfully
     */
    public boolean generateFieldLengthConfiguration() {
        logger.info("Starting field length analysis of JSON files");
        
        try {
            // Ensure configuration directory exists
            createConfigurationDirectory();
            
            // Analyze all JSON files
            Map<String, Map<String, Integer>> maxFieldLengths = analyzeJsonFiles();
            
            // Generate configuration file
            generateConfigurationFile(maxFieldLengths);
            
            logger.info("Field length configuration generated successfully");
            return true;
            
        } catch (Exception e) {
            logger.error("Failed to generate field length configuration", e);
            System.err.println("❌ Configuration generation failed: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Create Configuration directory if it doesn't exist
     */
    private void createConfigurationDirectory() throws IOException {
        Path configDir = Paths.get(CONFIG_DIR);
        if (!Files.exists(configDir)) {
            Files.createDirectories(configDir);
            logger.info("Created configuration directory: {}", configDir.toAbsolutePath());
        }
    }
    
    /**
     * Analyze all JSON files in the Json Files directory
     */
    private Map<String, Map<String, Integer>> analyzeJsonFiles() throws IOException {
        Map<String, Map<String, Integer>> maxFieldLengths = new HashMap<>();
        ObjectMapper objectMapper = new ObjectMapper();
        
        // Initialize max lengths map
        for (String tableName : tableFieldMappings.keySet()) {
            maxFieldLengths.put(tableName, new HashMap<>());
            for (String fieldName : tableFieldMappings.get(tableName).keySet()) {
                maxFieldLengths.get(tableName).put(fieldName, 0);
            }
        }
        
        // Process all JSON files recursively
        Path jsonDir = Paths.get(JSON_FILES_DIR);
        if (!Files.exists(jsonDir)) {
            throw new IOException("JSON files directory not found: " + JSON_FILES_DIR);
        }
        
        Files.walk(jsonDir)
            .filter(path -> path.toString().toLowerCase().endsWith(".json"))
            .forEach(path -> {
                try {
                    System.out.println("📄 Analyzing: " + path.toString());
                    JsonNode rootNode = objectMapper.readTree(path.toFile());
                    analyzeJsonNode(rootNode, "", maxFieldLengths);
                } catch (Exception e) {
                    logger.warn("Failed to analyze file: " + path + " - " + e.getMessage());
                }
            });
        
        return maxFieldLengths;
    }
    
    /**
     * Recursively analyze JSON node and update maximum field lengths
     */
    private void analyzeJsonNode(JsonNode node, String currentPath, Map<String, Map<String, Integer>> maxFieldLengths) {
        if (node.isObject()) {
            node.fields().forEachRemaining(entry -> {
                String fieldName = entry.getKey();
                JsonNode fieldValue = entry.getValue();
                String fullPath = currentPath.isEmpty() ? fieldName : currentPath + "." + fieldName;
                
                // Check if this field maps to a database field
                updateFieldLength(fullPath, fieldValue, maxFieldLengths);
                
                // Recursively process nested objects and arrays
                if (fieldValue.isObject() || fieldValue.isArray()) {
                    analyzeJsonNode(fieldValue, fullPath, maxFieldLengths);
                }
            });
        } else if (node.isArray()) {
            for (int i = 0; i < node.size(); i++) {
                analyzeJsonNode(node.get(i), currentPath, maxFieldLengths);
            }
        }
    }
    
    /**
     * Update maximum field length if current value is longer
     */
    private void updateFieldLength(String jsonPath, JsonNode value, Map<String, Map<String, Integer>> maxFieldLengths) {
        if (value == null || value.isNull()) {
            return;
        }
        
        String stringValue = value.asText();
        if (stringValue == null || stringValue.isEmpty()) {
            return;
        }
        
        int length = stringValue.length();
        
        // Check all table mappings for this JSON path
        for (Map.Entry<String, Map<String, String>> tableEntry : tableFieldMappings.entrySet()) {
            String tableName = tableEntry.getKey();
            Map<String, String> fieldMappings = tableEntry.getValue();
            
            for (Map.Entry<String, String> fieldEntry : fieldMappings.entrySet()) {
                String dbField = fieldEntry.getKey();
                String jsonField = fieldEntry.getValue();
                
                // Check if JSON path matches this mapping
                if (jsonPath.equals(jsonField) || jsonPath.endsWith("." + jsonField)) {
                    Integer currentMax = maxFieldLengths.get(tableName).get(dbField);
                    if (currentMax == null || length > currentMax) {
                        maxFieldLengths.get(tableName).put(dbField, length);
                        logger.debug("Updated max length for {}.{}: {} (value: '{}')", 
                                   tableName, dbField, length, stringValue.substring(0, Math.min(50, length)));
                    }
                }
            }
        }
    }
    
    /**
     * Generate the configuration file
     */
    private void generateConfigurationFile(Map<String, Map<String, Integer>> maxFieldLengths) throws IOException {
        Path configFile = Paths.get(CONFIG_DIR, CONFIG_FILE);
        
        try (FileWriter writer = new FileWriter(configFile.toFile())) {
            writer.write("# Vehicle Authorization Database - Field Length Configuration\n");
            writer.write("# Generated on: " + new Date() + "\n");
            writer.write("# This file contains maximum text field lengths found in JSON sample data\n");
            writer.write("# Format: TableName.FieldName=MaxLength\n\n");
            
            // Sort tables alphabetically
            List<String> sortedTables = new ArrayList<>(maxFieldLengths.keySet());
            Collections.sort(sortedTables);
            
            for (String tableName : sortedTables) {
                writer.write("# " + tableName + " Table\n");
                Map<String, Integer> fieldLengths = maxFieldLengths.get(tableName);
                
                // Sort fields alphabetically
                List<String> sortedFields = new ArrayList<>(fieldLengths.keySet());
                Collections.sort(sortedFields);
                
                for (String fieldName : sortedFields) {
                    Integer maxLength = fieldLengths.get(fieldName);
                    // Add some padding to the max length (+20% with minimum +10)
                    int recommendedLength = maxLength + Math.max(10, (int)(maxLength * 0.2));
                    
                    writer.write(String.format("%s.%s=%d\n", tableName, fieldName, recommendedLength));
                }
                writer.write("\n");
            }
        }
        
        System.out.println("✅ Configuration file generated: " + configFile.toAbsolutePath());
        System.out.println("📊 Analyzed " + maxFieldLengths.size() + " database tables");
        
        // Print summary
        int totalFields = maxFieldLengths.values().stream()
                .mapToInt(map -> map.size())
                .sum();
        System.out.println("📋 Total text fields analyzed: " + totalFields);
    }
    
    /**
     * Initialize mappings between database table fields and JSON paths
     * Based on the database schema and JSON structure analysis
     */
    private Map<String, Map<String, String>> initializeTableFieldMappings() {
        Map<String, Map<String, String>> mappings = new HashMap<>();
        
        // Applications table mappings
        Map<String, String> applications = new HashMap<>();
        applications.put("ApplicationID", "applicationId");
        applications.put("ID", "id");
        applications.put("CaseType", "caseType");
        applications.put("NationalRegNumber", "nationalRegNumber");
        applications.put("ProjectName", "projectName");
        applications.put("ApplicationType", "applicationType");
        applications.put("ApplicationTypeVariantVersion", "applicationTypeVariantVersion");
        applications.put("IssuingAuthority", "issuingAuthority");
        applications.put("ApplicationStatus", "applicationStatus");
        applications.put("Phase", "phase");
        applications.put("EIN", "ein");
        applications.put("LegalDenomination", "legalDenomination");
        applications.put("VehicleIdentifier", "vehicleIdentifier");
        applications.put("MemberStates", "memberStates");
        applications.put("Subcategory", "subcategory");
        applications.put("PreEngagementID", "preEngagementId");
        applications.put("PreEngagementOtherInformation", "preEngagementOtherInformation");
        applications.put("DocLang", "docLang");
        applications.put("ApplicationVersion", "applicationVersion");
        applications.put("ContactPersonID", "contactPerson.id");
        applications.put("FinancialContactPersonID", "financialContactPerson.id");
        applications.put("BillingInformationID", "billingInformation.id");
        applications.put("ApplicantBodyID", "applicantBody.id");
        mappings.put("Applications", applications);
        
        // Addresses table mappings
        Map<String, String> addresses = new HashMap<>();
        addresses.put("AddressID", "address.id");
        addresses.put("Street", "address.street");
        addresses.put("City", "address.city");
        addresses.put("PostalCode", "address.postalCode");
        addresses.put("CountryCode", "address.code");
        mappings.put("Addresses", addresses);
        
        // ContactDetails table mappings
        Map<String, String> contactDetails = new HashMap<>();
        contactDetails.put("ContactDetailsID", "contactDetails.id");
        contactDetails.put("Phone", "contactDetails.phone");
        contactDetails.put("Fax", "contactDetails.fax");
        contactDetails.put("Email", "contactDetails.email");
        contactDetails.put("Website", "contactDetails.website");
        mappings.put("ContactDetails", contactDetails);
        
        // ContactPersons table mappings
        Map<String, String> contactPersons = new HashMap<>();
        contactPersons.put("ContactPersonID", "contactPerson.id");
        contactPersons.put("FirstName", "contactPerson.firstName");
        contactPersons.put("Surname", "contactPerson.surname");
        contactPersons.put("TitleOrFunction", "contactPerson.titleOrFunction");
        contactPersons.put("AddressID", "contactPerson.userAddress.id");
        contactPersons.put("ContactDetailsID", "contactPerson.userContactDetails.id");
        contactPersons.put("LanguagesSpoken", "contactPerson.langSpoken");
        contactPersons.put("PersonType", "contactPerson.personType");
        mappings.put("ContactPersons", contactPersons);
        
        // BillingInformation table mappings
        Map<String, String> billing = new HashMap<>();
        billing.put("BillingID", "billingInformation.id");
        billing.put("LegalDenomination", "billingInformation.legalDenomination");
        billing.put("Acronym", "billingInformation.acronym");
        billing.put("VATNumber", "billingInformation.vatNumber");
        billing.put("NationalRegNumber", "billingInformation.nationalRegNumber");
        billing.put("AddressID", "billingInformation.address.id");
        billing.put("ContactDetailsID", "billingInformation.contactDetails.id");
        billing.put("SpecificBillingRequirements", "billingInformation.specificBillingRequirements");
        mappings.put("BillingInformation", billing);
        
        // Bodies table mappings
        Map<String, String> bodies = new HashMap<>();
        bodies.put("BodyID", "applicantBody.id");
        bodies.put("BodyType", "bodyType");
        bodies.put("LegalDenomination", "applicantBody.legalDenomination");
        bodies.put("Acronym", "applicantBody.acronym");
        bodies.put("VATNumber", "applicantBody.vatNumber");
        bodies.put("NationalRegNumber", "applicantBody.nationalRegNumber");
        bodies.put("AddressID", "applicantBody.address.id");
        bodies.put("ContactDetailsID", "applicantBody.contactDetails.id");
        bodies.put("AdditionalInfo", "applicantBody.additionalInfo");
        bodies.put("BodyName", "applicantBody.name");
        bodies.put("BodyIdNumber", "applicantBody.bodyId");
        bodies.put("EINNumber", "applicantBody.einNumber");
        mappings.put("Bodies", bodies);
        
        // Issues table mappings
        Map<String, String> issues = new HashMap<>();
        issues.put("IssueID", "issueId");
        issues.put("ID", "id");
        issues.put("ApplicationID", "applicationId");
        issues.put("Title", "title");
        issues.put("IssueDescription", "issueDescription");
        issues.put("Owner", "owner");
        issues.put("OwnerDisplayName", "ownerDisplayName");
        issues.put("Assignees", "assignees");
        issues.put("AssigneesDisplayNames", "assigneesDisplayNames");
        issues.put("IssueType", "type");
        issues.put("IssueStatus", "issueStatus");
        issues.put("Resolution", "resolution");
        issues.put("AssessmentStage", "assessmentStage");
        issues.put("ResolutionText", "resolutionText");
        issues.put("ResolutionDescription", "resolutionDescription");
        mappings.put("Issues", issues);
        
        // VehicleTypes table mappings
        Map<String, String> vehicleTypes = new HashMap<>();
        vehicleTypes.put("VehicleTypeID", "variantsTypesList.id");
        vehicleTypes.put("ApplicationID", "applicationId");
        vehicleTypes.put("AuthorizationType", "variantsTypesList.authorisationType");
        vehicleTypes.put("VehicleType", "variantsTypesList.vehicleType");
        vehicleTypes.put("TypeID", "variantsTypesList.typeId");
        vehicleTypes.put("TypeName", "variantsTypesList.typeName");
        vehicleTypes.put("AltTypeName", "variantsTypesList.altTypeName");
        vehicleTypes.put("ReferenceToExistingStr", "variantsTypesList.referenceToExistingStr");
        vehicleTypes.put("DescriptionNew", "variantsTypesList.descriptionNew");
        vehicleTypes.put("VehicleIdentifier", "variantsTypesList.vehicleIdentifier");
        vehicleTypes.put("VehicleValue", "variantsTypesList.vehicleValue");
        vehicleTypes.put("AuthorizationHolderID", "variantsTypesList.authorisationHolder.id");
        vehicleTypes.put("VehicleMainCategory", "variantsTypesList.vehicleMainCategory");
        vehicleTypes.put("VehicleSubCategory", "variantsTypesList.vehicleSubCategory");
        vehicleTypes.put("NonCodedRestrictions", "variantsTypesList.nonCodedRestrictions");
        vehicleTypes.put("CodedRestrictions", "variantsTypesList.codedRestrictions");
        vehicleTypes.put("ChangeSummary", "variantsTypesList.changeSummary");
        vehicleTypes.put("RegistrationEntityRecipients", "variantsTypesList.registrationEntityRecipients");
        mappings.put("VehicleTypes", vehicleTypes);
        
        // VehiclesToAuthorise table mappings
        Map<String, String> vehiclesToAuth = new HashMap<>();
        vehiclesToAuth.put("VehicleToAuthoriseID", "variantsTypesList.vehiclesToAuthorise.id");
        vehiclesToAuth.put("VehicleTypeID", "variantsTypesList.id");
        vehiclesToAuth.put("VehicleValue", "variantsTypesList.vehiclesToAuthorise.vehicleValue");
        vehiclesToAuth.put("VehicleIdentifier", "variantsTypesList.vehiclesToAuthorise.vehicleIdentifier");
        mappings.put("VehiclesToAuthorise", vehiclesToAuth);
        
        // Documents table mappings
        Map<String, String> documents = new HashMap<>();
        documents.put("DocumentID", "document.id");
        documents.put("FileTitle", "document.fileTitle");
        documents.put("FilePath", "document.filePath");
        mappings.put("Documents", documents);
        
        // ApplicationBodies table mappings
        Map<String, String> applicationBodies = new HashMap<>();
        applicationBodies.put("ApplicationID", "applicationId");
        applicationBodies.put("BodyID", "applicantBody.id");
        applicationBodies.put("BodyRole", "bodyRole");
        mappings.put("ApplicationBodies", applicationBodies);
        
        // ApplicationStaff table mappings
        Map<String, String> applicationStaff = new HashMap<>();
        applicationStaff.put("ApplicationID", "applicationId");
        applicationStaff.put("StaffType", "staffType");
        applicationStaff.put("StaffName", "staffName");
        mappings.put("ApplicationStaff", applicationStaff);
        
        // ApplicableRules table mappings
        Map<String, String> applicableRules = new HashMap<>();
        applicableRules.put("RuleID", "variantsTypesList.uiApplicableRules.rules.id");
        applicableRules.put("VehicleTypeID", "variantsTypesList.id");
        applicableRules.put("RuleType", "variantsTypesList.uiApplicableRules.type");
        applicableRules.put("MSCode", "variantsTypesList.uiApplicableRules.msCode");
        applicableRules.put("Comment", "variantsTypesList.uiApplicableRules.rules.comment");
        applicableRules.put("Directive", "variantsTypesList.uiApplicableRules.rules.directive");
        mappings.put("ApplicableRules", applicableRules);
        
        // MemberStateMappings table mappings
        Map<String, String> memberStateMappings = new HashMap<>();
        memberStateMappings.put("MappingID", "variantsTypesList.msMappings.id");
        memberStateMappings.put("VehicleTypeID", "variantsTypesList.id");
        memberStateMappings.put("CountryCode", "variantsTypesList.msMappings.code");
        memberStateMappings.put("Name", "variantsTypesList.msMappings.name");
        memberStateMappings.put("ShuntingOnlyTxt", "variantsTypesList.msMappings.shuntingOnlyTxt");
        memberStateMappings.put("OtherDescription", "variantsTypesList.msMappings.otherDescription");
        memberStateMappings.put("AssigneeStr", "variantsTypesList.msMappings.assigneeStr");
        mappings.put("MemberStateMappings", memberStateMappings);
        
        // AgencyMappings table mappings
        Map<String, String> agencyMappings = new HashMap<>();
        agencyMappings.put("AgencyMappingID", "variantsTypesList.agencyMappings.id");
        agencyMappings.put("VehicleTypeID", "variantsTypesList.id");
        agencyMappings.put("Requirement", "variantsTypesList.agencyMappings.requirement");
        agencyMappings.put("RequirementDescr", "variantsTypesList.agencyMappings.requirementDescr");
        mappings.put("AgencyMappings", agencyMappings);
        
        // Networks table mappings
        Map<String, String> networks = new HashMap<>();
        networks.put("MappingID", "variantsTypesList.msMappings.id");
        networks.put("NetworkName", "variantsTypesList.msMappings.networks");
        mappings.put("Networks", networks);
        
        // AgencyMappingValues table mappings
        Map<String, String> agencyMappingValues = new HashMap<>();
        agencyMappingValues.put("ValueID", "variantsTypesList.agencyMappings.values.id");
        agencyMappingValues.put("AgencyMappingID", "variantsTypesList.agencyMappings.id");
        agencyMappingValues.put("DocumentID", "variantsTypesList.agencyMappings.values.document.id");
        agencyMappingValues.put("ValueDescription", "variantsTypesList.agencyMappings.values.description");
        agencyMappingValues.put("ValueText", "variantsTypesList.agencyMappings.values.text");
        mappings.put("AgencyMappingValues", agencyMappingValues);
        
        // MSMappingRequirements table mappings
        Map<String, String> msMappingRequirements = new HashMap<>();
        msMappingRequirements.put("RequirementID", "msMappingRequirements.id");
        msMappingRequirements.put("MappingID", "variantsTypesList.msMappings.id");
        msMappingRequirements.put("Requirement", "msMappingRequirements.requirement");
        msMappingRequirements.put("RequirementDescr", "msMappingRequirements.requirementDescr");
        mappings.put("MSMappingRequirements", msMappingRequirements);
        
        // MSMappingRequirementValues table mappings
        Map<String, String> msMappingRequirementValues = new HashMap<>();
        msMappingRequirementValues.put("ValueID", "msMappingRequirementValues.id");
        msMappingRequirementValues.put("RequirementID", "msMappingRequirements.id");
        msMappingRequirementValues.put("DocumentID", "msMappingRequirementValues.document.id");
        msMappingRequirementValues.put("ValueDescription", "msMappingRequirementValues.description");
        msMappingRequirementValues.put("ValueText", "msMappingRequirementValues.text");
        mappings.put("MSMappingRequirementValues", msMappingRequirementValues);
        
        return mappings;
    }
    
    /**
     * Get service information
     */
    public String getServiceInfo() {
        StringBuilder info = new StringBuilder();
        info.append("Configuration Service Information:\n");
        info.append("- JSON Files Directory: ").append(JSON_FILES_DIR).append("\n");
        info.append("- Configuration Directory: ").append(CONFIG_DIR).append("\n");
        info.append("- Configuration File: ").append(CONFIG_FILE).append("\n");
        info.append("- Database Tables Mapped: ").append(tableFieldMappings.size()).append("\n");
        
        int totalFields = tableFieldMappings.values().stream()
                .mapToInt(map -> map.size())
                .sum();
        info.append("- Total Text Fields: ").append(totalFields).append("\n");
        
        return info.toString();
    }
}
