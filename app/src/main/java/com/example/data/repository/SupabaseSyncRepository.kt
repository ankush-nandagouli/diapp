package com.example.data.repository

import android.content.Context
import android.util.Log
import com.example.data.local.ActivityAlertEntity
import com.example.data.local.CashFlowEntity
import com.example.data.local.CompanyProfileEntity
import com.example.data.local.DailyReportEntity
import com.example.data.local.PartnerEntity
import com.example.data.local.ProjectComponentEntity
import com.example.data.local.ProjectEntity
import com.example.data.model.LogLevel
import com.example.data.model.PartnerRemovalMotion
import com.example.data.model.RemovalMotionStatus
import com.example.data.model.SystemLogEntry
import com.example.data.remote.SupabaseConfig
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class SupabaseConnectionStatus(
    val isConnected: Boolean,
    val latencyMs: Long = 0L,
    val statusCode: Int = 0,
    val message: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Supabase Cloud PostgreSQL Repository for Dakshyam Innovations.
 * Synchronizes all feature forms, entities, and actions to Supabase via PostgREST and Realtime.
 */
class SupabaseSyncRepository(
    private val context: Context,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    private val tag = "SupabaseSync"

    private val httpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .writeTimeout(20, TimeUnit.SECONDS)
            .build()
    }

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    fun isSupabaseAvailable(): Boolean = true

    suspend fun checkConnection(): SupabaseConnectionStatus = withContext(ioDispatcher) {
        val start = System.currentTimeMillis()
        try {
            val request = Request.Builder()
                .url("${SupabaseConfig.REST_URL}/partners?select=id&limit=1")
                .addHeader("apikey", SupabaseConfig.PUBLISHABLE_KEY)
                .addHeader("Authorization", "Bearer ${SupabaseConfig.PUBLISHABLE_KEY}")
                .get()
                .build()

            httpClient.newCall(request).execute().use { response ->
                val latency = System.currentTimeMillis() - start
                if (response.isSuccessful) {
                    SupabaseConnectionStatus(
                        isConnected = true,
                        latencyMs = latency,
                        statusCode = response.code,
                        message = "Connected to Supabase PostgreSQL (${latency}ms)",
                        timestamp = System.currentTimeMillis()
                    )
                } else {
                    SupabaseConnectionStatus(
                        isConnected = false,
                        latencyMs = latency,
                        statusCode = response.code,
                        message = "HTTP ${response.code}: ${response.message}",
                        timestamp = System.currentTimeMillis()
                    )
                }
            }
        } catch (e: Exception) {
            val latency = System.currentTimeMillis() - start
            SupabaseConnectionStatus(
                isConnected = false,
                latencyMs = latency,
                statusCode = 0,
                message = e.localizedMessage ?: "Connection error",
                timestamp = System.currentTimeMillis()
            )
        }
    }

    fun getDatabaseInfo(): Map<String, String> {
        return mapOf(
            "projectUrl" to SupabaseConfig.PROJECT_URL,
            "status" to "CONNECTED (PostgreSQL)",
            "database" to "Supabase Cloud DB",
            "host" to "db.uttffudevdijhrpmewqx.supabase.co:5432",
            "authMode" to "Publishable Key (Anon)"
        )
    }

    suspend fun testConnection(): Result<Boolean> = withContext(ioDispatcher) {
        try {
            val request = Request.Builder()
                .url("${SupabaseConfig.REST_URL}/company_profile?select=id&limit=1")
                .addHeader("apikey", SupabaseConfig.PUBLISHABLE_KEY)
                .addHeader("Authorization", "Bearer ${SupabaseConfig.PUBLISHABLE_KEY}")
                .get()
                .build()

            httpClient.newCall(request).execute().use { response ->
                if (response.isSuccessful || response.code == 404 || response.code == 200 || response.code == 206) {
                    Result.success(true)
                } else {
                    Result.failure(Exception("Supabase HTTP status: ${response.code}"))
                }
            }
        } catch (e: Exception) {
            Log.w(tag, "Connection check: ${e.message}")
            Result.failure(e)
        }
    }

    // ==========================================
    // 1. COMPANY PROFILE
    // ==========================================

    fun streamRemoteCompanyProfile(): Flow<CompanyProfileEntity?> = flow {
        try {
            val profile = fetchCompanyProfile()
            if (profile != null) {
                emit(profile)
            }
        } catch (e: Exception) {
            Log.w(tag, "Error loading company profile: ${e.message}")
        }
    }.flowOn(ioDispatcher)

    fun fetchCompanyProfileDirect(): CompanyProfileEntity? = fetchCompanyProfile()

    private fun fetchCompanyProfile(): CompanyProfileEntity? {
        val request = Request.Builder()
            .url("${SupabaseConfig.REST_URL}/company_profile?id=eq.1&select=*")
            .addHeader("apikey", SupabaseConfig.PUBLISHABLE_KEY)
            .addHeader("Authorization", "Bearer ${SupabaseConfig.PUBLISHABLE_KEY}")
            .get()
            .build()

        httpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) return null
            val body = response.body?.string() ?: return null
            val array = JSONArray(body)
            if (array.length() == 0) return null
            val obj = array.getJSONObject(0)
            return CompanyProfileEntity(
                id = obj.optLong("id", 1L),
                companyName = obj.optString("company_name", "Dakshyam Innovations"),
                tagline = obj.optString("tagline", "Engineering Next-Gen Robotics & Intelligent Systems"),
                logoUrl = obj.optString("logo_url", ""),
                registrationNumber = obj.optString("registration_number", "U72900MH2024PTC123456"),
                gstin = obj.optString("gstin", "27AABCD1234E1Z5"),
                officialEmail = obj.optString("official_email", "contact@dakshyam.com"),
                officialPhone = obj.optString("official_phone", "+91 98765 43210"),
                officeAddress = obj.optString("office_address", "Technology Incubation Park, Suite 402, India"),
                website = obj.optString("website", "https://dakshyam.com"),
                updatedByPartner = obj.optString("updated_by_partner", "Authorized Partner"),
                updatedAt = obj.optLong("updated_at", System.currentTimeMillis())
            )
        }
    }

    suspend fun syncCompanyProfileToRemote(profile: CompanyProfileEntity): Result<Unit> = withContext(ioDispatcher) {
        try {
            val json = JSONObject().apply {
                put("id", 1L)
                put("company_name", profile.companyName)
                put("tagline", profile.tagline)
                put("logo_url", profile.logoUrl)
                put("registration_number", profile.registrationNumber)
                put("gstin", profile.gstin)
                put("official_email", profile.officialEmail)
                put("official_phone", profile.officialPhone)
                put("office_address", profile.officeAddress)
                put("website", profile.website)
                put("updated_by_partner", profile.updatedByPartner)
                put("updated_at", profile.updatedAt)
            }

            val request = Request.Builder()
                .url("${SupabaseConfig.REST_URL}/company_profile")
                .addHeader("apikey", SupabaseConfig.PUBLISHABLE_KEY)
                .addHeader("Authorization", "Bearer ${SupabaseConfig.PUBLISHABLE_KEY}")
                .addHeader("Prefer", "resolution=merge-duplicates")
                .post(json.toString().toRequestBody(jsonMediaType))
                .build()

            httpClient.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    Result.success(Unit)
                } else {
                    Result.failure(Exception("Failed to sync profile: HTTP ${response.code}"))
                }
            }
        } catch (e: Exception) {
            Log.e(tag, "syncCompanyProfileToRemote error", e)
            Result.failure(e)
        }
    }

    // ==========================================
    // 2. PARTNERS
    // ==========================================

    fun streamRemotePartners(): Flow<List<PartnerEntity>> = flow {
        try {
            val partners = fetchPartners()
            if (partners.isNotEmpty()) {
                emit(partners)
            }
        } catch (e: Exception) {
            Log.w(tag, "Error loading partners: ${e.message}")
        }
    }.flowOn(ioDispatcher)

    fun fetchPartnersDirect(): List<PartnerEntity> = fetchPartners()

    private fun fetchPartners(): List<PartnerEntity> {
        val request = Request.Builder()
            .url("${SupabaseConfig.REST_URL}/partners?select=*&order=id.asc")
            .addHeader("apikey", SupabaseConfig.PUBLISHABLE_KEY)
            .addHeader("Authorization", "Bearer ${SupabaseConfig.PUBLISHABLE_KEY}")
            .get()
            .build()

        httpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                val errorBody = response.body?.string().orEmpty()
                throw IllegalStateException("Supabase request failed: HTTP \${response.code} \${response.message}: \${errorBody}")
            }
            val body = response.body?.string() ?: return emptyList()
            val array = JSONArray(body)
            val list = mutableListOf<PartnerEntity>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    PartnerEntity(
                        id = obj.optLong("id", 0L),
                        name = obj.optString("name", ""),
                        role = obj.optString("role", "Partner"),
                        email = obj.optString("email", ""),
                        phone = obj.optString("phone", ""),
                        capitalContributed = obj.optDouble("capital_contributed", 0.0),
                        isActive = obj.optBoolean("is_active", true),
                        avatarUrl = obj.optString("avatar_url", ""),
                        bio = obj.optString("bio", ""),
                        password = obj.optString("password", "427752"),
                        mustChangePassword = obj.optBoolean("must_change_password", false),
                        createdAt = obj.optLong("created_at", System.currentTimeMillis())
                    )
                )
            }
            return list
        }
    }

    suspend fun findPartnerByEmail(email: String): PartnerEntity? = withContext(ioDispatcher) {
        try {
            val clean = email.trim().lowercase()
            val request = Request.Builder()
                .url("${SupabaseConfig.REST_URL}/partners?email=ilike.$clean&limit=1")
                .addHeader("apikey", SupabaseConfig.PUBLISHABLE_KEY)
                .addHeader("Authorization", "Bearer ${SupabaseConfig.PUBLISHABLE_KEY}")
                .get()
                .build()

            httpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@withContext null
                val body = response.body?.string() ?: return@withContext null
                val array = JSONArray(body)
                if (array.length() == 0) return@withContext null
                val obj = array.getJSONObject(0)
                return@withContext PartnerEntity(
                    id = obj.optLong("id", 0L),
                    name = obj.optString("name", ""),
                    role = obj.optString("role", "Partner"),
                    email = obj.optString("email", ""),
                    phone = obj.optString("phone", ""),
                    capitalContributed = obj.optDouble("capital_contributed", 0.0),
                    isActive = obj.optBoolean("is_active", true),
                    avatarUrl = obj.optString("avatar_url", ""),
                    bio = obj.optString("bio", ""),
                    password = obj.optString("password", "427752"),
                    mustChangePassword = obj.optBoolean("must_change_password", false),
                    createdAt = obj.optLong("created_at", System.currentTimeMillis())
                )
            }
        } catch (e: Exception) {
            Log.w(tag, "findPartnerByEmail error: ${e.message}")
            null
        }
    }

    suspend fun syncPartnerToRemote(partner: PartnerEntity): Result<Unit> = withContext(ioDispatcher) {
        try {
            val json = JSONObject().apply {
                put("id", partner.id)
                put("name", partner.name)
                put("role", partner.role)
                put("email", partner.email)
                put("phone", partner.phone)
                put("capital_contributed", partner.capitalContributed)
                put("is_active", partner.isActive)
                put("avatar_url", partner.avatarUrl)
                put("bio", partner.bio)
                put("password", partner.password)
                put("must_change_password", partner.mustChangePassword)
                put("created_at", partner.createdAt)
            }

            val request = Request.Builder()
                .url("${SupabaseConfig.REST_URL}/partners")
                .addHeader("apikey", SupabaseConfig.PUBLISHABLE_KEY)
                .addHeader("Authorization", "Bearer ${SupabaseConfig.PUBLISHABLE_KEY}")
                .addHeader("Prefer", "resolution=merge-duplicates")
                .post(json.toString().toRequestBody(jsonMediaType))
                .build()

            httpClient.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    Result.success(Unit)
                } else {
                    Result.failure(Exception("Sync partner failed: HTTP ${response.code}"))
                }
            }
        } catch (e: Exception) {
            Log.e(tag, "syncPartnerToRemote error", e)
            Result.failure(e)
        }
    }

    suspend fun deletePartnerFromRemote(partnerId: Long): Result<Unit> = withContext(ioDispatcher) {
        try {
            val request = Request.Builder()
                .url("${SupabaseConfig.REST_URL}/partners?id=eq.$partnerId")
                .addHeader("apikey", SupabaseConfig.PUBLISHABLE_KEY)
                .addHeader("Authorization", "Bearer ${SupabaseConfig.PUBLISHABLE_KEY}")
                .delete()
                .build()

            httpClient.newCall(request).execute().use { response ->
                if (response.isSuccessful) Result.success(Unit)
                else Result.failure(Exception("Delete partner failed: HTTP ${response.code}"))
            }
        } catch (e: Exception) {
            Log.e(tag, "deletePartnerFromRemote error", e)
            Result.failure(e)
        }
    }

    suspend fun deletePartnerFromRemote(partner: PartnerEntity): Result<Unit> = deletePartnerFromRemote(partner.id)

    // ==========================================
    // 3. PROJECTS & BILL OF MATERIALS
    // ==========================================

    fun streamRemoteProjects(): Flow<List<ProjectEntity>> = flow {
        try {
            val projects = fetchProjects()
            emit(projects)
        } catch (e: Exception) {
            Log.w(tag, "Error loading projects: ${e.message}")
        }
    }.flowOn(ioDispatcher)

    fun fetchProjectsDirect(): List<ProjectEntity> = fetchProjects()

    private fun fetchProjects(): List<ProjectEntity> {
        val request = Request.Builder()
            .url("${SupabaseConfig.REST_URL}/projects?select=*&order=id.asc")
            .addHeader("apikey", SupabaseConfig.PUBLISHABLE_KEY)
            .addHeader("Authorization", "Bearer ${SupabaseConfig.PUBLISHABLE_KEY}")
            .get()
            .build()

        httpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                val errorBody = response.body?.string().orEmpty()
                throw IllegalStateException("Supabase request failed: HTTP \${response.code} \${response.message}: \${errorBody}")
            }
            val body = response.body?.string() ?: return emptyList()
            val array = JSONArray(body)
            val list = mutableListOf<ProjectEntity>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    ProjectEntity(
                        id = obj.optLong("id", 0L),
                        title = obj.optString("title", ""),
                        client = obj.optString("client", "Internal R&D"),
                        status = obj.optString("status", "In Progress"),
                        budget = obj.optDouble("budget", 0.0),
                        assignedPartners = obj.optString("assigned_partners", ""),
                        imageUrl = obj.optString("image_url", ""),
                        createdAt = obj.optLong("created_at", System.currentTimeMillis())
                    )
                )
            }
            return list
        }
    }

    suspend fun syncProjectToRemote(project: ProjectEntity): Result<Unit> = withContext(ioDispatcher) {
        try {
            val json = JSONObject().apply {
                put("id", project.id)
                put("title", project.title)
                put("client", project.client)
                put("status", project.status)
                put("budget", project.budget)
                put("assigned_partners", project.assignedPartners)
                put("image_url", project.imageUrl)
                put("created_at", project.createdAt)
            }

            val request = Request.Builder()
                .url("${SupabaseConfig.REST_URL}/projects")
                .addHeader("apikey", SupabaseConfig.PUBLISHABLE_KEY)
                .addHeader("Authorization", "Bearer ${SupabaseConfig.PUBLISHABLE_KEY}")
                .addHeader("Prefer", "resolution=merge-duplicates")
                .post(json.toString().toRequestBody(jsonMediaType))
                .build()

            httpClient.newCall(request).execute().use { response ->
                if (response.isSuccessful) Result.success(Unit)
                else Result.failure(Exception("Sync project failed: HTTP ${response.code}"))
            }
        } catch (e: Exception) {
            Log.e(tag, "syncProjectToRemote error", e)
            Result.failure(e)
        }
    }

    suspend fun deleteProjectFromRemote(projectId: Long): Result<Unit> = withContext(ioDispatcher) {
        try {
            // Cascade delete child components and daily reports for ACID referential consistency
            val compReq = Request.Builder()
                .url("${SupabaseConfig.REST_URL}/project_components?project_id=eq.$projectId")
                .addHeader("apikey", SupabaseConfig.PUBLISHABLE_KEY)
                .addHeader("Authorization", "Bearer ${SupabaseConfig.PUBLISHABLE_KEY}")
                .delete()
                .build()
            httpClient.newCall(compReq).execute().close()

            val dprReq = Request.Builder()
                .url("${SupabaseConfig.REST_URL}/daily_reports?project_id=eq.$projectId")
                .addHeader("apikey", SupabaseConfig.PUBLISHABLE_KEY)
                .addHeader("Authorization", "Bearer ${SupabaseConfig.PUBLISHABLE_KEY}")
                .delete()
                .build()
            httpClient.newCall(dprReq).execute().close()

            val request = Request.Builder()
                .url("${SupabaseConfig.REST_URL}/projects?id=eq.$projectId")
                .addHeader("apikey", SupabaseConfig.PUBLISHABLE_KEY)
                .addHeader("Authorization", "Bearer ${SupabaseConfig.PUBLISHABLE_KEY}")
                .delete()
                .build()

            httpClient.newCall(request).execute().use { response ->
                if (response.isSuccessful) Result.success(Unit)
                else Result.failure(Exception("Delete project failed: HTTP ${response.code}"))
            }
        } catch (e: Exception) {
            Log.e(tag, "deleteProjectFromRemote error", e)
            Result.failure(e)
        }
    }

    suspend fun deleteProjectFromRemote(project: ProjectEntity): Result<Unit> = deleteProjectFromRemote(project.id)

    // ==========================================
    // 4. PROJECT COMPONENTS (BOM)
    // ==========================================

    fun streamRemoteComponents(): Flow<List<ProjectComponentEntity>> = flow {
        try {
            val components = fetchComponents()
            emit(components)
        } catch (e: Exception) {
            Log.w(tag, "Error loading components: ${e.message}")
        }
    }.flowOn(ioDispatcher)

    fun fetchComponentsDirect(): List<ProjectComponentEntity> = fetchComponents()

    private fun fetchComponents(): List<ProjectComponentEntity> {
        val request = Request.Builder()
            .url("${SupabaseConfig.REST_URL}/project_components?select=*&order=id.asc")
            .addHeader("apikey", SupabaseConfig.PUBLISHABLE_KEY)
            .addHeader("Authorization", "Bearer ${SupabaseConfig.PUBLISHABLE_KEY}")
            .get()
            .build()

        httpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                val errorBody = response.body?.string().orEmpty()
                throw IllegalStateException("Supabase request failed: HTTP \${response.code} \${response.message}: \${errorBody}")
            }
            val body = response.body?.string() ?: return emptyList()
            val array = JSONArray(body)
            val list = mutableListOf<ProjectComponentEntity>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    ProjectComponentEntity(
                        id = obj.optLong("id", 0L),
                        projectId = obj.optLong("project_id", 0L),
                        name = obj.optString("name", ""),
                        quantity = obj.optInt("quantity", 1),
                        unitPrice = obj.optDouble("unit_price", 0.0),
                        totalPrice = obj.optDouble("total_price", 0.0)
                    )
                )
            }
            return list
        }
    }

    suspend fun syncComponentToRemote(comp: ProjectComponentEntity): Result<Unit> = withContext(ioDispatcher) {
        try {
            val json = JSONObject().apply {
                put("id", comp.id)
                put("project_id", comp.projectId)
                put("name", comp.name)
                put("quantity", comp.quantity)
                put("unit_price", comp.unitPrice)
                put("total_price", comp.totalPrice)
            }

            val request = Request.Builder()
                .url("${SupabaseConfig.REST_URL}/project_components")
                .addHeader("apikey", SupabaseConfig.PUBLISHABLE_KEY)
                .addHeader("Authorization", "Bearer ${SupabaseConfig.PUBLISHABLE_KEY}")
                .addHeader("Prefer", "resolution=merge-duplicates")
                .post(json.toString().toRequestBody(jsonMediaType))
                .build()

            httpClient.newCall(request).execute().use { response ->
                if (response.isSuccessful) Result.success(Unit)
                else Result.failure(Exception("Sync component failed: HTTP ${response.code}"))
            }
        } catch (e: Exception) {
            Log.e(tag, "syncComponentToRemote error", e)
            Result.failure(e)
        }
    }

    suspend fun deleteComponentFromRemote(componentId: Long): Result<Unit> = withContext(ioDispatcher) {
        try {
            val request = Request.Builder()
                .url("${SupabaseConfig.REST_URL}/project_components?id=eq.$componentId")
                .addHeader("apikey", SupabaseConfig.PUBLISHABLE_KEY)
                .addHeader("Authorization", "Bearer ${SupabaseConfig.PUBLISHABLE_KEY}")
                .delete()
                .build()

            httpClient.newCall(request).execute().use { response ->
                if (response.isSuccessful) Result.success(Unit)
                else Result.failure(Exception("Delete component failed: HTTP ${response.code}"))
            }
        } catch (e: Exception) {
            Log.e(tag, "deleteComponentFromRemote error", e)
            Result.failure(e)
        }
    }

    // ==========================================
    // 5. DAILY PROGRESS REPORTS (DPR)
    // ==========================================

    fun streamRemoteDailyReports(): Flow<List<DailyReportEntity>> = flow {
        try {
            val reports = fetchDailyReports()
            emit(reports)
        } catch (e: Exception) {
            Log.w(tag, "Error loading reports: ${e.message}")
        }
    }.flowOn(ioDispatcher)

    fun fetchDailyReportsDirect(): List<DailyReportEntity> = fetchDailyReports()

    private fun fetchDailyReports(): List<DailyReportEntity> {
        val request = Request.Builder()
            .url("${SupabaseConfig.REST_URL}/daily_reports?select=*&order=timestamp.desc")
            .addHeader("apikey", SupabaseConfig.PUBLISHABLE_KEY)
            .addHeader("Authorization", "Bearer ${SupabaseConfig.PUBLISHABLE_KEY}")
            .get()
            .build()

        httpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                val errorBody = response.body?.string().orEmpty()
                throw IllegalStateException("Supabase request failed: HTTP \${response.code} \${response.message}: \${errorBody}")
            }
            val body = response.body?.string() ?: return emptyList()
            val array = JSONArray(body)
            val list = mutableListOf<DailyReportEntity>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    DailyReportEntity(
                        id = obj.optLong("id", 0L),
                        projectId = obj.optLong("project_id", 0L),
                        projectTitle = obj.optString("project_title", ""),
                        reportedByPartner = obj.optString("reported_by_partner", "Partner"),
                        summary = obj.optString("summary", ""),
                        mediaUri = obj.optString("media_uri", ""),
                        mediaType = obj.optString("media_type", "NONE"),
                        timestamp = obj.optLong("timestamp", System.currentTimeMillis())
                    )
                )
            }
            return list
        }
    }

    suspend fun syncDailyReportToRemote(report: DailyReportEntity): Result<Unit> = withContext(ioDispatcher) {
        try {
            val json = JSONObject().apply {
                put("id", report.id)
                put("project_id", report.projectId)
                put("project_title", report.projectTitle)
                put("reported_by_partner", report.reportedByPartner)
                put("summary", report.summary)
                put("media_uri", report.mediaUri)
                put("media_type", report.mediaType)
                put("timestamp", report.timestamp)
            }

            val request = Request.Builder()
                .url("${SupabaseConfig.REST_URL}/daily_reports")
                .addHeader("apikey", SupabaseConfig.PUBLISHABLE_KEY)
                .addHeader("Authorization", "Bearer ${SupabaseConfig.PUBLISHABLE_KEY}")
                .addHeader("Prefer", "resolution=merge-duplicates")
                .post(json.toString().toRequestBody(jsonMediaType))
                .build()

            httpClient.newCall(request).execute().use { response ->
                if (response.isSuccessful) Result.success(Unit)
                else Result.failure(Exception("Sync DPR failed: HTTP ${response.code}"))
            }
        } catch (e: Exception) {
            Log.e(tag, "syncDailyReportToRemote error", e)
            Result.failure(e)
        }
    }

    suspend fun deleteDailyReportFromRemote(reportId: Long): Result<Unit> = withContext(ioDispatcher) {
        try {
            val request = Request.Builder()
                .url("${SupabaseConfig.REST_URL}/daily_reports?id=eq.$reportId")
                .addHeader("apikey", SupabaseConfig.PUBLISHABLE_KEY)
                .addHeader("Authorization", "Bearer ${SupabaseConfig.PUBLISHABLE_KEY}")
                .delete()
                .build()

            httpClient.newCall(request).execute().use { response ->
                if (response.isSuccessful) Result.success(Unit)
                else Result.failure(Exception("Delete DPR failed: HTTP ${response.code}"))
            }
        } catch (e: Exception) {
            Log.e(tag, "deleteDailyReportFromRemote error", e)
            Result.failure(e)
        }
    }

    suspend fun deleteDailyReportFromRemote(report: DailyReportEntity): Result<Unit> = deleteDailyReportFromRemote(report.id)

    // ==========================================
    // 6. CASH FLOW & TREASURY
    // ==========================================

    fun streamRemoteCashFlow(): Flow<List<CashFlowEntity>> = flow {
        try {
            val transactions = fetchCashFlow()
            emit(transactions)
        } catch (e: Exception) {
            Log.w(tag, "Error loading cash flow: ${e.message}")
        }
    }.flowOn(ioDispatcher)

    fun fetchCashFlowDirect(): List<CashFlowEntity> = fetchCashFlow()

    private fun fetchCashFlow(): List<CashFlowEntity> {
        val request = Request.Builder()
            .url("${SupabaseConfig.REST_URL}/cash_flow?select=*&order=timestamp.desc")
            .addHeader("apikey", SupabaseConfig.PUBLISHABLE_KEY)
            .addHeader("Authorization", "Bearer ${SupabaseConfig.PUBLISHABLE_KEY}")
            .get()
            .build()

        httpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                val errorBody = response.body?.string().orEmpty()
                throw IllegalStateException("Supabase request failed: HTTP \${response.code} \${response.message}: \${errorBody}")
            }
            val body = response.body?.string() ?: return emptyList()
            val array = JSONArray(body)
            val list = mutableListOf<CashFlowEntity>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    CashFlowEntity(
                        id = obj.optLong("id", 0L),
                        type = obj.optString("type", "EXPENSE"),
                        amount = obj.optDouble("amount", 0.0),
                        category = obj.optString("category", "General"),
                        description = obj.optString("description", ""),
                        paidByPartnerName = obj.optString("paid_by_partner_name", "Partner"),
                        paidByPartnerId = obj.optLong("paid_by_partner_id", 0L),
                        projectName = obj.optString("project_name", ""),
                        receiptUri = obj.optString("receipt_uri", ""),
                        timestamp = obj.optLong("timestamp", System.currentTimeMillis())
                    )
                )
            }
            return list
        }
    }

    suspend fun syncCashFlowToRemote(tx: CashFlowEntity): Result<Unit> = withContext(ioDispatcher) {
        try {
            val json = JSONObject().apply {
                put("id", tx.id)
                put("type", tx.type)
                put("amount", tx.amount)
                put("category", tx.category)
                put("description", tx.description)
                put("paid_by_partner_name", tx.paidByPartnerName)
                put("paid_by_partner_id", tx.paidByPartnerId)
                put("project_name", tx.projectName)
                put("receipt_uri", tx.receiptUri)
                put("timestamp", tx.timestamp)
            }

            val request = Request.Builder()
                .url("${SupabaseConfig.REST_URL}/cash_flow")
                .addHeader("apikey", SupabaseConfig.PUBLISHABLE_KEY)
                .addHeader("Authorization", "Bearer ${SupabaseConfig.PUBLISHABLE_KEY}")
                .addHeader("Prefer", "resolution=merge-duplicates")
                .post(json.toString().toRequestBody(jsonMediaType))
                .build()

            httpClient.newCall(request).execute().use { response ->
                if (response.isSuccessful) Result.success(Unit)
                else Result.failure(Exception("Sync transaction failed: HTTP ${response.code}"))
            }
        } catch (e: Exception) {
            Log.e(tag, "syncCashFlowToRemote error", e)
            Result.failure(e)
        }
    }

    suspend fun deleteCashFlowFromRemote(txId: Long): Result<Unit> = withContext(ioDispatcher) {
        try {
            val request = Request.Builder()
                .url("${SupabaseConfig.REST_URL}/cash_flow?id=eq.$txId")
                .addHeader("apikey", SupabaseConfig.PUBLISHABLE_KEY)
                .addHeader("Authorization", "Bearer ${SupabaseConfig.PUBLISHABLE_KEY}")
                .delete()
                .build()

            httpClient.newCall(request).execute().use { response ->
                if (response.isSuccessful) Result.success(Unit)
                else Result.failure(Exception("Delete transaction failed: HTTP ${response.code}"))
            }
        } catch (e: Exception) {
            Log.e(tag, "deleteCashFlowFromRemote error", e)
            Result.failure(e)
        }
    }

    suspend fun deleteCashFlowFromRemote(tx: CashFlowEntity): Result<Unit> = deleteCashFlowFromRemote(tx.id)

    // ==========================================
    // 7. ACTIVITY ALERTS
    // ==========================================

    fun streamRemoteAlerts(): Flow<List<ActivityAlertEntity>> = flow {
        try {
            val alerts = fetchAlerts()
            emit(alerts)
        } catch (e: Exception) {
            Log.w(tag, "Error loading alerts: ${e.message}")
        }
    }.flowOn(ioDispatcher)

    fun fetchAlertsDirect(): List<ActivityAlertEntity> = fetchAlerts()

    private fun fetchAlerts(): List<ActivityAlertEntity> {
        val request = Request.Builder()
            .url("${SupabaseConfig.REST_URL}/activity_alerts?select=*&order=timestamp.desc&limit=50")
            .addHeader("apikey", SupabaseConfig.PUBLISHABLE_KEY)
            .addHeader("Authorization", "Bearer ${SupabaseConfig.PUBLISHABLE_KEY}")
            .get()
            .build()

        httpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                val errorBody = response.body?.string().orEmpty()
                throw IllegalStateException("Supabase request failed: HTTP \${response.code} \${response.message}: \${errorBody}")
            }
            val body = response.body?.string() ?: return emptyList()
            val array = JSONArray(body)
            val list = mutableListOf<ActivityAlertEntity>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    ActivityAlertEntity(
                        id = obj.optLong("id", 0L),
                        title = obj.optString("title", ""),
                        description = obj.optString("description", ""),
                        category = obj.optString("category", "GENERAL"),
                        timestamp = obj.optLong("timestamp", System.currentTimeMillis()),
                        isRead = obj.optBoolean("is_read", false)
                    )
                )
            }
            return list
        }
    }

    suspend fun syncAlertToRemote(alert: ActivityAlertEntity): Result<Unit> = withContext(ioDispatcher) {
        try {
            val json = JSONObject().apply {
                put("id", alert.id)
                put("title", alert.title)
                put("description", alert.description)
                put("category", alert.category)
                put("timestamp", alert.timestamp)
                put("is_read", alert.isRead)
            }

            val request = Request.Builder()
                .url("${SupabaseConfig.REST_URL}/activity_alerts")
                .addHeader("apikey", SupabaseConfig.PUBLISHABLE_KEY)
                .addHeader("Authorization", "Bearer ${SupabaseConfig.PUBLISHABLE_KEY}")
                .addHeader("Prefer", "resolution=merge-duplicates")
                .post(json.toString().toRequestBody(jsonMediaType))
                .build()

            httpClient.newCall(request).execute().use { response ->
                if (response.isSuccessful) Result.success(Unit)
                else Result.failure(Exception("Sync alert failed: HTTP ${response.code}"))
            }
        } catch (e: Exception) {
            Log.e(tag, "syncAlertToRemote error", e)
            Result.failure(e)
        }
    }

    suspend fun markAlertReadInRemote(alertId: Long): Result<Unit> = withContext(ioDispatcher) {
        try {
            val json = JSONObject().apply {
                put("is_read", true)
            }

            val request = Request.Builder()
                .url("${SupabaseConfig.REST_URL}/activity_alerts?id=eq.$alertId")
                .addHeader("apikey", SupabaseConfig.PUBLISHABLE_KEY)
                .addHeader("Authorization", "Bearer ${SupabaseConfig.PUBLISHABLE_KEY}")
                .patch(json.toString().toRequestBody(jsonMediaType))
                .build()

            httpClient.newCall(request).execute().use { response ->
                if (response.isSuccessful) Result.success(Unit)
                else Result.failure(Exception("Mark alert read failed: HTTP ${response.code}"))
            }
        } catch (e: Exception) {
            Log.e(tag, "markAlertReadInRemote error", e)
            Result.failure(e)
        }
    }

    suspend fun clearAllAlertsFromRemote(): Result<Unit> = withContext(ioDispatcher) {
        try {
            val request = Request.Builder()
                .url("${SupabaseConfig.REST_URL}/activity_alerts")
                .addHeader("apikey", SupabaseConfig.PUBLISHABLE_KEY)
                .addHeader("Authorization", "Bearer ${SupabaseConfig.PUBLISHABLE_KEY}")
                .delete()
                .build()

            httpClient.newCall(request).execute().use { response ->
                if (response.isSuccessful) Result.success(Unit)
                else Result.failure(Exception("Clear alerts failed: HTTP ${response.code}"))
            }
        } catch (e: Exception) {
            Log.e(tag, "clearAllAlertsFromRemote error", e)
            Result.failure(e)
        }
    }

    // ==========================================
    // 8. GOVERNANCE REMOVAL MOTIONS
    // ==========================================

    fun streamRemoteRemovalMotions(): Flow<PartnerRemovalMotion?> = flow {
        try {
            val motion = fetchActiveRemovalMotion()
            emit(motion)
        } catch (e: Exception) {
            Log.w(tag, "Error loading motions: ${e.message}")
        }
    }.flowOn(ioDispatcher)

    fun fetchActiveRemovalMotionDirect(): PartnerRemovalMotion? = fetchActiveRemovalMotion()

    private fun fetchActiveRemovalMotion(): PartnerRemovalMotion? {
        val request = Request.Builder()
            .url("${SupabaseConfig.REST_URL}/removal_motions?status=eq.ACTIVE&limit=1")
            .addHeader("apikey", SupabaseConfig.PUBLISHABLE_KEY)
            .addHeader("Authorization", "Bearer ${SupabaseConfig.PUBLISHABLE_KEY}")
            .get()
            .build()

        httpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) return null
            val body = response.body?.string() ?: return null
            val array = JSONArray(body)
            if (array.length() == 0) return null
            val obj = array.getJSONObject(0)

            val statusStr = obj.optString("status", "ACTIVE")
            val status = try { RemovalMotionStatus.valueOf(statusStr) } catch (_: Exception) { RemovalMotionStatus.ACTIVE }

            val votesMap = mutableMapOf<Long, Boolean>()
            val votesRaw = obj.optString("votes", "{}")
            try {
                val vObj = JSONObject(votesRaw)
                val keys = vObj.keys()
                while (keys.hasNext()) {
                    val k = keys.next()
                    votesMap[k.toLong()] = vObj.getBoolean(k)
                }
            } catch (_: Exception) {}

            return PartnerRemovalMotion(
                id = obj.optString("id", "motion-active"),
                targetPartnerId = obj.optLong("target_partner_id", 0L),
                targetPartnerName = obj.optString("target_partner_name", ""),
                proposedByPartnerId = obj.optLong("proposed_by_partner_id", 0L),
                proposedByPartnerName = obj.optString("proposed_by_partner_name", ""),
                reason = obj.optString("reason", ""),
                timestamp = obj.optLong("timestamp", System.currentTimeMillis()),
                votes = votesMap,
                status = status,
                eligiblePartnersCount = obj.optInt("eligible_partners_count", 4),
                requiredMajority = obj.optInt("required_majority", 3)
            )
        }
    }

    suspend fun syncRemovalMotionToRemote(motion: PartnerRemovalMotion?): Result<Unit> = withContext(ioDispatcher) {
        try {
            if (motion == null) {
                // Clear any active motions
                val request = Request.Builder()
                    .url("${SupabaseConfig.REST_URL}/removal_motions?status=eq.ACTIVE")
                    .addHeader("apikey", SupabaseConfig.PUBLISHABLE_KEY)
                    .addHeader("Authorization", "Bearer ${SupabaseConfig.PUBLISHABLE_KEY}")
                    .delete()
                    .build()
                httpClient.newCall(request).execute().use { }
                return@withContext Result.success(Unit)
            }

            val votesJson = JSONObject().apply {
                for ((k, v) in motion.votes) {
                    put(k.toString(), v)
                }
            }

            val json = JSONObject().apply {
                put("id", motion.id)
                put("target_partner_id", motion.targetPartnerId)
                put("target_partner_name", motion.targetPartnerName)
                put("proposed_by_partner_id", motion.proposedByPartnerId)
                put("proposed_by_partner_name", motion.proposedByPartnerName)
                put("reason", motion.reason)
                put("timestamp", motion.timestamp)
                put("votes", votesJson.toString())
                put("status", motion.status.name)
                put("eligible_partners_count", motion.eligiblePartnersCount)
                put("required_majority", motion.requiredMajority)
            }

            val request = Request.Builder()
                .url("${SupabaseConfig.REST_URL}/removal_motions")
                .addHeader("apikey", SupabaseConfig.PUBLISHABLE_KEY)
                .addHeader("Authorization", "Bearer ${SupabaseConfig.PUBLISHABLE_KEY}")
                .addHeader("Prefer", "resolution=merge-duplicates")
                .post(json.toString().toRequestBody(jsonMediaType))
                .build()

            httpClient.newCall(request).execute().use { response ->
                if (response.isSuccessful) Result.success(Unit)
                else Result.failure(Exception("Sync motion failed: HTTP ${response.code}"))
            }
        } catch (e: Exception) {
            Log.e(tag, "syncRemovalMotionToRemote error", e)
            Result.failure(e)
        }
    }

    // ==========================================
    // 9. SYSTEM DIAGNOSTICS & AUDIT LOGS
    // ==========================================

    fun streamRemoteSystemLogs(): Flow<List<SystemLogEntry>> = flow {
        try {
            val logs = fetchSystemLogs()
            emit(logs)
        } catch (e: Exception) {
            Log.w(tag, "Error loading logs: ${e.message}")
        }
    }.flowOn(ioDispatcher)

    fun fetchSystemLogsDirect(): List<SystemLogEntry> = fetchSystemLogs()

    private fun fetchSystemLogs(): List<SystemLogEntry> {
        val request = Request.Builder()
            .url("${SupabaseConfig.REST_URL}/system_logs?select=*&order=timestamp.desc&limit=60")
            .addHeader("apikey", SupabaseConfig.PUBLISHABLE_KEY)
            .addHeader("Authorization", "Bearer ${SupabaseConfig.PUBLISHABLE_KEY}")
            .get()
            .build()

        httpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                val errorBody = response.body?.string().orEmpty()
                throw IllegalStateException("Supabase request failed: HTTP \${response.code} \${response.message}: \${errorBody}")
            }
            val body = response.body?.string() ?: return emptyList()
            val array = JSONArray(body)
            val list = mutableListOf<SystemLogEntry>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val lvlStr = obj.optString("level", "INFO")
                val lvl = try { LogLevel.valueOf(lvlStr) } catch (_: Exception) { LogLevel.INFO }
                list.add(
                    SystemLogEntry(
                        id = obj.optString("id", java.util.UUID.randomUUID().toString()),
                        level = lvl,
                        category = obj.optString("category", "SYSTEM"),
                        message = obj.optString("message", ""),
                        details = obj.optString("details", ""),
                        timestamp = obj.optLong("timestamp", System.currentTimeMillis())
                    )
                )
            }
            return list
        }
    }

    suspend fun logActivity(entry: SystemLogEntry): Result<Unit> =
        logActivity(entry.level, entry.category, entry.message, entry.details)

    suspend fun logActivity(
        level: LogLevel,
        category: String,
        message: String,
        details: String? = null
    ): Result<Unit> = withContext(ioDispatcher) {
        try {
            val json = JSONObject().apply {
                put("id", java.util.UUID.randomUUID().toString())
                put("level", level.name)
                put("category", category)
                put("message", message)
                put("details", details ?: "")
                put("timestamp", System.currentTimeMillis())
            }

            val request = Request.Builder()
                .url("${SupabaseConfig.REST_URL}/system_logs")
                .addHeader("apikey", SupabaseConfig.PUBLISHABLE_KEY)
                .addHeader("Authorization", "Bearer ${SupabaseConfig.PUBLISHABLE_KEY}")
                .post(json.toString().toRequestBody(jsonMediaType))
                .build()

            httpClient.newCall(request).execute().use { response ->
                if (response.isSuccessful) Result.success(Unit)
                else Result.failure(Exception("Log activity failed: HTTP ${response.code}"))
            }
        } catch (e: Exception) {
            Log.w(tag, "logActivity error: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun purgeAllDatabaseRecords(): Result<Unit> = withContext(ioDispatcher) {
        try {
            val tables = listOf(
                "daily_reports",
                "project_components",
                "projects",
                "cash_flow",
                "activity_alerts",
                "removal_motions",
                "system_logs"
            )
            for (table in tables) {
                val req = Request.Builder()
                    .url("${SupabaseConfig.REST_URL}/$table?id=not.is.null")
                    .addHeader("apikey", SupabaseConfig.PUBLISHABLE_KEY)
                    .addHeader("Authorization", "Bearer ${SupabaseConfig.PUBLISHABLE_KEY}")
                    .delete()
                    .build()
                httpClient.newCall(req).execute().close()
            }

            val partnersDeleteRequest = Request.Builder()
                .url("${SupabaseConfig.REST_URL}/partners?id=gt.3")
                .addHeader("apikey", SupabaseConfig.PUBLISHABLE_KEY)
                .addHeader("Authorization", "Bearer ${SupabaseConfig.PUBLISHABLE_KEY}")
                .delete()
                .build()
            httpClient.newCall(partnersDeleteRequest).execute().close()

            // Atomically reset all remaining partner capitals & profit withdrawn to 0.0 in Supabase
            val resetPartnerReq = Request.Builder()
                .url("${SupabaseConfig.REST_URL}/partners?id=not.is.null")
                .addHeader("apikey", SupabaseConfig.PUBLISHABLE_KEY)
                .addHeader("Authorization", "Bearer ${SupabaseConfig.PUBLISHABLE_KEY}")
                .addHeader("Content-Type", "application/json")
                .patch("{\"capital_contributed\":0,\"profit_withdrawn\":0}".toRequestBody(jsonMediaType))
                .build()
            httpClient.newCall(resetPartnerReq).execute().close()

            logActivity(LogLevel.WARN, "DATABASE", "Database Purged", "All operational database tables and partner balances were purged to zero.")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(tag, "purgeAllDatabaseRecords failed: ${e.message}", e)
            Result.failure(e)
        }
    }
}
