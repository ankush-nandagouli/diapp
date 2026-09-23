package com.example.data.model

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class SystemLogLevel {
    INFO,
    WARN,
    ERROR,
    SUCCESS,
    CRITICAL
}

typealias LogLevel = SystemLogLevel

enum class RemovalMotionStatus {
    ACTIVE,
    PASSED,
    REJECTED,
    CANCELLED,
    APPROVED
}

data class SystemLogEntry(
    val id: String = java.util.UUID.randomUUID().toString(),
    val timestamp: Long = System.currentTimeMillis(),
    val level: SystemLogLevel = SystemLogLevel.INFO,
    val tag: String = "SYSTEM",
    val category: String = tag,
    val message: String,
    val details: String? = ""
) {
    val formattedTime: String
        get() {
            val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
            return sdf.format(Date(timestamp))
        }

    val formattedDate: String
        get() {
            val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm:ss", Locale.getDefault())
            return sdf.format(Date(timestamp))
        }
}

data class AppHealthStatus(
    val isDatabaseHealthy: Boolean = true,
    val databaseTablesCount: Int = 9,
    val totalRecordsCount: Int = 0,
    val isCloudDatabaseActive: Boolean = true,
    val cloudDatabaseName: String = "Supabase PostgreSQL",
    val activeUserSession: String = "",
    val totalPartnersCount: Int = 4,
    val errorCount: Int = 0,
    val uptimeMillis: Long = System.currentTimeMillis(),
    val supabaseConnected: Boolean = true,
    val roomDbConnected: Boolean = true,
    val supabaseSyncStatus: String = "Supabase PostgreSQL Online (Direct REST & Realtime)",
    val totalTransactionsCount: Int = 0,
    val treasuryBalance: Double = 0.0,
    val totalProjectsCount: Int = 0,
    val activeRemovalMotionCount: Int = 0,
    val lastHealthCheck: Long = System.currentTimeMillis()
) {
    val isHealthy: Boolean get() = isDatabaseHealthy && errorCount == 0
}

data class PartnerRemovalMotion(
    val id: String = java.util.UUID.randomUUID().toString(),
    val motionId: String = id,
    val targetPartnerId: Long,
    val targetPartnerName: String,
    val proposedByPartnerId: Long,
    val proposedByPartnerName: String,
    val reason: String,
    val timestamp: Long = System.currentTimeMillis(),
    val status: RemovalMotionStatus = RemovalMotionStatus.ACTIVE,
    val votes: Map<Long, Boolean> = emptyMap(), // partnerId -> true (Approve) / false (Reject)
    val eligiblePartnersCount: Int = 3,
    val requiredMajority: Int = 2
)
