package com.example.data.local

/**
 * Pure Kotlin domain models for Dakshyam Innovations business entities,
 * synchronized directly with Supabase PostgreSQL cloud backend.
 * SQLite/Room database has been completely removed.
 */

data class PartnerEntity(
    val id: Long = 0,
    val name: String,
    val role: String = "Partner",
    val email: String = "",
    val phone: String = "",
    val capitalContributed: Double = 0.0,
    val isActive: Boolean = true,
    val avatarUrl: String = "",
    val bio: String = "",
    val password: String = "427752",
    val mustChangePassword: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

data class CashFlowEntity(
    val id: Long = 0,
    val type: String, // "EXPENSE", "CAPITAL_INJECTION", "CLIENT_INFLOW"
    val amount: Double,
    val category: String,
    val description: String,
    val paidByPartnerName: String,
    val paidByPartnerId: Long = 0,
    val projectName: String = "",
    val receiptUri: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

data class ProjectEntity(
    val id: Long = 0,
    val title: String,
    val client: String = "Internal R&D",
    val status: String = "In Progress", // "Planning", "In Progress", "Testing", "Completed", "On Hold"
    val budget: Double = 0.0,
    val assignedPartners: String = "",
    val imageUrl: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

data class ProjectComponentEntity(
    val id: Long = 0,
    val projectId: Long,
    val name: String,
    val quantity: Int = 1,
    val unitPrice: Double = 0.0,
    val totalPrice: Double = 0.0
)

data class DailyReportEntity(
    val id: Long = 0,
    val projectId: Long,
    val projectTitle: String,
    val reportedByPartner: String,
    val summary: String,
    val mediaUri: String = "",
    val mediaType: String = "NONE", // "PHOTO", "VIDEO", "NONE"
    val timestamp: Long = System.currentTimeMillis()
)

data class ActivityAlertEntity(
    val id: Long = 0,
    val title: String,
    val description: String,
    val category: String, // "FINANCE", "PROJECT", "PARTNER", "REPORT"
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
)

data class CompanyProfileEntity(
    val id: Long = 1L,
    val companyName: String = "Dakshyam Innovations",
    val tagline: String = "Engineering Next-Gen Robotics & Intelligent Systems",
    val logoUrl: String = "",
    val registrationNumber: String = "U72900MH2024PTC123456",
    val gstin: String = "27AABCD1234E1Z5",
    val officialEmail: String = "contact@dakshyam.com",
    val officialPhone: String = "+91 98765 43210",
    val officeAddress: String = "Technology Incubation Park, Suite 402, India",
    val website: String = "https://dakshyam.com",
    val updatedByPartner: String = "Authorized Partner",
    val updatedAt: Long = System.currentTimeMillis()
)
