package com.example.data.validation

import com.example.data.local.CashFlowEntity
import com.example.data.local.CompanyProfileEntity
import com.example.data.local.DailyReportEntity
import com.example.data.local.PartnerEntity
import com.example.data.local.ProjectEntity
import java.util.regex.Pattern

/**
 * Validation result containing validation status and field-specific errors.
 */
data class ValidationResult(
    val isValid: Boolean,
    val errors: Map<String, String> = emptyMap(),
    val warningMessage: String? = null
) {
    val firstErrorMessage: String? get() = errors.values.firstOrNull()

    companion object {
        fun success(warning: String? = null) = ValidationResult(true, emptyMap(), warning)
        fun failure(field: String, message: String) = ValidationResult(false, mapOf(field to message))
        fun failure(errors: Map<String, String>) = ValidationResult(false, errors)
    }
}

/**
 * Real validators connecting business operations to respective application features,
 * entity relationships, legal formats, and financial rules.
 */
object BusinessValidator {

    private val EMAIL_REGEX = Pattern.compile(
        "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,64}$"
    )

    // Indian GSTIN: 2-digit state code + 10-char PAN + 1-digit entity + 'Z' + 1 checksum char
    private val GSTIN_REGEX = Pattern.compile(
        "^[0-9]{2}[A-Z]{5}[0-9]{4}[A-Z]{1}[1-9A-Z]{1}Z[0-9A-Z]{1}$"
    )

    // Indian Phone: Optional +91 / 0 followed by 10 digits
    private val PHONE_REGEX = Pattern.compile(
        "^(\\+91[\\-\\s]?)?[6789]\\d{9}$"
    )

    // Corporate Registration Number / CIN / LLPIN
    private val REG_NUMBER_REGEX = Pattern.compile(
        "^[A-Z0-9\\-\\/]{6,30}$",
        Pattern.CASE_INSENSITIVE
    )

    val ALLOWED_EXPENSE_CATEGORIES = setOf(
        "Operations",
        "Hardware & R&D",
        "Salary & Stipend",
        "Office Rent & Utilities",
        "Marketing & Sales",
        "Legal & Compliance",
        "Travel & Field",
        "Software & Cloud",
        "Miscellaneous"
    )

    val ALLOWED_PROJECT_STATUSES = setOf(
        "Planning",
        "In Progress",
        "Testing",
        "Review",
        "Completed",
        "On Hold"
    )

    // =========================================================================
    // 1. COMPANY PROFILE VALIDATION
    // =========================================================================
    fun validateCompanyProfile(profile: CompanyProfileEntity): ValidationResult {
        val errors = mutableMapOf<String, String>()

        if (profile.companyName.trim().length < 3) {
            errors["companyName"] = "Company name must be at least 3 characters."
        }

        if (profile.officialEmail.isNotBlank() && !EMAIL_REGEX.matcher(profile.officialEmail.trim()).matches()) {
            errors["officialEmail"] = "Invalid official corporate email address format."
        }

        val cleanPhone = profile.officialPhone.replace(" ", "").replace("-", "")
        if (cleanPhone.isNotBlank() && !PHONE_REGEX.matcher(cleanPhone).matches()) {
            errors["officialPhone"] = "Phone number must be a valid 10-digit Indian contact number (+91...)."
        }

        val cleanGstin = profile.gstin.trim().uppercase()
        if (cleanGstin.isNotBlank() && !GSTIN_REGEX.matcher(cleanGstin).matches()) {
            errors["gstin"] = "GSTIN format invalid (Expected 15 alphanumeric characters e.g. 27AABCD1234E1Z5)."
        }

        val cleanReg = profile.registrationNumber.trim()
        if (cleanReg.isNotBlank() && !REG_NUMBER_REGEX.matcher(cleanReg).matches()) {
            errors["registrationNumber"] = "Invalid Corporate Registration Number / CIN format."
        }

        if (profile.website.isNotBlank() && !profile.website.startsWith("http://") && !profile.website.startsWith("https://")) {
            errors["website"] = "Website URL must start with http:// or https://"
        }

        return if (errors.isEmpty()) ValidationResult.success() else ValidationResult.failure(errors)
    }

    // =========================================================================
    // 2. CASH FLOW & FINANCIAL TRANSACTIONS VALIDATION
    // =========================================================================
    fun validateCashFlow(
        transaction: CashFlowEntity,
        currentNetBalance: Double,
        availableProjects: List<ProjectEntity> = emptyList(),
        availablePartners: List<PartnerEntity> = emptyList()
    ): ValidationResult {
        val errors = mutableMapOf<String, String>()
        var warning: String? = null

        if (transaction.amount <= 0.0) {
            errors["amount"] = "Transaction amount must be strictly greater than ₹0."
        } else if (transaction.amount > 100_000_000.0) {
            errors["amount"] = "Transaction amount exceeds enterprise threshold (₹10 Crores)."
        }

        if (transaction.description.trim().length < 3) {
            errors["description"] = "Description must provide at least 3 characters explaining the purpose."
        }

        if (transaction.type !in setOf("EXPENSE", "CAPITAL_INJECTION", "CLIENT_INFLOW")) {
            errors["type"] = "Invalid transaction type '${transaction.type}'."
        }

        // Validate partner relationship
        if (transaction.type == "CAPITAL_INJECTION") {
            if (transaction.paidByPartnerId <= 0L && transaction.paidByPartnerName.isBlank()) {
                errors["partner"] = "Capital injection must be mapped to a valid contributing partner."
            } else if (availablePartners.isNotEmpty() && transaction.paidByPartnerId > 0L) {
                val partnerExists = availablePartners.any { it.id == transaction.paidByPartnerId }
                if (!partnerExists) {
                    errors["partner"] = "Selected partner ID #${transaction.paidByPartnerId} not found in active partner directory."
                }
            }
        }

        // Validate project relationship
        if (transaction.projectName.isNotBlank() && availableProjects.isNotEmpty()) {
            val projectExists = availableProjects.any { it.title.equals(transaction.projectName.trim(), ignoreCase = true) }
            if (!projectExists) {
                warning = "Note: Project '${transaction.projectName}' is not currently active in projects catalog."
            }
        }

        // Treasury liquidity warning for expenses
        if (transaction.type == "EXPENSE" && transaction.amount > currentNetBalance) {
            warning = "Treasury Alert: Expense of ₹${transaction.amount.toLong()} exceeds available net treasury liquidity (₹${currentNetBalance.toLong()})."
        }

        return if (errors.isEmpty()) ValidationResult.success(warning) else ValidationResult.failure(errors)
    }

    fun validateTransaction(
        transaction: CashFlowEntity,
        currentNetBalance: Double = 0.0,
        availableProjects: List<ProjectEntity> = emptyList(),
        availablePartners: List<PartnerEntity> = emptyList()
    ): ValidationResult = validateCashFlow(transaction, currentNetBalance, availableProjects, availablePartners)

    // =========================================================================
    // 3. PROJECT VALIDATION
    // =========================================================================
    fun validateProject(
        project: ProjectEntity,
        availablePartners: List<PartnerEntity> = emptyList()
    ): ValidationResult {
        val errors = mutableMapOf<String, String>()

        if (project.title.trim().length < 3) {
            errors["title"] = "Project title must be at least 3 characters long."
        }

        if (project.client.trim().isEmpty()) {
            errors["client"] = "Client or R&D unit name is required."
        }

        if (project.budget < 0.0) {
            errors["budget"] = "Project budget cannot be negative."
        }

        if (project.status !in ALLOWED_PROJECT_STATUSES) {
            errors["status"] = "Invalid project status '${project.status}'."
        }

        // Validate assigned partners
        if (project.assignedPartners.isNotBlank() && availablePartners.isNotEmpty()) {
            val assignedNames = project.assignedPartners.split(",").map { it.trim().lowercase() }
            val partnerNames = availablePartners.map { it.name.trim().lowercase() }
            val unknownPartners = assignedNames.filter { assigned ->
                partnerNames.none { it.contains(assigned) || assigned.contains(it) }
            }
            if (unknownPartners.isNotEmpty() && unknownPartners.any { it != "all" && it != "all partners" }) {
                // Log minor error/warning
            }
        }

        return if (errors.isEmpty()) ValidationResult.success() else ValidationResult.failure(errors)
    }

    // =========================================================================
    // 4. DAILY PROGRESS REPORT (DPR) VALIDATION
    // =========================================================================
    fun validateDailyReport(
        report: DailyReportEntity,
        availableProjects: List<ProjectEntity> = emptyList(),
        availablePartners: List<PartnerEntity> = emptyList()
    ): ValidationResult {
        val errors = mutableMapOf<String, String>()

        if (report.summary.trim().length < 5) {
            errors["summary"] = "Report summary must be at least 5 characters detailing progress."
        }

        if (report.projectId <= 0L && report.projectTitle.isBlank()) {
            errors["project"] = "Report must be linked to a valid project."
        } else if (availableProjects.isNotEmpty() && report.projectId > 0L) {
            val projectExists = availableProjects.any { it.id == report.projectId }
            if (!projectExists) {
                errors["project"] = "Selected Project ID #${report.projectId} not found in project database."
            }
        }

        if (report.reportedByPartner.trim().isEmpty()) {
            errors["reportedBy"] = "Reporting partner identity is mandatory."
        }

        if (report.mediaType !in setOf("NONE", "PHOTO", "VIDEO", "DOCUMENT")) {
            errors["mediaType"] = "Invalid media type format."
        }

        return if (errors.isEmpty()) ValidationResult.success() else ValidationResult.failure(errors)
    }

    // =========================================================================
    // 5. PARTNER VALIDATION
    // =========================================================================
    fun validatePartner(
        partner: PartnerEntity,
        existingPartners: List<PartnerEntity> = emptyList()
    ): ValidationResult {
        val errors = mutableMapOf<String, String>()

        if (partner.name.trim().length < 2) {
            errors["name"] = "Partner name must be at least 2 characters long."
        }

        val cleanEmail = partner.email.trim().lowercase()
        if (cleanEmail.isEmpty()) {
            errors["email"] = "Email address is required."
        } else if (!EMAIL_REGEX.matcher(cleanEmail).matches()) {
            errors["email"] = "Invalid partner email address format."
        } else {
            // Check uniqueness if new partner
            val duplicate = existingPartners.any { it.id != partner.id && it.email.trim().equals(cleanEmail, ignoreCase = true) }
            if (duplicate) {
                errors["email"] = "Another partner with email '$cleanEmail' is already registered."
            }
        }

        val cleanPhone = partner.phone.replace(" ", "").replace("-", "")
        if (cleanPhone.isNotBlank() && !PHONE_REGEX.matcher(cleanPhone).matches()) {
            errors["phone"] = "Invalid phone number (Expected 10-digit number e.g. +91 98765 43210)."
        }

        if (partner.capitalContributed < 0.0) {
            errors["capital"] = "Capital contribution cannot be negative."
        }

        return if (errors.isEmpty()) ValidationResult.success() else ValidationResult.failure(errors)
    }
}
